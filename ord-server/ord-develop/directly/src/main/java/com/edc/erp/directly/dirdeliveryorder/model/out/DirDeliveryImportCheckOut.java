package com.edc.erp.directly.dirdeliveryorder.model.out;

import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.directly.dirdeliveryorder.model.excel.OrdDirDeliveryImportErrorResult;
import com.edc.erp.directly.dirdeliveryorder.model.in.ImportDirDeliveryOrder;
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
public class DirDeliveryImportCheckOut implements Serializable {
    private static final long serialVersionUID = -2647162931537927383L;

    private List<OrdDirDeliveryImportErrorResult> errorResultList;

    private List<ImportDirDeliveryOrder> deliveryOrderList;

    private Map<String, StoreInfo> storeInfoMap;
}
