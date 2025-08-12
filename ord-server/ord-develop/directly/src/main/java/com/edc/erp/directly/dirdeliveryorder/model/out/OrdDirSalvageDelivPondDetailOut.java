package com.edc.erp.directly.dirdeliveryorder.model.out;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirSalvageDelivPondDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrdDisSalvageDelivPondDetailOut",description = "捞单明细扩展类")
public class OrdDirSalvageDelivPondDetailOut extends OrdDirSalvageDelivPondDetail {

    @ApiModelProperty(name = "salesPriority", value = "捞单销售优先级")
    private Integer salesPriority;

    @ApiModelProperty(name = "centerStockBizOrgCode", value = "中心仓业务组织代码")
    private String centerStockBizOrgCode;

    @ApiModelProperty(name = "centerStockOrgCode", value = "中心仓组织代码")
    private String centerStockOrgCode;
}
