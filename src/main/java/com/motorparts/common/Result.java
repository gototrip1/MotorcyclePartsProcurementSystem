package com.motorparts.common;

import com.motorparts.common.enums.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（有数据）
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return error(ResultCode.ERROR);
    }

    /**
     * 失败响应（指定错误码）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(ResultCode.ERROR.getCode())
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应（指定错误码和自定义消息）
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应（指定错误码和自定义消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 参数错误响应
     */
    public static <T> Result<T> paramError() {
        return error(ResultCode.PARAM_ERROR);
    }

    /**
     * 参数错误响应（自定义消息）
     */
    public static <T> Result<T> paramError(String message) {
        return error(ResultCode.PARAM_ERROR, message);
    }

    /**
     * 未授权响应
     */
    public static <T> Result<T> unauthorized() {
        return error(ResultCode.UNAUTHORIZED);
    }

    /**
     * 禁止访问响应
     */
    public static <T> Result<T> forbidden() {
        return error(ResultCode.FORBIDDEN);
    }

    /**
     * 资源不存在响应
     */
    public static <T> Result<T> notFound() {
        return error(ResultCode.NOT_FOUND);
    }

    /**
     * 服务器内部错误响应
     */
    public static <T> Result<T> serverError() {
        return error(ResultCode.SERVER_ERROR);
    }
}