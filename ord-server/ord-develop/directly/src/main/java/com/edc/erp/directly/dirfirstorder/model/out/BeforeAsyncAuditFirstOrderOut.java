package com.edc.erp.directly.dirfirstorder.model.out;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDetail;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BeforeAsyncAuditFirstOrderOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/11/7 17:56
 **/
@Data
public class BeforeAsyncAuditFirstOrderOut implements Serializable {
    private static final long serialVersionUID = 9021567141738491955L;

    private List<OrdDirOrderFirstDetail> ordDirOrderFirstDetailList;

    private Map<String, OrderGoodsOut> goodsMap;
}
