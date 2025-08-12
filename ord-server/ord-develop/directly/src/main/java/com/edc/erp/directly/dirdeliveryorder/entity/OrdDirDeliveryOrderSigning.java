package com.edc.erp.directly.dirdeliveryorder.entity;


import java.math.BigDecimal;
import java.util.Date;
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
import java.time.LocalDateTime;



/**
 * 配货单签收(OrdDirDeliveryOrderSigning)实体类
 *
 * @author weichao
 * @since 2022-11-21 11:15:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_delivery_order_signing")
@ApiModel(value = "OrdDirDeliveryOrderSigning", description = "配货单签收")
public class OrdDirDeliveryOrderSigning implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 门店代码 */
    @ApiModelProperty(name = "erpStoreCode", value = "门店代码")
    private String erpStoreCode;

    /** 配货单主键 */
    @ApiModelProperty(name = "dirDeliveryOrderId", value = "配货单主键")
    private Long dirDeliveryOrderId;

    /** 出货整箱件数 */
    @ApiModelProperty(name = "shipmentWholePackageQuantity", value = "出货整箱件数")
    private BigDecimal shipmentWholePackageQuantity;

    /** 出货纸箱数 */
    @ApiModelProperty(name = "shipmentCartonsQuantity", value = "出货纸箱数")
    private BigDecimal shipmentCartonsQuantity;

    /** 出货物流箱数 */
    @ApiModelProperty(name = "shipmentLogisticsBoxQuantity", value = "出货物流箱数")
    private BigDecimal shipmentLogisticsBoxQuantity;

    /** 整箱件数 */
    @ApiModelProperty(name = "wholePackageQuantity", value = "整箱件数")
    private BigDecimal wholePackageQuantity;

    /** 纸箱数 */
    @ApiModelProperty(name = "cartonsQuantity", value = "纸箱数")
    private BigDecimal cartonsQuantity;

    /** 物流箱数 */
    @ApiModelProperty(name = "logisticsBoxQuantity", value = "物流箱数")
    private BigDecimal logisticsBoxQuantity;

    /** 差异备注 */
    @ApiModelProperty(name = "differencesRemark", value = "差异备注")
    private String differencesRemark;

    /** 差异类型 */
    @ApiModelProperty(name = "differencesType", value = "差异类型")
    private Integer differencesType;

    /** 门店签名 */
    @ApiModelProperty(name = "signature", value = "门店签名")
    private String signature;

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
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

}
