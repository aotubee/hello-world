package com.edc.erp.directly.returnnoticeorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @return: 退货门店信息入参
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnStoreInfoIn implements Serializable {


    /**
     * 门店代码
     */
    @ApiModelProperty(name = "id", value = "门店表id")
    private Long id;
    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 可退数量
     */
    @ApiModelProperty(name = "returnNum", value = "可退数量")
    private BigDecimal qty;
}
