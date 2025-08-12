package com.edc.erp.returnorder.model.out;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.returnorder.model.excel.OrdDisReturnImportErrorResult;
import com.edc.erp.returnorder.model.in.ImportOrdReturnOrderVO;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName DisReturnImportCheckOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/11/28 8:50
 **/
@Data
public class DisReturnImportCheckOut implements Serializable {

    private static final long serialVersionUID = 3029640149562133197L;
    private List<ImportOrdReturnOrderVO> importOrdReturnOrderVOList;

    private List<OrdDisReturnImportErrorResult> errorResultList;

    private Map<String, OrderGoodsOut> storeGoodsMap;

    private Map<String, String> storeBizOrgCodeMap;

    private Map<String, Map<String, OrdDisDeliveryDetail>> deliverysMap;
}
