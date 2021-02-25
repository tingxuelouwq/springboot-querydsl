package com.kevin.querydsl;

import com.kevin.querydsl.entity.*;
import com.kevin.querydsl.jpa.UserJPA;
import com.kevin.querydsl.util.JsonUtil;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SpringBootTest
class SpringbootQuerydslApplicationTests {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /********************纯QueryDSL**************************/

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Test
    public void testFindByUsernameAndAge() {
        QUserEntity qUserEntity = QUserEntity.userEntity;
        UserEntity userEntity = jpaQueryFactory.selectFrom(qUserEntity)
                .where(qUserEntity.name.eq("kevin"),
                        qUserEntity.age.eq(28))
                .fetchOne();
        logger.info(JsonUtil.bean2Json(userEntity));
    }

    @Test
    public void testFindAll() {
        QUserEntity qUserEntity = QUserEntity.userEntity;
        List<UserEntity> userEntities = jpaQueryFactory.selectFrom(qUserEntity)
                .orderBy(qUserEntity.age.asc())
                .fetch();
        logger.info(JsonUtil.bean2Json(userEntities));
    }

    @Test
    public void testPageFind() {
        int pageNo = 1;
        int pageSize = 2;

        QUserEntity qUserEntity = QUserEntity.userEntity;
        QueryResults<UserEntity> results = jpaQueryFactory.selectFrom(qUserEntity)
                .orderBy(qUserEntity.age.asc())
                .offset(pageNo)
                .limit(pageSize)
                .fetchResults();

        long total = results.getTotal();
        long totalPage = (total - 1) / pageSize + 1;
        logger.info("分页查询第:[{}]页,pageSize:[{}],共有:[{}]数据,共有:[{}]页",
                pageNo, pageSize, total, totalPage);
        List<UserEntity> userEntities = results.getResults();
        logger.info(JsonUtil.bean2Json(userEntities));
    }

    @Test
    public void testLikeFindAndBetween() {
        QUserEntity qUserEntity = QUserEntity.userEntity;
        List<UserEntity> userEntities = jpaQueryFactory.selectFrom(qUserEntity)
                .where(
                        qUserEntity.name.like("%ry"),
                        qUserEntity.age.between(20, 30)
                ).orderBy(qUserEntity.id.asc())
                .fetch();
        logger.info(JsonUtil.bean2Json(userEntities));
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testUpdateDSL() {
        QUserEntity qUserEntity = QUserEntity.userEntity;
        jpaQueryFactory.update(qUserEntity)
                .set(qUserEntity.name, "cookie3")
                .set(qUserEntity.address, "农业展览馆")
                .where(qUserEntity.id.eq(7L))
                .execute();
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testDeleteDSL() {
        QUserEntity qUserEntity = QUserEntity.userEntity;
        jpaQueryFactory.delete(qUserEntity)
                .where(qUserEntity.id.eq(7L))
                .execute();
    }

    @Test
    public void testJoin() {
        QGoodInfoEntity qGoodInfo = QGoodInfoEntity.goodInfoEntity;
        QGoodTypeEntity qGoodType = QGoodTypeEntity.goodTypeEntity;
        List<GoodInfoEntity> goods = jpaQueryFactory.select(qGoodInfo)
                .from(qGoodInfo, qGoodType)
                .where(qGoodInfo.typeId.eq(qGoodType.id)
                        .and(qGoodType.id.eq(3L))
                ).orderBy(qGoodInfo.order.desc())
                .fetch();
        logger.info(JsonUtil.bean2Json(goods));
    }

    /**********************SpringDataJPA&QueryDSL整合*******************/

    @Autowired
    private UserJPA userJPA;

    @Test
    public void testWhere() {
        QUserEntity qUserEntity = QUserEntity.userEntity;
        Optional<UserEntity> optional = userJPA.findOne(
                qUserEntity.name.eq("kevin"));
        optional.ifPresent(userEntity ->
                logger.info(JsonUtil.bean2Json(userEntity)));
    }

    @Test
    public void testLikeAndPage() {
        int pageNo = 1;
        int pageSize = 2;

        QUserEntity qUserEntity = QUserEntity.userEntity;
        // 模糊查询条件
        BooleanExpression expression = qUserEntity.age.like("2%")
                .and(qUserEntity.age.between(20, 30));
        // 排序、分页参数
        Sort sort = Sort.by(Sort.Direction.DESC, "age");
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, sort);
        Page<UserEntity> page = userJPA.findAll(expression, pageRequest);
        logger.info("分页查询第:[{}]页,pageSize:[{}],共有:[{}]数据,共有:[{}]页",
                pageNo, pageSize, page.getTotalElements(), page.getTotalPages());
        List<UserEntity> userEntities = page.getContent();
        logger.info(JsonUtil.bean2Json(userEntities));
    }

    @Test
    public void testDynamicQueryAndPage() {
        Integer age = 28;
        String address = "丰台";
        String name = "ev";

        int pageNo = 0; // 第几页
        int pageSize = 2; // 每页大小

        QUserEntity qUserEntity = QUserEntity.userEntity;
        // 初始化组装条件(类似where 1=1)
        Predicate predicate = qUserEntity.isNotNull().or(qUserEntity.isNull());

        // 执行动态条件拼装
        // 相等
        predicate = age == null ? predicate :
                ExpressionUtils.and(predicate, qUserEntity.age.eq(age));
        // like 模糊匹配
        predicate = address == null ? predicate :
                ExpressionUtils.and(predicate, qUserEntity.address.like(address + "%"));
        // 包含，相当于like %xxx%
        predicate = name == null ? predicate :
                ExpressionUtils.and(predicate, qUserEntity.name.contains(name));

        // 排序、分页参数
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageRequest = PageRequest.of(pageNo < 0 ? 0 : pageNo, pageSize, sort);
        Page<UserEntity> page = userJPA.findAll(predicate, pageRequest);
        logger.info("分页查询第:[{}]页,pageSize:[{}],共有:[{}]数据,共有:[{}]页",
                pageNo, pageSize, page.getTotalElements(), page.getTotalPages());
        List<UserEntity> userEntities = page.getContent();
        logger.info(JsonUtil.bean2Json(userEntities));
    }

    @Test
    public void testJpaUpdate() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(7L);
        userEntity.setName("cookie2");
        userEntity.setAge(30);
        userEntity.setAddress("军事博物馆");
        userEntity.setPwd("tttx");
        userJPA.save(userEntity);
    }

    @Test
    public void testJpaDelete() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(6L);
        userJPA.delete(userEntity);
    }
}
