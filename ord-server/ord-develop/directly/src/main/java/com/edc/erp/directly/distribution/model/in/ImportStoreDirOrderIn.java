package com.edc.erp.directly.distribution.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 单店导入分货入参
 *
 * @author yaojinpeng
 * @since 2022/10/19 12:08
 */
@Data
public class ImportStoreDirOrderIn extends BaseEntity {

    @ApiModelProperty(name = "distributionOrderId", value = "分货单主键")
    private Long distributionOrderId;

    /**
     * 门店代码集合
     */
    @ApiModelProperty(name = "storeCodeList", value = "门店代码集合")
    private List<String> storeCodeList;

    /**
     * 导入SKU集合
     */
    @ApiModelProperty(name = "importStoreGoodsInList", value = "导入SKU集合")
    private List<ImportStoreGoodsIn> importStoreGoodsInList;
}
