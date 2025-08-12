package com.edc.erp.common.model.in.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订货时间周期入参
 * @author zhaolei
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleWeekIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品code
    */
   private String goodsCode;

    /**
     * 组织代码
    */
   private String bizOrgCode;

}
