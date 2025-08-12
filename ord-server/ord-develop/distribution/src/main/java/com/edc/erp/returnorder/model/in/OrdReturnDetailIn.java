package com.edc.erp.returnorder.model.in;

import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lh
 */
@Data
public class OrdReturnDetailIn extends OrdDisReturnDetail {


    private String storeCode;

    private String wrhCode;

    private String stockCode;
}
