package com.edc.erp.common.model.out.appuser;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fxw
 * @description: app登录用户信息
 * @since 2022/10/17 17:54
 */
@Data
public class AppUserOut implements Serializable {
    private static final long serialVersionUID = 3278615614415557069L;
    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 代码
     */
    @ApiModelProperty(name = "code", value = "代码")
    @NotBlank(message = "账号代码不能为空")
    private String code;

    /**
     * 名称
     */
    @ApiModelProperty(name = "name", value = "名称")
    @NotBlank(message = "账号名称不能为空")
    private String name;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    @NotBlank(message = "门店代码不能为空")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 门店id
     */
    @ApiModelProperty(name = "storeId",value = "门店id")
    private Integer storeId;

    /**
     * 密码
     */
    @ApiModelProperty(name = "password", value = "密码")
    private String password;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /**
     * 是否启用，0否1是
     */
    @ApiModelProperty(name = "isEnable", value = "是否启用，0否1是")
    private Integer isEnable;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /**
     * 修改时间
     */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

    /**
     * 门店地址
     */
    @ApiModelProperty(name = "storeAddress", value = "门店地址")
    private String storeAddress;

    /**
     * 门店业态
     */
    @ApiModelProperty(name = "storeType", value = "门店业态")
    private String storeType;

    /**
     * 门店属性
     */
    @ApiModelProperty(name = "storeProperty", value = "门店属性")
    private String storeProperty;

    /**
     * 门店单元ID
     */
    @ApiModelProperty(name = "uniteId", value = "门店单元ID")
    private Integer unitId;

    /**
     * 单元代码
     */
    @ApiModelProperty(value = "单元代码")
    private String unitCode;

    /**
     * 单元名称
     */
    @ApiModelProperty(value = "单元名称")
    private String unitName;
}
