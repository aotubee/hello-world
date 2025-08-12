package com.edc.erp.disdifferenceorder.model.in;

import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifferenceDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 配销差异单入参
 *
 * @author weichao
 */
@Data
public class ApprovedDisDifferenceOrderIn {

    @ApiModelProperty(name = "diffOrderId", value = "差异单Id")
    @NotNull(message = "差异单Id")
    private Integer diffOrderId;
    /**
     * 差异单明细集合
     */
    private List<OrdDisDelivDifferenceDetail> differenceDetails;


    private String bizOrgCode;
}
