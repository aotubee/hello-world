package com.edc.erp.directly.distribution.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName QueryAllocationPoolPageIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/1/7 18:26
 **/
@Data
public class QueryAllocationPoolPageIn extends Page implements Serializable {
    private static final long serialVersionUID = 2385860086678614853L;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;

    private List<String> storeCodeList;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    @ApiModelProperty(name = "operation", value = "补单操作符")
    private String operation;

    @ApiModelProperty(name = "supplementQuantity", value = "补单量")
    private BigDecimal supplementQuantity;


    @ApiModelProperty(name = "truncationDateTimeBegin", value = "截单开始时间")
    private String truncationDateTimeBegin;


    @ApiModelProperty(name = "truncationDateTimeEnd", value = "截单结束时间")
    private String truncationDateTimeEnd;

    @ApiModelProperty(name = "sortCodes", value = "分类")
    private String sortCodes;

    private List<String> sortCodeList;

    private String bizOrgCode;
}
