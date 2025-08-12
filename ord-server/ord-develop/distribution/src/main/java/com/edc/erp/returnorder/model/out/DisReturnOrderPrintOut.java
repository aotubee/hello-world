package com.edc.erp.returnorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * description 退货单打印出参
 *
 * @author gusiyuan
 * @since 2023/10/9 18:41
 */
@Data
public class DisReturnOrderPrintOut implements Serializable {

    private static final long serialVersionUID = 5518930420595767646L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    private Integer id;

    /**
     * 退货单号
     */
    @ApiModelProperty(name = "returnOrderNo", value = "退货单号")
    private String returnOrderNo;

    /**
     * 退货状态
     */
    @ApiModelProperty(name = "returnStatus", value = "退货状态")
    private String returnStatus;

    /**
     * 退货状态中文
     */
    @ApiModelProperty(name = "returnStatusName", value = "退货状态中文")
    private String returnStatusName;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 仓储代码
     */
    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String wrhCode;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 仓位名称
     */
    @ApiModelProperty(name = "stockName", value = "仓位名称")
    private String stockName;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 申请退货数量
     */
    @ApiModelProperty(name = "applyReturnQuantity", value = "申请退货数量")
    private BigDecimal applyReturnQuantity;

    /**
     * 申请退货包装数量
     */
    @ApiModelProperty(name = "applyReturnQuantity", value = "申请退货包装数量")
    private BigDecimal applyReturnPackQuantity;

    /**
     * 申请退货金额
     */
    @ApiModelProperty(name = "applyReturnAmount", value = "申请退货金额")
    private BigDecimal applyReturnAmount;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /**
     * 退货单原因
     */
    @ApiModelProperty(name = "returnOrderReason", value = "退货单原因")
    private String returnOrderReason;

    /**
     * 收货时间
     */
    @ApiModelProperty(name = "receiveTime", value = "收货时间")
    private LocalDateTime receiveTime;

    @ApiModelProperty(name = "dtlPrintOuts", value = "退货单明细")
    private List<DisReturnOrderDtlPrintOut> dtlPrintOuts;
}
