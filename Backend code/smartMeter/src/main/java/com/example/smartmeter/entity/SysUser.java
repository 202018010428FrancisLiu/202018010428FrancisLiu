package com.example.smartmeter.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_user")
public class SysUser {

    private Long id;
    private String username;
    private String password;
    private Date lastLoginTime;
}
