package com.edc.erp.wholesale.model.in.returns;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 批发退-手动收货 入参
 * @author lx
 * @since 2023-02-07 14:32:44
 */
@Data
public class WholesaleRetReceivingIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 批发退货单ID集合 */
    @Size(min = 1,max = 20,message = "集合不能为空或集合不能超过20条")
    @ApiModelProperty(name = "wholesaleReturnIds",value = "批发退货单ID集合")
    private List<Long> wholesaleReturnIds;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode",value = "业务组织代码")
    private String bizOrgCode;
}
