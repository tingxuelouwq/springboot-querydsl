package com.kevin.querydsl;

import com.kevin.querydsl.dto.GoodDTO;
import com.kevin.querydsl.entity.*;
import com.kevin.querydsl.jpa.UserJPA;
import com.kevin.querydsl.util.JsonUtil;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
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
import java.util.stream.Collectors;

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

    @Test
    public void testCustomDTO() {
        QGoodInfoEntity qGoodInfo = QGoodInfoEntity.goodInfoEntity;
        QGoodTypeEntity qGoodType = QGoodTypeEntity.goodTypeEntity;
        List<GoodDTO> result = jpaQueryFactory
                .select(Projections.bean(
                        GoodDTO.class,  // 指定返回的自定义实体的类型
                        qGoodInfo.id,
                        qGoodInfo.price,
                        qGoodInfo.title,
                        qGoodInfo.unit,
                        qGoodType.name.as("typeName"),  // 使用别名对应dto内的typeName
                        qGoodType.id.as("typeId")))
                .from(qGoodInfo, qGoodType)
                .where(qGoodType.id.eq(qGoodInfo.typeId))
                .orderBy(qGoodInfo.order.desc())
                .fetch();
        logger.info(JsonUtil.bean2Json(result));
    }

    @Test
    public void testCustomDTOByStream() {
        QGoodInfoEntity qGoodInfo = QGoodInfoEntity.goodInfoEntity;
        QGoodTypeEntity qGoodType = QGoodTypeEntity.goodTypeEntity;
        List<GoodDTO> result = jpaQueryFactory
                .select(
                        qGoodInfo.id,
                        qGoodInfo.price,
                        qGoodInfo.title,
                        qGoodInfo.unit,
                        qGoodType.name,
                        qGoodType.id
                )
                .from(qGoodInfo, qGoodType)
                .where(qGoodType.id.eq(qGoodInfo.typeId))
                .orderBy(qGoodInfo.order.desc())
                .fetch()
                .stream()
                .map(tuple -> { //  //转换集合内的数据
                    GoodDTO dto = new GoodDTO();
                    dto.setId(tuple.get(qGoodInfo.id));
                    dto.setPrice(tuple.get(qGoodInfo.price));
                    dto.setPrice(tuple.get(qGoodInfo.price));
                    dto.setTitle(tuple.get(qGoodInfo.title));
                    dto.setUnit(tuple.get(qGoodInfo.unit));
                    dto.setTypeId(tuple.get(qGoodType.id));
                    dto.setTypeName(tuple.get(qGoodType.name));
                    return dto;
                })
                .collect(Collectors.toList());
        logger.info(JsonUtil.bean2Json(result));
    }

    @Test
    public void testCount() {
        QStudentEntity qStudent = QStudentEntity.studentEntity;
        long count = jpaQueryFactory
                .select(qStudent.id.count())
                .from(qStudent)
                .fetchOne();
        logger.info("总数" + count);
    }

    @Test
    public void testSum() {
        QStudentEntity qStudent = QStudentEntity.studentEntity;
        double sum = jpaQueryFactory
                .select(qStudent.socre.sum())
                .from(qStudent)
                .fetchOne();
        logger.info("总和" + sum);
    }

    @Test
    public void testAvg() {
        QStudentEntity qStudent = QStudentEntity.studentEntity;
        double avg = jpaQueryFactory
                .select(qStudent.socre.avg())
                .from(qStudent)
                .fetchOne();
        logger.info("总和" + avg);
    }

    @Test
    public void testMax() {
        QStudentEntity qStudent = QStudentEntity.studentEntity;
        double max = jpaQueryFactory
                .select(qStudent.socre.max())
                .from(qStudent)
                .fetchOne();
        logger.info("总和" + max);
    }

    @Test
    public void testGroupBy() {
        QStudentEntity qStudent = QStudentEntity.studentEntity;
        double score = jpaQueryFactory
                .select(qStudent.socre)
                .from(qStudent)
                .groupBy(qStudent.socre)
                .having(qStudent.socre.gt(80))
                .fetchOne();
        logger.info("分数" + score);
    }

    @Test
    public void testFindVegetables() {
        QGoodInfoEntity qGoodInfo = QGoodInfoEntity.goodInfoEntity;
        QGoodTypeEntity qGoodType = QGoodTypeEntity.goodTypeEntity;
        List<GoodInfoEntity> result = jpaQueryFactory
                .selectFrom(qGoodInfo)
                .where(
                        qGoodInfo.typeId.in(
                                JPAExpressions.select(
                                        qGoodType.id
                                )
                                .from(qGoodType)
                                .where(qGoodType.name.like("%蔬菜%"))
                        )
                )
                .fetch();
        logger.info(JsonUtil.bean2Json(result));
    }

    @Test
    public void testFindMaxPrices() {
        QGoodInfoEntity qGoodInfo = QGoodInfoEntity.goodInfoEntity;
        List<GoodInfoEntity> result = jpaQueryFactory
                .selectFrom(qGoodInfo)
                .where(
                        qGoodInfo.price.eq(
                                JPAExpressions.select(
                                        qGoodInfo.price.max()
                                )
                                .from(qGoodInfo)
                        )
                )
                .fetch();
        logger.info(JsonUtil.bean2Json(result));
    }

    @Test
    public void testFindGTAvgPrices() {
        QGoodInfoEntity qGoodInfo = QGoodInfoEntity.goodInfoEntity;
        List<GoodInfoEntity> result = jpaQueryFactory
                .selectFrom(qGoodInfo)
                .where(
                        qGoodInfo.price.gt(
                                JPAExpressions.select(
                                        qGoodInfo.price.avg()
                                ).from(qGoodInfo)
                        )
                )
                .fetch();
        logger.info(JsonUtil.bean2Json(result));
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
