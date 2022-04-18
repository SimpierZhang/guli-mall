package com.zjw.common.constant;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-09 14:16
 * @Modifier:
 */
public class ProductConstant
{
    public enum AttrTypeEnum{
        ATTR_TYPE_BASE(1, "基本属性"),
        ATTR_TYPE_SALE(0, "销售属性");

        private Integer code;
        private String message;

        AttrTypeEnum(Integer code, String message){
            this.code = code;
            this.message = message;
        }

        AttrTypeEnum(){}

        public Integer getCode(){
            return this.code;
        }

        public String getMessage(){
            return this.message;
        }
    }

    public enum PublishStatusEnum{
        PUBLISH_STATUS_NEW(0, "新建"),
        PUBLISH_STATUS_UP(1, "已上架"),
        PUBLISH_STATUS_DOWN(2, "已下架");

        private Integer code;
        private String message;

        PublishStatusEnum(Integer code, String message){
            this.code = code;
            this.message = message;
        }

        PublishStatusEnum(){}

        public Integer getCode(){
            return this.code;
        }

        public String getMessage(){
            return this.message;
        }
    }
}
