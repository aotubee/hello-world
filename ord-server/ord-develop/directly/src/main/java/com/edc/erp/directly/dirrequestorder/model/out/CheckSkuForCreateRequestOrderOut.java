package com.edc.erp.directly.dirrequestorder.model.out;


import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import com.edc.erp.directly.distribution.model.out.AppOrderDetailOut;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author fxw
 * @description: 创建要货单前商品信息校验
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
    private Map<OrdDirOrder, List<AppOrderDetailOut>> legalOrderMap;

    /**
     * 非法订货单
     *
     */
    @ApiModelProperty(name = "illegalOrderMap", value = "非法订货单")
    private Map<OrdDirOrder, List<OrdDirOrderDetail>> illegalOrderMap;

    /**
     * 非法sku
     *
     */
    @ApiModelProperty(name = "illegalSkuMap",value = "非法sku")
    private Map<String,String> illegalSkuMap;
}
