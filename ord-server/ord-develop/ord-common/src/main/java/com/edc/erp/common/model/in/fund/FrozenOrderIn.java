package com.edc.erp.common.model.in.fund;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName FrozenOrderIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/7/20 8:47
 **/
@Data
public class FrozenOrderIn implements Serializable {
    private static final long serialVersionUID = -7271603975762338629L;

    @ApiModelProperty(name = "businessNo", value = "业务单号")
    private String businessNo;

    @ApiModelProperty(name = "amount", value = "冻结金额")
    private BigDecimal amount;

    @ApiModelProperty(name = "businessType", value = "业务类型")
    private String businessType;
}
