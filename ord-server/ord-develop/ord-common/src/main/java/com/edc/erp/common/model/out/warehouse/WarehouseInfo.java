package com.edc.erp.common.model.out.warehouse;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 仓储表
 * </p>
 *
 * @author gusiyuan
 * @since 2022-06-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "logc_warehouse_info")
@ApiModel(value="WarehouseInfo对象", description="仓储表")
public class WarehouseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ApiModelProperty(value = "仓储代码")
    @NotEmpty(message = "仓储代码不能为空")
    private String warehouseCode;

    @ApiModelProperty(value = "仓储名称")
    @NotEmpty(message = "仓储名称不能为空")
    private String warehouseName;

    @ApiModelProperty(value = "是否启用")
    private Integer isEnable;

    @ApiModelProperty(value = "联系人")
    @NotEmpty(message = "联系人不能为空")
    private String contact;

    @ApiModelProperty(value = "联系电话")
    @NotEmpty(message = "联系电话不能为空")
    private String contactPhone;

    @ApiModelProperty(value = "地址")
    @NotEmpty(message = "地址不能为空")
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

    public WarehouseInfo(Integer logcId, Integer isDelete){
        this.logcId = logcId;
        this.isDelete = isDelete;
    }
}
