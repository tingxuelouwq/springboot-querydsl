package com.kevin.querydsl.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * kevin<br/>
 * 2021/2/25 10:55<br/>
 */
@Data
@Entity
@Table(name = "t_student")
public class StudentEntity
{
    @Id
    @GeneratedValue
    @Column(name = "u_id")
    private Long id;
    @Column(name = "u_username")
    private String name;
    @Column(name = "u_age")
    private int age;
    @Column(name = "u_score")
    private double socre;
}
