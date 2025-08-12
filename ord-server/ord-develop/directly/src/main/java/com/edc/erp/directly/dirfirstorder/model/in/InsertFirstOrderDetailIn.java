/**
 * Copyright © 2010-2021 Everyday Chain. All rights reserved.
 */
package com.edc.erp.directly.dirfirstorder.model.in;

import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 新增铺货单及明细入参
 *
 * @author: weichao
 */
@Data
public class InsertFirstOrderDetailIn {

    @ApiModelProperty(value = "铺货单主键id")
    private Long firstOrderId;

    @ApiModelProperty(value = "门店代码")
    private String storeCode;
    /** 生效时间 */
    @ApiModelProperty(name = "effectiveTime", value = "生效时间")
    private LocalDateTime effectiveTime;

    /** 是否立即生效 */
    @ApiModelProperty(name = "isEffectiveImmediately", value = "是否立即生效")
    private Integer isEffectiveImmediately;

    @ApiModelProperty(value = "铺货单明细集合")
    private List<OrdDirOrderFirstDetail> firstOrderDetails;

}
