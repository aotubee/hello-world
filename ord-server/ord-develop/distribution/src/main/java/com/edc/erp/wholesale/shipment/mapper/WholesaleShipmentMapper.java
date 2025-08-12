package com.edc.erp.wholesale.shipment.mapper;

import com.edc.erp.common.model.out.purchase.TransferShipmentPushPurchaseBackVO;
import com.edc.erp.model.in.QueryWarehouseIn;
import com.edc.erp.model.out.StockInfoOut;
import com.edc.erp.model.out.WarehouseInfoOut;
import com.edc.erp.wholesale.model.in.PushPurUpdateIn;
import com.edc.erp.wholesale.model.in.shipment.QueryPushPurIn;
import com.edc.erp.wholesale.model.in.shipment.QueryShipmentIn;
import com.edc.erp.wholesale.model.out.shipment.QueryShipmentReportOut;
import com.edc.erp.wholesale.model.out.shipment.ShipmentWithDetailOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleDateInfoOut;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 批发出货单(WholesaleShipment)表数据库访问层
 *
 * @author lx
 * @since 2022-10-18 11:59:38
 */
@Repository
public interface WholesaleShipmentMapper extends BaseMapper<WholesaleShipment> {

    /**
     * 分页查询批发出货单
     * @param queryShipmentIn
     * @return
     */
    List<ShipmentWithDetailOut> findShipmentByPage(@Param("query") QueryShipmentIn queryShipmentIn);

    /**
     * 查询允许批发的仓位信息
     * @param queryWarehouseIn 仓位信息入参
     * @return
     */
    List<StockInfoOut> findStockInfo(QueryWarehouseIn queryWarehouseIn);

    /**
     * 根据允许批发的仓位信息的仓储id查询仓储信息
     * @param stockInfoList 仓位信息集合
     * @return
     */
    List<WarehouseInfoOut> findWarehouseInfo(@Param("stockInfo") List<StockInfoOut> stockInfoList);

    /**
     * 根据商品代码和出货单状态 查询出货单详情
     * @param goodsCode 商品代码
     * @param status 出货单状态
     * @param bizOrgCode 业务组织
     * @return
     */
    WholesaleShipmentDetail getWholesaleShipmentDetail(
            @Param("goodsCode") String goodsCode,
            @Param("status") String status,
            @Param("bizOrgCode") String bizOrgCode);

    QueryShipmentReportOut queryShipmentReportForPage(@Param("query") QueryShipmentIn queryShipmentIn);

    List<Long> findNeedPushPurWholesaleShipmentId(QueryPushPurIn queryPushPurIn);

    int updatePurBatchNumberByIdList(PushPurUpdateIn pushPurUpdateIn);

    List<Long> findNeedSumWholesaleOrderIdList(@Param("query") QueryShipmentIn queryShipmentIn);

    int updateOrder(@Param("order") WholesaleShipment wholesaleShipment, @Param("beforeStatus") String beforeStatus);

    List<Long> findNeedDelayPushPurWholesaleShipmentId(QueryPushPurIn queryPushPurIn);

    int updatePushPurTime(WholesaleShipment wholesaleShipment);

    int updateShipmentPurchaseNoByPurBatchNumber(@Param("obj") TransferShipmentPushPurchaseBackVO transferShipmentPushPurchaseBackVO,
                                                 @Param("updater") String updater, @Param("updateTime") LocalDateTime updateTime);


}
