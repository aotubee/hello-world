package com.edc.erp.presale.model.out;

import com.edc.erp.presale.entity.OrdDisPresaleAssets;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrdDisPresaleAssetsPageOut extends OrdDisPresaleAssets {
    private static final long serialVersionUID = 3590321841009001071L;
    /**
     * 品项数
     */
    @ApiModelProperty(name = "totalSkuQty", value = "品项数")
    private Integer totalSkuQty;

    /**
     * 总剩余数量
     */
    @ApiModelProperty(name = "totalSurplusGoodsQty", value = "总剩余数量")
    private BigDecimal totalSurplusGoodsQty;
}
