package com.edc.erp.common.model.in.fund;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Created by Intellij IDEA.
 * User:  LZQ
 * Date:  2022/10/21
 * 对外提供接口查询入参
 * @author LZQ
 */
@Data
public class ForeignAccountFundIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主体类型 */
    @ApiModelProperty(name = "principalType", value = "主体类型")
    @NotBlank(message = "主体类型不能为空")
    private String principalType;

    /** 主体编码 */
    @ApiModelProperty(name = "principalCode", value = "主体编码")
    @NotBlank(message = "主体编码不能为空")
    private String principalCode;

    /** 业务组织 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织")
    @NotBlank(message = "业务组织不能为空")
    private String bizOrgCode;

}
