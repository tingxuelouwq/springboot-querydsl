package com.kevin.querydsl.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * kevin<br/>
 * 2021/2/23 17:23<br/>
 */
@NoRepositoryBean
public interface BaseJPA<T>
        extends JpaRepository<T, Long>,
        QuerydslPredicateExecutor<T> {

}
