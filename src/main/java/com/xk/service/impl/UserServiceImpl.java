package com.xk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xk.common.ErrorCode;
import com.xk.exception.BusinessException;
import com.xk.model.User;
import com.xk.service.UserService;
import com.xk.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xk.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author jojiboy
* @description 针对表【user(用户)】的数据库操作Service实现
 * 用户服务实现类
*
* @createDate 2022-12-03 15:54:14
*/
@Service
@Slf4j//日志
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService{

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "YuPi"; //加盐加密

    /**
     * 用户注册
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param stuCode 学号
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String stuCode) {
        //1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, stuCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数含空值");
        }
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (stuCode.length() > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户学号过长");
        }
        //账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号包含特殊字符");
        }
        //用户密码和校验密码相同
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二次检验密码错误");
        }
        //账户不能重复
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("userAccount", userAccount);
        long count = userMapper.selectCount(qw);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号已存在");
        }
        //学号不能重复
        qw = new QueryWrapper<>();
        qw.eq("stuCode", stuCode);
        count = userMapper.selectCount(qw);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户学号已存在");
        }

        //2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));

        //3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setStuCode(stuCode);
        boolean saveResult = userMapper.insert(user) > 0;
        if (!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "信息注册失败");
        }
        return user.getId();
    }


    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.LOGIN_ERROR, "参数含空值");
        }
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.LOGIN_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8){
            throw new BusinessException(ErrorCode.LOGIN_ERROR, "用户密码过短");
        }
        //账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.LOGIN_ERROR, "用户账号包含特殊字符");
        }

        // 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        //2.查询用户是否存在
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("userAccount", userAccount);
        qw.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(qw);
        if (user == null){
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.LOGIN_ERROR, "用户不存在或密码错误");
        }

        //3.用户脱敏, 隐藏敏感信息, 防止数据库中的字段泄露
        User safetyUser = getSafetyUser(user);
        //4.记录用户的登录态, 存到服务端session中
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;

    }

    /**
     * 用户脱敏
     * @param originUser 原用戶信息
     * @return
     */
    public User getSafetyUser(User originUser){
        if (originUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "脱敏用户为空");
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setStuCode(originUser.getStuCode());
        return safetyUser;
    }


    /**
     * 用户注销
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




