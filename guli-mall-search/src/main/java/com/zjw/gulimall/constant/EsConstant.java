package com.zjw.gulimall.constant;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-15 17:36
 * @Modifier:
 */
public class EsConstant
{
    public static final int PAGE_SIZE = 6;

    public enum SaveIndexEnum{
        Index_TYPE_PRODUCT(0, "guli_product");

        private Integer code;
        private String message;

        SaveIndexEnum(Integer code, String message){
            this.code = code;
            this.message = message;
        }

        SaveIndexEnum(){}

        public Integer getCode(){
            return this.code;
        }

        public String getMessage(){
            return this.message;
        }
    }
}
