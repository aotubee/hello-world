
package com.edc.erp.distribution.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: 订货单分页查询入参
 * @author fxw
 * @since 2022/10/17
 */
@Data
public class OrderIn extends Page {

    @ApiModelProperty(name = "id", value = "主键")
    private Integer id;

    @ApiModelProperty(name = "orderCycleId", value = "订货周期主键")
    private Integer orderCycleId;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "orderNo", value = "订货单号")
    private String orderNo;

    @ApiModelProperty(name = "orderStatusCode", value = "订货单状态")
    private String orderStatusCode;

    @ApiModelProperty(name = "orderAmount", value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(name = "payableAmount", value = "应付金额")
    private BigDecimal payableAmount;

    @ApiModelProperty(name = "preferentialAmount", value = "优惠金额")
    private BigDecimal preferentialAmount;

    @ApiModelProperty(name = "orderTypeCode", value = "订单类型")
    private String orderTypeCode;

    @ApiModelProperty(name = "sourceCode", value = "订货类型")
    private String sourceCode;

    @ApiModelProperty(name = "releaseFreezeAmountTime", value = "释放订单金额时间")
    private LocalDateTime releaseFreezeAmountTime;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

    /**
     * 提交开始时间
     */
    @ApiModelProperty(name = "beginTime", value = "提交开始时间")
    private String beginTime;

    /**
     * 提交结束时间
     */
    @ApiModelProperty(name = "endTime", value = "提交结束时间")
    private String endTime;

    /**
     * 接单日期开始
     */
    @ApiModelProperty(name = "beginAcceptDate", value = "接单日期开始")
    private String beginAcceptDate;

    /**
     * 接单日期结束
     */
    @ApiModelProperty(name = "endAcceptDate", value = "接单日期结束")
    private String endAcceptDate;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 门店区域
     */
    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;

    /**
     * 门店erp代码
     */
    private List<String> storeCodeList;

    @ApiModelProperty(name = "requestOrderNo", value = "集货单号")
    private String requestOrderNo;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "totalPayAmountOperator", value = "实付，1-小于等于 2-大于等于")
    private Integer totalPayAmountOperator;

    @ApiModelProperty(name = "orderTypeCodeList", value = "订单类型集合")
    private List<String> orderTypeCodeList;

    @ApiModelProperty(name = "distributionOrderNo", value = "分货单号")
    private String distributionOrderNo;

    @ApiModelProperty(name = "orderIdentification", value = "分货标识")
    private String orderIdentification;
}
