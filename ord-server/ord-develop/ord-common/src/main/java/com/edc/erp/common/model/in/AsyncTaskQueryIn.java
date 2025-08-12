package com.edc.erp.common.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @ClassName AsyncTaskQueryIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/3/2 10:32
 **/
@Data
public class AsyncTaskQueryIn implements Serializable {

    @ApiModelProperty(value = "业务类型")
    private String type;

    @ApiModelProperty(value = "ERP单号")
    private String businessNo;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(value = "是否查询异常状态")
    private Integer isError;
}
