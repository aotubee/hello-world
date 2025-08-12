package com.edc.erp.directly.dirfirstorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 铺货单拆单配置出参
 * @author weichao
 */
@Data
public class FirstOrderConfigOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private Integer id;

    @ApiModelProperty(value = "拆分条件")
    private String splitCondition;

    @ApiModelProperty(value = "是否按照仓位拆分")
    private Integer isStock;

    @ApiModelProperty(value = "是否需要配送方式拆分")
    private Integer isDistributionMode;

    @ApiModelProperty(value = "是否需要分类拆分")
    private Integer isSort;

    @ApiModelProperty(value = "组织代码")
    private String bizOrgCode;

    @ApiModelProperty(value = "公司代码")
    private String orgCode;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;

    @ApiModelProperty(value = "品类值集合")
    private List<FirstOrderSortOut> firstOrderSortOuts;
}
