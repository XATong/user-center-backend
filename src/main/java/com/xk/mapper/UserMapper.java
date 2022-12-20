package com.xk.mapper;

import com.xk.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author jojiboy
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2022-12-03 15:54:14
* @Entity com.xk.model.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




