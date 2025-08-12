/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */

package com.edc.erp.common.model.out.store;

import com.edc.erp.common.model.out.store.BizInvDtlOut;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 根据仓位code+商品codes获取业务可用库存出参
 *
 * @author: zhaolei
 * @date: 2022-08-20
 */
@Data
public class StoreBizInvOut implements Serializable {

    private static final long serialVersionUID = 590367413578909856L;

    /** 业务组织代码 */
    @ApiModelProperty(value = "业务组织代码")
    private String bizOrgCode;

    /** 门店代码(erp) */
    @ApiModelProperty(value = "门店代码(erp)")
    private String storeCode;

    /** 商品明细集合 */
    @ApiModelProperty(value = "商品明细集合")
    private List<BizInvDtlOut> bizInvDtlOutList;
}
