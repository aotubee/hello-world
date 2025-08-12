package com.edc.erp.common.model.out.ucmanager;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lx
 * @since 2022-11-23 15:00:46
 */
@Data
public class SysUserOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID 新的组织用
     */
    @ApiModelProperty(name = "id",value = "主键 新的组织用")
    private Integer id;

    /**
     * 钉钉用户ID
     */
    @ApiModelProperty(name = "dingTalkUserId",value = "钉钉用户ID")
    private String dingTalkUserId;

    /**
     * 职位ID
     */
    @ApiModelProperty(name = "posId",value = "职位ID")
    private Long posId;

    /**
     * 部门id
     */
    @ApiModelProperty(name = "deptId",value = "部门id")
    private Long deptId;

    /**
     * 用户名
     */
    @ApiModelProperty(name = "username",value = "用户名")
    private String username;

    /**
     * 昵称
     */
    @ApiModelProperty(name = "nickname",value = "昵称")
    private String nickname;
}
