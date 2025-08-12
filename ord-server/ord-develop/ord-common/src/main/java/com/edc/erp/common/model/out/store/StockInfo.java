package com.edc.erp.common.model.out.store;

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
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 仓位表
 * </p>
 *
 * @author gusiyuan
 * @since 2022-06-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "logc_stock_info")
@ApiModel(value="StockInfo对象", description="仓位表")
public class StockInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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


}
