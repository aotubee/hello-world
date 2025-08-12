package com.edc.erp.directly.returnorder.model.out;

import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 基础退货单出参对象
 *
 * @author yaojinpeng
 * @since 2022/10/24 16:30
 */
@Data
public class BaseReturnOrderOut extends OrdDirReturn {


    /**
     * 退货类型中文值
     */
    @ApiModelProperty(name = "returnTypeValue", value = "退货类型中文值")
    private String returnTypeValue;



    /**
     * 退货状态中文值
     */
    @ApiModelProperty(name = "returnStatusValue", value = "退货状态中文值")
    private String returnStatusValue;

    /**
     * 退货通知单单号
     */
    @ApiModelProperty(name = "returnNoticeNo", value = "退货通知单单号")
    private String returnNoticeNo;

    /**
     * 仓储名称
     */
    @ApiModelProperty(name = "wrhName", value = "仓储名称")
    private String wrhName;

    /**
     * 仓位名称
     */
    @ApiModelProperty(name = "stockName", value = "仓位名称")
    private String stockName;

    /**
     * 退货单原因中文值
     */
    @ApiModelProperty(name = "returnOrderReasonValue", value = "退货单原因中文值")
    private String returnOrderReasonValue;

    /**
     * 区域名称
     */
    @ApiModelProperty(name = "storeAreaName", value = "区域名称")
    private String storeAreaName;

    /**
     * 区域名称
     */
    @ApiModelProperty(name = "applySkuCount", value = "区域名称")
    private Integer applySkuCount;

}
