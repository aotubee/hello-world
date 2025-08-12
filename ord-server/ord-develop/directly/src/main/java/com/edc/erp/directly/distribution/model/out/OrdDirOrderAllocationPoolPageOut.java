package com.edc.erp.directly.distribution.model.out;

import com.edc.erp.directly.distribution.entity.OrdDirOrderAllocationPool;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName OrdDirOrderAllocationPoolPageOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/1/7 18:34
 **/
@Data
public class OrdDirOrderAllocationPoolPageOut extends OrdDirOrderAllocationPool implements Serializable {
    private static final long serialVersionUID = -8961448265060714237L;

    @ApiModelProperty(name = "statusStr", value = "状态")
    private String statusStr;

    @ApiModelProperty(name = "businessQty", value = "库存")
    private BigDecimal businessQty;
}
