package com.zjw.common.utils;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-15 17:52
 * @Modifier:
 */
public class ErrorConstant
{
    public enum ProductErrorEnum
    {
        PRODUCT_ERROR_UPLOAD_ENUM(10200, "商品信息上架到es异常"),
        PRODUCT_ERROR_VALID_ENUM(10100, "参数校验异常"),
        PRODUCT_ERROR_DEFAULT_ENUM(10000, "默认异常");


        private Integer code;
        private String message;

        ProductErrorEnum(Integer code, String message){
            this.code = code;
            this.message = message;
        }

        ProductErrorEnum(){}

        public Integer getCode(){
            return this.code;
        }

        public String getMessage(){
            return this.message;
        }
        }
}
