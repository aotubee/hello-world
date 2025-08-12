package com.edc.erp.directly.distribution.model.out;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 直营订货单明细出参
 *
 * @author lishaobo
 * @since 2023/1/17 11:42
 */
@Data
@ApiModel(value = "AppOrderDetailOut", description = "直营订货单明细出参")
public class AppOrderDetailOut extends OrdDirOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "giftOutList", value = "赠品集合")
    private List<OrdDirOrderDetail> giftOutList;
}