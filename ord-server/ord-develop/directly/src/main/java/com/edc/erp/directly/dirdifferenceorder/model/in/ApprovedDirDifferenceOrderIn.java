package com.edc.erp.directly.dirdifferenceorder.model.in;

import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifferenceDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 直营配货差异单入参
 *
 * @author weichao
 */
@Data
public class ApprovedDirDifferenceOrderIn {

    @ApiModelProperty(name = "diffOrderId", value = "差异单Id")
    @NotNull(message = "差异单Id")
    private Integer diffOrderId;
    /**
     * 差异单明细集合
     */
    private List<OrdDirDelivDifferenceDetail> differenceDetails;

//    private String name;

    private String bizOrgCode;
}
