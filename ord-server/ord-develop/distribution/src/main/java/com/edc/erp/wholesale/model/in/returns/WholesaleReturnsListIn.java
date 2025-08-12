package com.edc.erp.wholesale.model.in.returns;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author wld
 * @description:批发退货单列表入参
 * @since 2022/10/26 18:13
 */
@Data
public class WholesaleReturnsListIn extends Page implements Serializable {

    /**
     * 批发退货单单号
     */
    @ApiModelProperty(name = "wholesaleReturnNo", value = "批发退货单单号")
    private String wholesaleReturnNo;

    /**
     * 退货单状态
     */
    @ApiModelProperty(name = "returnStatus", value = "退货单状态")
    private String returnStatus;

    /**
     * 客户代码
     */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /**
     * 入库仓储
     */
    @ApiModelProperty(name = "storageWrh", value = "入库仓储")
    private String storageWrh;

    /**
     * 入库仓位
     */
    @ApiModelProperty(name = "storageStockCode", value = "入库仓位")
    private String storageStockCode;

    /**
     * 是否红冲单
     */
    @ApiModelProperty(name = "isReversalOrder", value = "是否红冲单")
    private Integer isReversalOrder;

    /**
     * 是否被红冲
     */
    @ApiModelProperty(name = "isReversal", value = "是否被红冲")
    private Integer isReversal;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "startCreateTime", value = "开始创建时间")
    private LocalDateTime startCreateTime;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "endCreateTime", value = "最后创建时间")
    private LocalDateTime endCreateTime;

    /**
     * 是否删除(0：否，1：是；默认0)
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 收货开始时间
     */
    @ApiModelProperty(name = "receiveTimeBegin", value = "收货开始时间")
    private LocalDateTime receiveTimeBegin;

    /**
     * 收货结束时间
     */
    @ApiModelProperty(name = "receiveTimeEnd", value = "收货结束时间")
    private LocalDateTime receiveTimeEnd;

    @ApiModelProperty(name = "sourceNo", value = "来源单号")
    private String sourceNo;

    @ApiModelProperty(name = "trackingNo", value = "物流单号")
    private String trackingNo;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;
}
