package com.edc.erp.directly.dirfirstorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName OrdDirDistributionDetailAuditIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/4 15:21
 **/
@Data
public class OrdDirFirstOrderDetailAuditIn implements Serializable {
    private static final long serialVersionUID = -1761136474439167146L;

    @ApiModelProperty(name = "id", value = "明细id")
    private Long id;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "num", value = "直营铺货数量")
    private Integer num;
}
