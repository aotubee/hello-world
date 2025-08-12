package com.edc.erp.directly.dirrequestorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description
 *
 * @author :weichao
 */
@Data
public class DirRequestOrderPageIn extends Page {

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;

    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "orderTypeConfigId", value = "订单类型主键")
    private Long orderTypeConfigId;

    @ApiModelProperty(name = "beginTruncationTime", value = "截单开始时间")
    private String beginTruncationTime;

    @ApiModelProperty(name = "endTruncationTime", value = "截单结束时间")
    private String endTruncationTime;

    @ApiModelProperty(name = "totalAmount", value = "总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(name = "totalAmountType", value = "总金额类型0全部1-小于等于 2-大于等于")
    private Integer totalAmountType;

    @ApiModelProperty(name = "requestOrderNo", value = "要货单单号")
    private String requestOrderNo;

    @ApiModelProperty(name = "statusCode", value = "要货单状态")
    private String statusCode;

    @ApiModelProperty(name = "deliveryOrderNo", value = "配货单号")
    private String deliveryOrderNo;

    @ApiModelProperty(name = "orderNo", value = "订货单号")
    private String orderNo;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    private List<String> storeCodeList;

    @ApiModelProperty(name = "createMonth", value = "创建月份")
    private String createMonth;

    @ApiModelProperty(name = "orderTypeConfigIdList", value = "订单类型主键集合")
    private List<Long> orderTypeConfigIdList;
}
