package com.kevin.querydsl.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * kevin<br/>
 * 2021/2/24 17:05<br/>
 */
@Data
@Entity
@Table(name = "good_infos")
public class GoodInfoEntity implements Serializable {
    //主键
    @Id
    @GeneratedValue
    @Column(name = "tg_id")
    private Long id;
    //商品标题
    @Column(name = "tg_title")
    private String title;
    //商品价格
    @Column(name = "tg_price")
    private double price;
    //商品单位
    @Column(name = "tg_unit")
    private String unit;
    //商品排序
    @Column(name = "tg_order")
    private int order;
    //类型外键
    @Column(name = "tg_type_id")
    private Long typeId;
}
