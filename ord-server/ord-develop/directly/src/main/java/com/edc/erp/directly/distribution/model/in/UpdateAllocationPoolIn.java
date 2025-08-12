package com.edc.erp.directly.distribution.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName UpdateAllocationPoolIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/1/7 17:56
 **/
@Data
public class UpdateAllocationPoolIn implements Serializable {
    private static final long serialVersionUID = -8207952290786489508L;

    @ApiModelProperty(name = "id", value = "主键")
    @NotEmpty
    private Long id;

    @ApiModelProperty(name = "supplementQuantity", value = "补单量")
    @NotEmpty
    private BigDecimal supplementQuantity;

    @ApiModelProperty(name = "remark", value = "差异说明")
    private String remark;
}
