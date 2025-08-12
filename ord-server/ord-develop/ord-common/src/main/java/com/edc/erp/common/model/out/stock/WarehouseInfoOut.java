package com.edc.erp.common.model.out.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fxw
 * @description: 仓储出参信息
 * @since 2022/11/17 17:03
 */
@Data
public class WarehouseInfoOut implements Serializable {
    private static final long serialVersionUID = 6275256115938239763L;

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "仓储代码")
    private String warehouseCode;

    @ApiModelProperty(value = "仓储名称")
    private String warehouseName;

    @ApiModelProperty(value = "是否启用")
    private Integer isEnable;

    @ApiModelProperty(value = "联系人")
    private String contact;

    @ApiModelProperty(value = "联系电话")
    private String contactPhone;

    @ApiModelProperty(value = "地址")
    private String address;

    @ApiModelProperty(value = "物流商ID")
    private Integer logcId;

    @ApiModelProperty(value = "配送中心ID")
    private Integer dcId;

    @ApiModelProperty(value = "公司代码")
    private String orgCode;

    @ApiModelProperty(value = "业务组织")
    private String bizOrgCode;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建人")
    private String creator;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改人")
    private String updater;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;

    @ApiModelProperty(value = "物流商代码")
    private String logisticsProviderCode;

    @ApiModelProperty(value = "物流商名称")
    private String logisticsProviderName;

    @ApiModelProperty(value = "配送中心代码")
    private String dcCode;

    @ApiModelProperty(value = "配送中心名称")
    private String dcName;
}
