package com.edc.erp.directly.distribution.model.out;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreOut;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 校验通过 保存门店信息和商品信息 实体
 * @author lx
 * @since 2022-12-06 17:05:57
 */
@Data
public class CheckDirOrderOut implements Serializable {

    private static final long serialVersionUID = -1L;

    /** 门店信息 */
    @ApiModelProperty(name = "storeOut",value = "门店信息")
    private StoreOut storeOut;

    /** 商品信息 */
    @ApiModelProperty(name = "goodsOut",value = "商品信息")
    private OrderGoodsOut goodsOut;
}
