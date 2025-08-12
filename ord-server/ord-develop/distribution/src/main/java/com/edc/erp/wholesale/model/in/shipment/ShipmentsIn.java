package com.edc.erp.wholesale.model.in.shipment;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 批发出-手动发货 入参
 * @author lx
 * @since 2023-01-31 9:36:33
 */
@Data
public class ShipmentsIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 批发出货单ID集合 */
    @Size(min = 1,max = 20,message = "集合不能为空或集合不能超过20条")
    @ApiModelProperty(name = "wholesaleShipmentIds",value = "批发出货单ID集合")
    private List<Long> wholesaleShipmentIds;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode",value = "业务组织代码")
    private String bizOrgCode;
}
