package com.edc.erp.disfirstorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName DeleteFirstOrderDetilIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/11/15 17:13
 **/
@Data
public class OrdDisDeleteFirstOrderDetailIn implements Serializable {
    private static final long serialVersionUID = 5495460041699851460L;

    @ApiModelProperty(value = "铺货单主键id")
    private Long firstOrderId;

    @ApiModelProperty(value = "明细id")
    private Long id;
}
