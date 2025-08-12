package com.edc.erp.directly.returnorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退货单分页入参对象
 *
 * @author yaojinpeng
 * @since 2022/10/24 16:29
 */
@Data
public class OrdReturnOrderPageIn extends Page implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 门店主键
     */
    @ApiModelProperty(name = "storeCode", value = "门店code")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 退货状态
     */
    @ApiModelProperty(name = "returnStatus", value = "退货状态")
    private String returnStatus;


    @ApiModelProperty(name = "returnOrderNo", value = "退货单号")
    private String returnOrderNo;


    @ApiModelProperty(name = "returnType", value = "退货类型")
    private String returnType;


    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String wrhCode;


    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    @ApiModelProperty(name = "createMonth", value = "创建时间")
    private String createTime;

    @ApiModelProperty(name = "isReversal", value = "是否红冲")
    private Integer isReversal;

    @ApiModelProperty(name = "isReversalOrder", value = "是否是红冲单")
    private Integer isReversalOrder;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "startTime", value = "开始时间")
    private String startTime;

    @ApiModelProperty(name = "endTime", value = "结束时间")
    private String endTime;

    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;

    /**退货单原因*/
    @ApiModelProperty(name = "returnOrderReason", value = "退货单原因")
    private String returnOrderReason;

    @ApiModelProperty(name = "createMonth", value = "创建月份(App查询使用)")
    private String createMonth;

    /** 物流单号 */
    @ApiModelProperty(name = "logisticsNo", value = "物流单号")
    private String logisticsNo;

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

    /** 品类代码集合 */
    @ApiModelProperty(name = "orgSorts", value = "品类代码集合")
    private List<String> orgSorts;

    /** 仓位代码集合 */
    @ApiModelProperty(name = "stockCodeList", value = "仓位代码集合")
    private List<String> stockCodeList;
}
