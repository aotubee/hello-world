package com.edc.erp.disdeliveryorder.model.in;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 配销单入参
 * @since 2022/10/25 15:09
 */
@Data
@ApiModel(value = "OrdDisDeliveryIn",description = "配销单入参")
public class OrdDisDeliveryIn extends OrdDisDelivery implements Serializable {

    private static final long serialVersionUID = 4773554142447048530L;

    /**
     * 配销单明细列表
     */
    @ApiModelProperty(value = "配销单明细列表")
    private List<OrdDisDeliveryDetail> detailList;
    /**
     * 是否来源铺货单
     */
    private Integer isFirstOrderSource;

    private String auditType;
}
