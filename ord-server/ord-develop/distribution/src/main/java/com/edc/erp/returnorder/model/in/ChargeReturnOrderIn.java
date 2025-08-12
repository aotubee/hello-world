package com.edc.erp.returnorder.model.in;

import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 分页查询退货单明细入参
 *
 * @author yaojinpeng
 * @since 2022/10/27 22:28
 */
@Data
public class ChargeReturnOrderIn  {

    @ApiModelProperty(name = "returnOrderId", value = "退货单id")
    @NotNull(message = "退货单id")
    private Integer returnOrderId;
    /**
     * 退货单单明细集合
     */
    private List<OrdDisReturnDetail> returnGoodsInfoInList;

    private String name;

    private String bizOrgCode;

}
