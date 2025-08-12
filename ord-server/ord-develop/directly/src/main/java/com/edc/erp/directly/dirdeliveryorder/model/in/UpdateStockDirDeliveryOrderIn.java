package com.edc.erp.directly.dirdeliveryorder.model.in;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 配货单占库存审核更新入参
 *
 * @author Administrator
 */
@Data
public class UpdateStockDirDeliveryOrderIn implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "ordDirDelivery", value = "配货单")
    private OrdDirDelivery ordDirDelivery;

    @ApiModelProperty(name = "beforeDeliveryStatus", value = "配销单之前状态")
    private String beforeDeliveryStatus;

    @ApiModelProperty(name = "isStockOutAll", value = "是否整单缺货,0否1是")
    private Integer isStockOutAll;

    @ApiModelProperty(name = "stockOutLog", value = "商品缺货日志")
    private String stockOutLog;

    @ApiModelProperty(name = "dirDeliveryDetailList", value = "配货单明细")
    private List<OrdDirDeliveryDetail> dirDeliveryDetailList;
}
