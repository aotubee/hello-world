package com.edc.erp.common.model.out.ucmanager;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 分货单列表创建人搜索条件出参
 * @author lx
 * @since 2022-11-23 14:48:54
 */
@Data
public class UserNameOut extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -4222436605040968934L;

    @ApiModelProperty(name = "realName",value = "真实名称")
    private String realName;

    @ApiModelProperty(name = "username",value = "用户名")
    private String username;

    @ApiModelProperty(name = "creator",value = "操作人")
    private String creator;
}
