package com.xk.service;

import com.xk.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author jojiboy
* @description 针对表【user(用户)】的数据库操作Service
 * 用户服务
*
* @createDate 2022-12-03 15:54:14
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param stuCode 学号
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String stuCode);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 返回脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销,退出登录
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);
}
