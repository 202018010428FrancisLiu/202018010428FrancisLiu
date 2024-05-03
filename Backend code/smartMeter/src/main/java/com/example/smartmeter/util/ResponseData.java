package com.example.smartmeter.util;
import com.example.smartmeter.entity.Constants;
import lombok.Data;

@Data
public class ResponseData {

    private int state;
    private Object data;
    private String message;

    public static ResponseData success() {
        ResponseData res = new ResponseData();
        res.setState(Constants.SUCCESS_CODE);
        return res;
    }

    public static ResponseData success(Object data) {
        ResponseData res = new ResponseData();
        res.setState(Constants.SUCCESS_CODE);
        res.setData(data);
        return res;
    }

    public static ResponseData error(String message) {
        ResponseData res = new ResponseData();
        res.setState(Constants.ERROR_CODE);
        res.setMessage(message);
        return res;
    }

    public static ResponseData error(String message, Object data) {
        ResponseData res = new ResponseData();
        res.setState(Constants.ERROR_CODE);
        res.setMessage(message);
        res.setData(data);
        return res;
    }
}
