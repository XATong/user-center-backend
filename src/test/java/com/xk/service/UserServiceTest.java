package com.xk.service;
import java.util.Date;

import com.xk.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 用户服务测试
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void addUser(){
        User user = new User();
        user.setUsername("att");
        user.setUserAccount("1234");
        user.setAvatarUrl("https://cn.bing.com/images");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        user.setUserStatus(0);
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void userRegister(){
        String userAccount = "yupi";
        String userPassword = "";
        String checkPassword = "123456";
        String stuCode = "1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword, stuCode);
        //验证注册数据不能为空
        Assertions.assertEquals(-1, result); //断言

        userAccount = "yu";
        result = userService.userRegister(userAccount, userPassword, checkPassword, stuCode);
        //验证账号长度不小于4位
        Assertions.assertEquals(-1, result);

        userAccount = "yupi";
        userPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword, stuCode);
        //验证密码不小于8位
        Assertions.assertEquals(-1, result);

        userAccount = "yu pi";
        userPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword, stuCode);
        //验证账号不包含特殊字符
        Assertions.assertEquals(-1, result);

        checkPassword = "123456789";
        result = userService.userRegister(userAccount, userPassword, checkPassword, stuCode);
        //验证密码和二次密码相同
        Assertions.assertEquals(-1, result);

        userAccount = "1234";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword, stuCode);
        //验证账户不能重复
        Assertions.assertEquals(-1, result);

        userAccount = "yupi";
        result = userService.userRegister(userAccount, userPassword, checkPassword, stuCode);
        Assertions.assertTrue(result > 0);
    }
}