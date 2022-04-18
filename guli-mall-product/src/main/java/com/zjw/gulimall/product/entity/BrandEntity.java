package com.zjw.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.zjw.gulimall.product.group.AddGroup;
import com.zjw.gulimall.product.group.ListValue;
import com.zjw.gulimall.product.group.UpdateGroup;
import com.zjw.gulimall.product.group.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 品牌
 * 
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 16:35:30
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@NotNull(message = "品牌id不能为空", groups = {UpdateGroup.class})
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空", groups = {AddGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "请输入正确的logo地址", groups = {AddGroup.class, UpdateGroup.class})
	@NotBlank(message = "logo地址不能为空", groups = {AddGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@ListValue(value = {0, 1}, message = "请输入0或1", groups = {AddGroup.class, UpdateGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	//如果JavaBean的属性中未指定group，那么对于采用@validated(group={AddGroup.class})的注解就不会校验
	@Pattern(regexp = "^[a-zA-z]$", groups = {AddGroup.class, UpdateGroup.class})
	@NotBlank(message = "请输入a-z或A-z的检索首字母", groups = {AddGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	private Integer sort;

}
