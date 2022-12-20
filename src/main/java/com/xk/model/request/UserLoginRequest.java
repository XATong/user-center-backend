package com.xk.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 * @author xk
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 1938844121099568039L;

    //用户账号
    private String userAccount;

    //用户密码
    private String userPassword;

}
