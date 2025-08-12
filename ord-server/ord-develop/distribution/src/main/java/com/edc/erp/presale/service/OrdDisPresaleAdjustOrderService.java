package com.edc.erp.presale.service;

import com.edc.erp.presale.entity.OrdDisPresaleAdjustOrder;
import com.edc.erp.presale.model.in.PresaleAdjustOrderPageIn;
import com.edc.erp.presale.model.in.PresaleAdjustOrderSaveIn;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsDetailOut;
import com.edc.erp.presale.model.out.PresaleAdjustOrderDetailOut;
import com.edc.erp.presale.model.out.PresaleAdjustOrderGetOut;
import com.edc.erp.presale.model.out.PresaleAdjustOrderPageOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface OrdDisPresaleAdjustOrderService extends BaseService<OrdDisPresaleAdjustOrder> {
    /**
     * 分页查询预售调整单
     *
     * @param presaleAdjustOrderPageIn
     * @return
     */
    Page<PresaleAdjustOrderPageOut> findPresaleAdjustOrderByPage(PresaleAdjustOrderPageIn presaleAdjustOrderPageIn);

    /**
     * 预售调整单详情
     *
     * @param id
     * @return
     */
    PresaleAdjustOrderGetOut getPresaleAdjustOrderDetail(@NotNull Long id);

    /**
     * 作废预售调整单
     *
     * @param id
     * @return
     */
    boolean invalidAdjustOrder(@NotNull Long id);

    /**
     * 审核预售调整单
     *
     * @param id
     * @return
     */
    boolean approveAdjustOrder(@NotNull Long id);

    /**
     * 保存预售调整单
     *
     * @param orderSaveIn
     * @return
     */
    Long saveAdjustOrder(PresaleAdjustOrderSaveIn orderSaveIn);

    /**
     * 冲销预售调整单
     *
     * @param id
     * @return
     */
    boolean chargeAdjustOrder(@NotNull Long id);

    /**
     * 导出预售调整单
     *
     * @param presaleAdjustOrderPageIn
     * @return
     */
    String exportAdjustOrder(PresaleAdjustOrderPageIn presaleAdjustOrderPageIn);

    /**
     * 预售调整单详情商品批量导出
     * @param presaleAdjustOrderPageIn
     * @return
     */
    String exportAdjustOrders(PresaleAdjustOrderPageIn presaleAdjustOrderPageIn);

    /**
     * 导出预售调整单详情
     *
     * @param id
     * @return
     */
    String exportAdjustOrderDetail(@NotNull Long id);

    /**
     * 根据门店编码、商品代码查询资产模块该门店商品代码关联的数据
     *
     * @param storeCode
     * @param goodsCode
     * @return
     */
    List<OrdDisPresaleAssetsDetailOut> getStoreGoodsAssetsDetailList(String storeCode, String goodsCode);

    /**
     * 导入预售调整单
     *
     * @param fileId
     * @return
     */
    Response<String> importAdjustOrder(String fileId);

    /**
     * 导入预售调整单明细
     *
     * @param fileId
     * @param adjustType
     * @return
     */
    Response<List<PresaleAdjustOrderDetailOut>> importAdjustOrderDetail(String storeCode, String fileId, String adjustType);
}
