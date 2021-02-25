package com.kevin.querydsl.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * kevin<br/>
 * 2021/2/25 10:25<br/>
 */
@Data
public class GoodDTO implements Serializable {
    //主键
    private Long id;
    //标题
    private String title;
    //单位
    private String unit;
    //价格
    private double price;
    //类型名称
    private String typeName;
    //类型编号
    private Long typeId;
}
