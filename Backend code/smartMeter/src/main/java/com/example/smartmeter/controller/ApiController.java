package com.example.smartmeter.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.smartmeter.dao.HistoryMapper;
import com.example.smartmeter.dao.SysUserMapper;
import com.example.smartmeter.entity.History;
import com.example.smartmeter.entity.HistoryVo;
import com.example.smartmeter.entity.SysUser;
import com.example.smartmeter.util.ResponseData;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("api")
@Log4j2
public class ApiController {

    @Autowired
    SysUserMapper userMapper;

    @Autowired
    HistoryMapper historyMapper;

    @Autowired
    IndexWebSocket webSocket;

    @RequestMapping("login")
    public ResponseData login(@RequestBody SysUser user) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        SysUser sysUser = userMapper.selectOne(wrapper);
        if (sysUser == null) {
            return ResponseData.error("User does not exist");
        }
        String password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        if (!password.equals(sysUser.getPassword())) {
            return ResponseData.error("Username or password error");
        }
        sysUser.setLastLoginTime(new Date());
        userMapper.updateById(sysUser);
        return ResponseData.success(sysUser);
    }

    @RequestMapping("register")
    public ResponseData register(@RequestBody SysUser user) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        SysUser sysUser = userMapper.selectOne(wrapper);
        if (sysUser != null) {
            return ResponseData.error("User already exists");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setLastLoginTime(new Date());
        userMapper.insert(user);
        return ResponseData.success(user);
    }

    @RequestMapping("updateUser")
    public ResponseData updateUser(@RequestBody SysUser user) {
        SysUser sysUser = userMapper.selectById(user.getId());
        sysUser.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        userMapper.updateById(sysUser);
        return ResponseData.success();
    }

    @RequestMapping(value = "check", method = {RequestMethod.GET, RequestMethod.POST})
    public Object check(@RequestParam(required = false) String msg, HttpServletRequest request) {
        if (request.getMethod().equalsIgnoreCase("GET")) {
            log.info("GET request: " + msg);
            return msg;
        }
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            JSONObject postMsg = JSONObject.parseObject(requestBody.toString());
            History history = new History();
            JSONObject json = postMsg.getJSONObject("msg");
            JSONObject data = json.getJSONObject("data").getJSONObject("params");
            history.setCurrent(new BigDecimal(data.getJSONObject("Active_Current").getString("value")));
            history.setVoltage(new BigDecimal(data.getJSONObject("Active_Volt").getString("value")));
            history.setEffectivePower(new BigDecimal(data.getJSONObject("Active_Power").getString("value")));
            history.setAddTime(new Date());
            log.info(history.toString());
            historyMapper.insert(history);
            JSONObject his = (JSONObject) JSONObject.toJSON(history);
            webSocket.onMessage(his.toJSONString());
            return json.toJSONString();
        } catch (Exception e) {
            log.error("Error reading request body", e);
            return ResponseData.success();
        }
    }

    @RequestMapping(value = "insertHistory")
    public ResponseData insertHistory(@RequestBody String data) {
        log.info("insertHistory:" + data);
        return ResponseData.success();
    }

    @RequestMapping("getHistory")
    public ResponseData getHistory(@RequestBody HistoryVo vo) {
        LambdaQueryWrapper<History> wrapper = Wrappers.lambdaQuery();
        if (!StringUtils.isEmpty(vo.getStartTime())) {
            wrapper.ge(History::getAddTime, vo.getStartTime());
        }
        if (!StringUtils.isEmpty(vo.getEndTime())) {
            wrapper.le(History::getAddTime, vo.getEndTime());
        }
        List<History> historyList = historyMapper.selectList(wrapper);
        long startTime = CollectionUtils.isEmpty(historyList) ? 0 : historyList.get(0).getAddTime().getTime();
        long endTime = CollectionUtils.isEmpty(historyList) ? 0 : historyList.get(historyList.size() - 1).getAddTime().getTime();
        long time = (endTime - startTime) / 1000;
        BigDecimal powerFactor = new BigDecimal(0);       // 功率因素
        BigDecimal apparentPower = new BigDecimal(0);     // 视在功率，单位：W
        BigDecimal electricityConsumed = new BigDecimal(0); // 用电量，单位：kWh
        BigDecimal voltage = new BigDecimal(0);
        BigDecimal current = new BigDecimal(0);
        BigDecimal effectivePower = new BigDecimal(0);
        for (History history : historyList) {
            apparentPower = apparentPower.add(history.getEffectivePower()); // 单位：W
            voltage = voltage.add(history.getVoltage());
            current = current.add(history.getCurrent());
            effectivePower = effectivePower.add(history.getEffectivePower());
        }
        voltage = voltage.divide(new BigDecimal(historyList.size()), 2, RoundingMode.HALF_UP);
        current = current.divide(new BigDecimal(historyList.size()), 2, RoundingMode.HALF_UP);
        effectivePower = effectivePower.divide(new BigDecimal(historyList.size()), 2, RoundingMode.HALF_UP);
        electricityConsumed = effectivePower.multiply(new BigDecimal(time).divide(BigDecimal.valueOf(60 * 60.0), 2, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);// 单位：kWh，

        // 计算功率因素
        powerFactor = new BigDecimal(9).divide(apparentPower, 2, RoundingMode.HALF_UP);
        JSONObject data = new JSONObject();
        data.put("powerFactor", powerFactor);
        data.put("apparentPower", apparentPower);
        data.put("electricityConsumed", electricityConsumed);
        data.put("time", time);
        data.put("voltage", voltage);
        data.put("current", current);
        data.put("effectivePower", effectivePower);
        return ResponseData.success(data);
    }
}
