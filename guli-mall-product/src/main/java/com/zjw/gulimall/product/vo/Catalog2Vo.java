package com.zjw.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-16 23:02
 * @Modifier:
 */
@Data
public class Catalog2Vo
{
    private Long catalog1Id;
    private Long id;
    private String name;
    private List<Catalog3Vo> catalog3List;

    @Data
    public static class Catalog3Vo{
        private Long catalog2Id;
        private Long id;
        private String name;
    }
}
