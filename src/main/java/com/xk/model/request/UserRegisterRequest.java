package com.xk.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author xk
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 7563253326333316345L;

    //用户账号
    private String userAccount;

    //用户密码
    private String userPassword;

    //二次校验密码
    private String checkPassword;

    //学号
    private String stuCode;

}
