package com.edc.erp.disordercart.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;



/**
 * 配销购物车(OrdDisOrderCart)实体类
 *
 * @author fxw
 * @since 2022-10-17 18:58:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_order_cart")
@ApiModel(value = "OrdDisOrderCart", description = "配销购物车")
public class OrdDisOrderCart implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 购物车主键 */
    @ApiModelProperty(name = "id", value = "购物车主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 门店主键 */
    @ApiModelProperty(name = "storeId", value = "门店主键")
    private Integer storeId;

    /** 商品主键 */
    @ApiModelProperty(name = "goodsCode", value = "商品主键")
    private String goodsCode;

    /** 商品数量 */
    @ApiModelProperty(name = "quantity", value = "商品数量")
    private BigDecimal quantity;

    /** 活动主键 */
    @ApiModelProperty(name = "activityId", value = "活动主键")
    private Long activityId;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 修改人 */
    @ApiModelProperty(name = "updator", value = "修改人")
    private String updator;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

    /** 修改人 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

}
