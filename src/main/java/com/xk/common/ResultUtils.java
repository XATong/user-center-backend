package com.xk.common;

/**
 * 返回工具类
 */
public class ResultUtils {

    /**
     * 成功
     * @param data 返回到前端的对象数据
     * @param <T> 泛型
     * @return
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     * @param errorCode 错误码对象
     * @param message 状态码信息
     * @param description 状态码描述(详情)
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String message ,String description){
        return new BaseResponse(errorCode.getCode(),null, message, description);
    }

    /**
     * 失败
     * @param code 状态码
     * @param message 状态码信息
     * @param description 状态码描述
     * @return
     */
    public static BaseResponse error(int code, String message ,String description){
        return new BaseResponse(code,null, message, description);
    }


}
