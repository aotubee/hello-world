package com.edc.erp.directly.dirdeliveryorder.model.in;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 配货单入参
 * @since 2022/10/25 15:09
 */
@Data
@ApiModel(value = "OrdDirDeliveryIn",description = "配货单入参")
public class OrdDirDeliveryIn extends OrdDirDelivery implements Serializable {

    private static final long serialVersionUID = 4773554142447048530L;

    /**
     * 配货单明细列表
     */
    @ApiModelProperty(value = "配货单明细列表")
    private List<OrdDirDeliveryDetail> detailList;

    /**
     * 是否来源铺货单
     */
    private Integer isFirstOrderSource;


    @ApiModelProperty(name = "auditType", value = "审核类型")
    private String auditType;
}
