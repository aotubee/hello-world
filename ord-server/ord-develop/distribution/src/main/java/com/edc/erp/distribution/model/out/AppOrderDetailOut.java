package com.edc.erp.distribution.model.out;

import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author lishaobo
 * @description: APP订货单明细出参
 * @since 2023/01/17 11:16
 */
@Data
public class AppOrderDetailOut extends OrdDisOrderDetail implements Serializable {
    private static final long serialVersionUID = -4222436605040968934L;

    @ApiModelProperty(name = "giftOutList", value = "赠品集合")
    private List<OrdDisOrderDetail> giftOutList;
}
