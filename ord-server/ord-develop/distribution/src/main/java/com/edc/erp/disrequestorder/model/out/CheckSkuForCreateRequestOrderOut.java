package com.edc.erp.disrequestorder.model.out;

import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import com.edc.erp.distribution.model.out.AppOrderDetailOut;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author fxw
 * @description: 创建集货单前商品信息校验
 * @since 2022/10/24 12:15
 */
@Data
public class CheckSkuForCreateRequestOrderOut implements Serializable {
    private static final long serialVersionUID = -3862180960802859513L;

    /**
     * 合法订货单
     *
     */
    @ApiModelProperty(name = "legalOrderMap", value = "合法订货单")
    private Map<OrdDisOrder, List<AppOrderDetailOut>> legalOrderMap;

    /**
     * 非法订货单
     *
     */
    @ApiModelProperty(name = "illegalOrderMap", value = "非法订货单")
    private Map<OrdDisOrder, List<OrdDisOrderDetail>> illegalOrderMap;

    /**
     * 非法SKU集合
     *
     */
    @ApiModelProperty(name = "illegalSkuAmountMap", value = "非法SKU金额集合")
    private Map<String, BigDecimal> illegalSkuAmountMap;


    /**
     * 非法sku
     *
     */
    @ApiModelProperty(name = "illegalSkuMap",value = "非法sku")
    private Map<String,String> illegalSkuMap;
}
