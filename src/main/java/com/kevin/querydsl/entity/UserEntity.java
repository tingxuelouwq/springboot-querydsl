package com.kevin.querydsl.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * kevin<br/>
 * 2021/2/23 17:17<br/>
 */
@Data
@Entity
@Table(name = "t_user")
public class UserEntity implements Serializable {
    @Id
    @Column(name = "t_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "t_name")
    private String name;
    @Column(name = "t_age")
    private int age;
    @Column(name = "t_address")
    private String address;
    @Column(name = "t_pwd")
    private String pwd;
}
