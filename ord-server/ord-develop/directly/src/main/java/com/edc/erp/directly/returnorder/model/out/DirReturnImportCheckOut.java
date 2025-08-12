package com.edc.erp.directly.returnorder.model.out;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.returnorder.model.excel.OrdDirReturnImportErrorResult;
import com.edc.erp.directly.returnorder.model.in.ImportOrdReturnOrderVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @ClassName DisReturnImportCheckOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/11/28 8:50
 **/
@Data
public class DirReturnImportCheckOut implements Serializable {

    private static final long serialVersionUID = -6774822896855582695L;
    private List<ImportOrdReturnOrderVO> importOrdReturnOrderVOList;

    private List<OrdDirReturnImportErrorResult> errorResultList;

    private Map<String, OrderGoodsOut> storeGoodsMap;

    private Map<String, String> storeBizOrgCodeMap;

    private Map<String, Map<String, OrdDirDeliveryDetail>> deliverysMap;

}
