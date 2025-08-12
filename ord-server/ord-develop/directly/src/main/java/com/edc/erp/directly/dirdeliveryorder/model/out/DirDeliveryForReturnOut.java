package com.edc.erp.directly.dirdeliveryorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @ClassName QueryDeliveryForReturnOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/7/24 16:49
 **/
@Data
public class DirDeliveryForReturnOut implements Serializable {
    private static final long serialVersionUID = 6552252089068172458L;

    @ApiModelProperty(name = "id", value = "主键")
    private Long id;

    @ApiModelProperty(name = "deliveryOrderNo", value = "配货单")
    private String deliveryOrderNo;

    @ApiModelProperty(name = "stockCode", value = "仓位")
    private String stockCode;

    @ApiModelProperty(name = "stockNameStr", value = "仓位中文值")
    private String stockNameStr;

    /** 收货日期 */
    @ApiModelProperty(name = "receiveTime", value = "收货日期")
    private LocalDateTime receiveTime;

    /** 收货日期 */
    @ApiModelProperty(name = "receiveTimeStr", value = "收货日期中文")
    private String receiveTimeStr;
}
