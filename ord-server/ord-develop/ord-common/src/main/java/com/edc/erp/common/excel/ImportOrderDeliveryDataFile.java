package com.edc.erp.common.excel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 配销、配货数据文件上传
 */
@Data
public class ImportOrderDeliveryDataFile implements Serializable {

    private static final long serialVersionUID = 6841797282546396126L;

    @ApiModelProperty(name = "orderNo", value = "上传文件单号")
    private String orderNo;

    @ApiModelProperty(name = "orderNo", value = "上传文件行号")
    private Integer lineNo;

    @ApiModelProperty(name = "orderNo", value = "上传文件商品编码")
    private String goodsCode;

    @ApiModelProperty(name = "amount", value = "上传文件数据数量")
    private BigDecimal amount;

    @ApiModelProperty(name = "remark", value = "错误备注")
    private String remark;

    @ApiModelProperty(name = "isSuccessed", value = "是否成功状态 1：是；0：否")
    private Integer isSuccessed;

}
