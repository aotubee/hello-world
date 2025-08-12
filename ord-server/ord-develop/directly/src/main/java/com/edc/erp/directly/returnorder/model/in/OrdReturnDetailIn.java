package com.edc.erp.directly.returnorder.model.in;

import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
import lombok.Data;

/**
 * @return: 退货单明细入参
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
public class OrdReturnDetailIn extends OrdDirReturnDetail {

    private String storeCode;

    private String wrhCode;

    private String stockCode;

}
