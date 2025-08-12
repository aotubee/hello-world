package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 标准规格出参
 * @author lee
 */
@Data
public class StandardSpecOut {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "标准商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "规格说明")
    private String qpcStr;

    @ApiModelProperty(value = "规格数量")
    private Integer qpc;

    @ApiModelProperty(value = "单位ID")
    private Integer unitId;

    @ApiModelProperty(value = "长(cm)")
    private BigDecimal length;

    @ApiModelProperty(value = "宽(cm)")
    private BigDecimal width;

    @ApiModelProperty(value = "高(cm)")
    private BigDecimal height;

    @ApiModelProperty(value = "重量(g)")
    private BigDecimal weight;

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

    @ApiModelProperty(value = "单位名称")
    private String unitName;

}
