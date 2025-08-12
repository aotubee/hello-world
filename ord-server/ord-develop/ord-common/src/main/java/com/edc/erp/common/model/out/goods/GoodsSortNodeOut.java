package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName GoodsSortNodeOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/3/5 9:41
 **/
@Data
public class GoodsSortNodeOut extends GoodsSortOut implements Serializable {
    private static final long serialVersionUID = -6499720509889651114L;

    @ApiModelProperty(value = "子集")
    private List<GoodsSortNodeOut> childNodes;


}
