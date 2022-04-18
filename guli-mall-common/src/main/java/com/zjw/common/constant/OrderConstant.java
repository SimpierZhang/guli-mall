package com.zjw.common.constant;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 23:27
 * @Modifier:
 */
public class OrderConstant
{
    public static final String ORDER_TOKEN_PREFIX = "order:token:";

    public enum OrderSubmitStatusEnum{
        ORDER_SUBMIT_SUCCESS(0, "订单提交成功"),
        ORDER_SUBMIT_FAIL_EXPIRED(1, "订单信息过期,请刷新在提交"),
        ORDER_SUBMIT_FAIL_PRICE_CHANGED(2, "订单商品价格发送变化,请确认后再次提交"),
        ORDER_SUBMIT_FAIL_LOCK_STOCK(3, "商品库存不足"),
        ORDER_SUBMIT_FAIL_DEFAULT_ERROR(4, "订单提交异常，请稍后重试");

        private int submitStatusCode;
        private String msg;

        OrderSubmitStatusEnum(int submitStatusCode, String msg) {
            this.submitStatusCode = submitStatusCode;
            this.msg = msg;
        }

        public int getSubmitStatusCode() {
            return submitStatusCode;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum OrderStatus{
        ORDER_STATUS_NEW(0, "新建订单"),
        ORDER_STATUS_PAYED(1, "订单已付款"),
        ORDER_STATUS_RECEIVED(2, "订单已收货"),
        ORDER_STATUS_COMMENTED(3, "订单已评价"),
        ORDER_STATUS_FINISHED(4, "订单已结束"),
        ORDER_STATUS_CANCELED(5, "订单已取消");
        private int statusCode;
        private String statusMsg;

        OrderStatus(int statusCode, String statusMsg) {
            this.statusCode = statusCode;
            this.statusMsg = statusMsg;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getStatusMsg() {
            return statusMsg;
        }
    }
}
