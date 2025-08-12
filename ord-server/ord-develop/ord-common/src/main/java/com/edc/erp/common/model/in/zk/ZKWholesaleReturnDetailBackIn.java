package com.edc.erp.common.model.in.zk;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName ShipmentDetailSendToZkIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/12 13:48
 **/
@Data
public class ZKWholesaleReturnDetailBackIn implements Serializable {
    private static final long serialVersionUID = -5713201470660740027L;

    @ApiModelProperty(name = "itemNo", value = "商品代码")
    private String itemNo;

    @ApiModelProperty(name = "itemQty", value = "数量")
    private String itemQty;

    @ApiModelProperty(name = "line", value = "行号")
    private String line;


}
