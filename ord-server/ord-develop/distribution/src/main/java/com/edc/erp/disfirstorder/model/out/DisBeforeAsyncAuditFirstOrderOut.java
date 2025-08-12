package com.edc.erp.disfirstorder.model.out;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDetail;
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
public class DisBeforeAsyncAuditFirstOrderOut implements Serializable {
    private static final long serialVersionUID = -5570637168369856913L;

    private List<OrdDisOrderFirstDetail> ordDisOrderFirstDetailList;

    private Map<String, OrderGoodsOut> goodsMap;
}
