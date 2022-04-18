package com.zjw.common.constant;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-22 20:43
 * @Modifier:
 *  * Description：
 *  * 错误码和错误信息定义类
 *  * 1. 错误码定义规则为5为数字
 *  * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
 *  * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 *  * 错误码列表：
 *  * 10: 通用
 *  * 001：参数格式校验
 *  * 002: 短信验证码频率太高
 *  * 11: 商品
 *  * 12: 订单
 *  * 13: 购物车
 *  * 14: 物流
 *  * 15: 用户
 *  * 21: 库存
 */
public enum BizCodeEnum
{
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VAILD_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高,稍后再试"),
    TO_MANY_REQUEST(10003, "请求流量过大"),
    SMS_SEND_CODE_EXCEPTION(10403, "短信发送失败"),
    USER_EXIST_EXCEPTION(15001, "用户已经存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号已经存在"),
    USER_NOT_EXIST_EXCEPTION(15003, "用户不存在, 请先注册"),
    USER_NOT_LOGIN_EXCEPTION(15004, "用户未登录，请先登录"),
    LOGINACTT_PASSWORD_ERROR(15003, "账号或密码错误"),
    SOCIALUSER_LOGIN_ERROR(15004, "社交账号登录失败"),
    NOT_STOCK_EXCEPTION(21000, "商品库存不足"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    CART_CHECK_EXCEPTION(11001, "查询购物车异常"),
    SECKILL_LACK_STOCK_EXCEPTION(11002, "秒杀商品库存不足"),
    SECKILL_END_EXCEPTION(11003, "秒杀活动已经结束"),
    SECKILL_NOT_START_EXCEPTION(11004, "秒杀活动尚未开始，请等待"),
    SECKILL_ABILITY_SUCCESS(11005, "秒杀活动正在进行中，请快速抢购"),
    SECKILL_ALREADY_BUYED_LIMIT_EXCEPTION(11006, "你已经参与了该活动了"),
    SECKILL_BUYED_LIMIT_EXCEPTION(11007, "你要购买的数量已经超限制了");

    private int code;

    private String msg;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
