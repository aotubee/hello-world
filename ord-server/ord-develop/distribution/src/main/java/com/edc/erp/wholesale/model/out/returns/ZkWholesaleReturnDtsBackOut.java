package com.edc.erp.wholesale.model.out.returns;

import com.edc.erp.wholesale.model.out.ZkWholesaleDtsBackBaseOut;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName ZkWholesaleReturnDtsBackOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/12 10:48
 **/
@Data
public class ZkWholesaleReturnDtsBackOut extends ZkWholesaleDtsBackBaseOut implements Serializable {

    private static final long serialVersionUID = 9169957464536720438L;

    @ApiModelProperty(name = "wholesaleReturnsNo", value = "批发退单号")
    private String wholesaleReturnsNo;

}
