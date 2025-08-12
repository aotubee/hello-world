package com.edc.erp.common.model.out.stock;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 仓位出参类
 * @author lee
 */
@Data
public class StockInfoOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "仓位id")
    private Integer id;

    @ApiModelProperty(value = "仓储代码")
    private String warehouseCode;

    @ApiModelProperty(value = "仓储名称")
    private String warehouseName;

    @ApiModelProperty(value = "仓位代码")
    @NotEmpty(message = "仓位代码不能为空")
    private String stockCode;

    @ApiModelProperty(value = "仓位名称")
    @NotEmpty(message = "仓位代码不能为空")
    private String stockName;

    @ApiModelProperty(value = "是否启用")
    private Integer isEnable;

    @ApiModelProperty(value = "仓储ID")
    @NotNull(message = "所属仓储不能为空")
    private Integer warehouseId;

    @ApiModelProperty(value = "是否允许负库存")
    private Integer isAllowNegativeStocks;

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

    @ApiModelProperty(value = "配送周期类型")
    private String distributionCycleType;

}
