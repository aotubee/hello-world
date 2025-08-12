package com.edc.erp.directly.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName OrdDisDistributionStoreGoodsOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/19 11:10
 **/
@Data
public class OrdDirOverallDistributionStoreOut implements Serializable {
    private static final long serialVersionUID = -5139596056783754304L;

    @ApiModelProperty(name = "storeCode", value = "明细id")
    private String storeCode;

    @ApiModelProperty(name = "storeName", value = "明细id")
    private String storeName;

    @ApiModelProperty(name = "storeArea", value = "明细id")
    private String storeArea;

    @ApiModelProperty(name = "detailList", value = "分货明细")
    private List<OrdDirOverallDistributionGoodsOut> detailList;

}
