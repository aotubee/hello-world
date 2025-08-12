package com.edc.erp.disfirstorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *铺货单明细入参
 * @author wuke
 */
@Data
public class OrdDisOrderFirstDetailIn extends Page {

    /** 主键id */
    @ApiModelProperty(name = "id", value = "主键id")
    private Long id;

    /** 配销铺货订单主键 */
    @ApiModelProperty(name = "firstOrderId", value = "配销铺货订单主键")
    private Long firstOrderId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 配销铺货数量 */
    @ApiModelProperty(name = "num", value = "配销铺货数量")
    private Integer num;

    /** 配销铺货金额 */
    @ApiModelProperty(name = "amount", value = "配销铺货金额")
    private BigDecimal amount;

    /** 创建者 */
    @ApiModelProperty(name = "creator", value = "创建者")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 修改者 */
    @ApiModelProperty(name = "updater", value = "修改者")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

}
