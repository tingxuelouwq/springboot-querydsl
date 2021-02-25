package com.kevin.querydsl.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * kevin<br/>
 * 2021/2/24 16:25<br/>
 */
@Data
@Entity
@Table(name = "good_types")
public class GoodTypeEntity implements Serializable {
    //主键
    @Id
    @GeneratedValue
    @Column(name = "tgt_id")
    private Long id;
    //类型名称
    @Column(name = "tgt_name")
    private String name;
    //是否显示
    @Column(name = "tgt_is_show")
    private int isShow;
    //排序
    @Column(name = "tgt_order")
    private int order;
}
