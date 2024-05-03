package com.example.smartmeter.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("history")
public class History {

    private Long id;
    private BigDecimal current;
    private BigDecimal voltage;
    private BigDecimal effectivePower;
    private Date addTime;

}
