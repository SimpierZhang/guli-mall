package com.zjw.common.exception;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-27 23:36
 * @Modifier:
 */
public class StockLackException extends RuntimeException
{
    public StockLackException(){
        super("库存不足");
    }

    public StockLackException(String msg){
        super(msg);
    }
}
