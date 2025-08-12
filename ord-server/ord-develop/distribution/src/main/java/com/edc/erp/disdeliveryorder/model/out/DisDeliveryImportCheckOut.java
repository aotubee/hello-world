package com.edc.erp.disdeliveryorder.model.out;

import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.disdeliveryorder.model.excel.OrdDisDeliveryImportErrorResult;
import com.edc.erp.disdeliveryorder.model.in.ImportDisDeliveryOrder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @ClassName DisDeliveryAsyncImportCheckResult
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/11/22 9:28
 **/
@Data
public class DisDeliveryImportCheckOut implements Serializable {
    private static final long serialVersionUID = -2647162931537927383L;

    private List<OrdDisDeliveryImportErrorResult> errorResultList;

    private List<ImportDisDeliveryOrder> deliveryOrderList;

    private Map<String, StoreInfo> storeInfoMap;

}
