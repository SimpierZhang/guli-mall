package com.zjw.common.constant;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-12 18:11
 * @Modifier:
 */
public class WareConstant
{
    //采购需求状态
    public enum PurchaseDetailStatusEnum
    {
        PURCHASE_STATUS_NEW(0, "新建"),
        PURCHASE_STATUS_ASSIGN(1, "已分配"),
        PURCHASE_STATUS_BUYING(2, "采购中"),
        PURCHASE_STATUS_FINISHED(3, "已完成"),
        PURCHASE_STATUS_FAIL(4, "采购失败");


        private int code;
        private String message;

        private PurchaseDetailStatusEnum(){

        }

        private PurchaseDetailStatusEnum(int code, String message){
            this.code = code;
            this.message = message;
        }

        public int getCode(){
            return this.code;
        }
    }

    //采购单状态
    public enum PurchaseStatusEnum
    {
        PURCHASE_STATUS_NEW(0, "新建"),
        PURCHASE_STATUS_ASSIGN(1, "已分配"),
        PURCHASE_STATUS_RECEIVED(2, "已领取"),
        PURCHASE_STATUS_FINISHED(3, "已完成"),
        PURCHASE_STATUS_FAIL(4, "有异常");


        private int code;
        private String message;

        private PurchaseStatusEnum(){

        }

        private PurchaseStatusEnum(int code, String message){
            this.code = code;
            this.message = message;
        }

        public int getCode(){
            return this.code;
        }
    }

    public enum WareOrderTaskDetailStatusEnum
    {
        TASK_DETAIL_STATUS_UNLOCKED(0, "库存未锁定"),
        TASK_DETAIL_STATUS_LOCKED(1, "库存已锁定"),
        TASK_DETAIL_STATUS_SALE(2, "库存已售出");

        private int code;
        private String message;

        private WareOrderTaskDetailStatusEnum(){

        }

        private WareOrderTaskDetailStatusEnum(int code, String message){
            this.code = code;
            this.message = message;
        }

        public int getCode(){
            return this.code;
        }

        public String getMessage() {
            return message;
        }
    }
}
