package com.xk.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xk.common.BaseResponse;
import com.xk.common.ErrorCode;
import com.xk.common.ResultUtils;
import com.xk.exception.BusinessException;
import com.xk.model.User;
import com.xk.model.request.UserLoginRequest;
import com.xk.model.request.UserRegisterRequest;
import com.xk.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.xk.constant.UserConstant.ADMIN_ROLE;
import static com.xk.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口服务
 *
 * @author xk
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param urr 用户注册请求体
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest urr){
        if (urr == null){
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数错误");
        }
        String userAccount = urr.getUserAccount();
        String userPassword = urr.getUserPassword();
        String checkPassword = urr.getCheckPassword();
        String stuCode = urr.getStuCode();
        //判断是否为空
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword, stuCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数含空");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, stuCode);
        return ResultUtils.success(result);
    }


    /**
     * 用户登录
     * @param ulr 用户登录请求体
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest ulr, HttpServletRequest request){
        if (ulr == null){
            throw new BusinessException(ErrorCode.LOGIN_ERROR, "请求参数错误");
        }
        String userAccount = ulr.getUserAccount();
        String userPassword = ulr.getUserPassword();
        if (StringUtils.isAllBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.LOGIN_ERROR, "请求参数含空");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }


    /**
     * 用户注销,退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if (request == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求错误");
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    /**
     * 查询所有用户数据
     * @param username
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request){
        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH, "无管理员权限");
        }
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)){
            qw.like("username", username);
        }
        List<User> userList = userService.list(qw);
        List<User> users = userList.stream().map(user -> {
            return userService.getSafetyUser(user);
        }).collect(Collectors.toList());
        return ResultUtils.success(users);
    }


    /**
     * 删除用户数据
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request){
        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH, "无管理员权限");
        }
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id错误");
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }


    /**
     * 获取当前用户信息
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObject;
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }
        long userId = currentUser.getId();
        // TODO 检验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }


    /**
     * 权限判断
     *   某用户登录成功，则该用户的脱敏用户信息则保存到服务端的session中，此session只匹配当前用户的请求，并返回给前端一个设置 cookie 的 ”命令“
     *   session => cookie
     *   前端接收到后端的命令后，设置 cookie，保存到浏览器内
     *   当该用户调用查询接口时，前端再次请求后端的时候（相同的域名），在请求头中带上cookie去请求
     *   服务器端则通过该用户的请求中cookie携带的JSESSIONID来匹配服务器中的session
     *   若匹配上，则通过session中的 用户登录态USER_LOGIN_STATE 再获取到脱敏用户数据
     *   最终根据脱敏用户对象（用户的登录信息、登录名等）来判断该用户的权限是否为管理员 (safetyUser.getUserRole())
     */
    private boolean isAdmin(HttpServletRequest request){
        //仅管理员可查询以及删除
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

}
