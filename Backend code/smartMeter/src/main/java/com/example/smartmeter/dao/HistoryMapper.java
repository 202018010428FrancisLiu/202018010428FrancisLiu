package com.example.smartmeter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartmeter.entity.History;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HistoryMapper extends BaseMapper<History> {
}
