package com.edc.erp.directly.distribution.model.out;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @ClassName DirDistributionCheckDetailOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/12 17:59
 **/
@Data
public class DirDistributionGoodslOut implements Serializable {

    private String storeCode;

    private Map<String, OrderGoodsOut> goodsMap;
}
