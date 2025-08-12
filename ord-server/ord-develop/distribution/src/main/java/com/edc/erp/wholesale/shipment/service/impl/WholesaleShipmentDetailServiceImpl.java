package com.edc.erp.wholesale.shipment.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.model.out.purchase.TransferShipmentPushPurchaseBackVO;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.wholesale.model.in.shipment.*;
import com.edc.erp.wholesale.model.out.shipment.TransferShipmentPushPurchaseOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleDateInfoOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleShipmentDetailOut;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import com.edc.erp.wholesale.shipment.mapper.WholesaleShipmentDetailMapper;
import com.edc.erp.wholesale.shipment.mapper.WholesaleShipmentMapper;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 批发出货单明细(WholesaleShipmentDetail)表服务实现类
 *
 * @author lx
 * @since 2022-10-18 11:59:38
 */
@Service
@RequiredArgsConstructor
public class WholesaleShipmentDetailServiceImpl extends BaseServiceImpl<WholesaleShipmentDetail> implements WholesaleShipmentDetailService {

    private final WholesaleShipmentDetailMapper wholesaleShipmentDetailMapper;
    private final WholesaleShipmentMapper wholesaleShipmentMapper;

    /**
     * 保存出货单明细
     * @param wholesaleShipmentDetailList 出货单明细
     * @param wholesaleShipment 出货单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList, WholesaleShipment wholesaleShipment) {
        //明细实体
        WholesaleShipmentDetail wholesaleShipmentDetail = new WholesaleShipmentDetail();
        //添加出货单id
        wholesaleShipmentDetail.setWholesaleShipmentId(wholesaleShipment.getId());
        //根据出货单id 删除明细 避免重复添加
        wholesaleShipmentDetailMapper.delete(wholesaleShipmentDetail);

        //当前列表商品去重
        Map<String, String> checkMap = new HashMap<>(2);
        AtomicReference<Integer> line = new AtomicReference<>(NumberUtil.INTEGER_ONE);
        wholesaleShipmentDetailList.forEach(item -> {
            if (checkMap.containsKey(item.getGoodsCode())) {
                throw new BusinessException(item.getGoodsCode() + "：此出货单商品代码重复");
            }
            //添加审核数量
            item.setAuditQuantity(item.getAuditQuantity());
            //添加审核金额
            item.setAuditAmount(item.getUnitPrice().multiply(new BigDecimal(item.getAuditQuantity())));

            //添加出货单id
            item.setWholesaleShipmentId(wholesaleShipment.getId());
            //添加创建人
            item.setCreator(wholesaleShipment.getCreator());
            //添加创建时间
            item.setCreateTime(LocalDateTime.now());
            //添加更新者
            item.setUpdater(wholesaleShipment.getUpdater());
            //添加更新时间
            item.setUpdateTime(LocalDateTime.now());
            item.setLine(line.get());
            if (Objects.isNull(item.getIsGift())) {
                item.setIsGift(ModelConst.DELETE.NO);
            }
            line.getAndSet(line.get() + NumberUtil.INTEGER_ONE);
            //将当前列表商品code添加 去重
            checkMap.put(item.getGoodsCode(), item.getGoodsCode());
        });
        //批量保存出货单明细
        this.batchSave(wholesaleShipmentDetailList);
    }

    /**
     * 批量保存出货单明细
     * @param wholesaleShipmentDetailList 出货单明细
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList) {
        //批量保存出货单明细
        int pages = wholesaleShipmentDetailList.size() % SystemConstant.PAGE_SIZE == 0 ? wholesaleShipmentDetailList.size() / SystemConstant.PAGE_SIZE : wholesaleShipmentDetailList.size() / SystemConstant.PAGE_SIZE + 1;
        for (int i = 0; i < pages; i++) {
            wholesaleShipmentDetailMapper.batchSave(wholesaleShipmentDetailList.subList(i * SystemConstant.PAGE_SIZE, i == pages - 1 ? wholesaleShipmentDetailList.size() : (i + 1) * SystemConstant.PAGE_SIZE));
        }
    }

    /**
     * 查询出货单详情
     * @param queryShipmentDetailIn 调整单明细入参类
     * @return
     */
    @Override
    public List<WholesaleShipmentDetailIn> findShipmentDetailList(QueryShipmentDetailIn queryShipmentDetailIn) {
        return wholesaleShipmentDetailMapper.findShipmentDetailList(queryShipmentDetailIn);
    }

    /**
     * 分页查询出货单明细
     * @param queryShipmentDetailIn 出货单明细查询入参
     * @return
     */
    @Override
    public Page<WholesaleShipmentDetailOut> findByPage(QueryShipmentDetailIn queryShipmentDetailIn) {
        List<WholesaleShipmentDetailOut> shipmentDetailOuts = wholesaleShipmentDetailMapper.findByPage(queryShipmentDetailIn);
        shipmentDetailOuts.forEach(item -> {
            //品类属性 - 中文
            item.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(item.getGoodsType()));
        });
        Page<WholesaleShipmentDetailOut> detailOutPage = new Page<>();
        detailOutPage.setList(shipmentDetailOuts);
        return detailOutPage;
    }

    /**
     * 批量更新批发出货单明细
     * @param wholesaleShipmentDetailList 批发出货单明细 DTS回传处理之后的数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList) {
        //批量保存出货单明细
        int pages = wholesaleShipmentDetailList.size() % SystemConstant.PAGE_SIZE == 0 ? wholesaleShipmentDetailList.size() / SystemConstant.PAGE_SIZE : wholesaleShipmentDetailList.size() / SystemConstant.PAGE_SIZE + 1;
        for (int i = 0; i < pages; i++) {
            wholesaleShipmentDetailMapper.batchUpdate(wholesaleShipmentDetailList.subList(i * SystemConstant.PAGE_SIZE, i == pages - 1 ? wholesaleShipmentDetailList.size() : (i + 1) * SystemConstant.PAGE_SIZE));
        }
    }

    @Override
    public int batchUpdateAudit(List<WholesaleShipmentDetail> wholesaleShipmentDetailList) {
        return wholesaleShipmentDetailMapper.batchUpdateAudit(wholesaleShipmentDetailList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveList(List<WholesaleShipmentDetail> wholesaleShipmentDetailList) {
        //批量保存出货单明细
        int pages = wholesaleShipmentDetailList.size() % SystemConstant.PAGE_SIZE == 0 ? wholesaleShipmentDetailList.size() / SystemConstant.PAGE_SIZE : wholesaleShipmentDetailList.size() / SystemConstant.PAGE_SIZE + 1;
        for (int i = 0; i < pages; i++) {
            wholesaleShipmentDetailMapper.batchSaveList(wholesaleShipmentDetailList.subList(i * SystemConstant.PAGE_SIZE, i == pages - 1 ? wholesaleShipmentDetailList.size() : (i + 1) * SystemConstant.PAGE_SIZE));
        }
    }

    @Override
    public List<WholesaleShipmentDetail> findListByShipmentId(Long shipmentId) {
        WholesaleShipmentDetail wholesaleShipmentDetail = new WholesaleShipmentDetailIn();
        wholesaleShipmentDetail.setWholesaleShipmentId(shipmentId);
        wholesaleShipmentDetail.setIsDelete(ModelConst.DELETE.NO);
        return wholesaleShipmentDetailMapper.select(wholesaleShipmentDetail);
    }

    @Override
    public List<TransferShipmentPushPurchaseOut> findNeedPushPurDetailList(QueryPushPurIn queryPushPurIn) {
        return wholesaleShipmentDetailMapper.findNeedPushPurDetailList(queryPushPurIn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePurchaseNoByPurBatchNumber(TransferShipmentPushPurchaseBackVO transferShipmentPushPurchaseBackVO, String updater, LocalDateTime updateTime) {
        return wholesaleShipmentDetailMapper.updateShipmentDetailPurchaseNoByPurBatchNumber(transferShipmentPushPurchaseBackVO, updater, updateTime);
    }

    @Override
    public List<TransferShipmentPushPurchaseOut> findNeedPushPurDetailListByShipmentIdList(List<Long> shipmentIdList, String bizOrgCode) {
        return wholesaleShipmentDetailMapper.findNeedPushPurDetailListByShipmentIdList(shipmentIdList, bizOrgCode);
    }

    @Override
    public WholesaleDateInfoOut sumWholesaleDateInfoByIdList(List<Long> idList) {
        return wholesaleShipmentDetailMapper.sumWholesaleDateInfoByIdList(idList);
    }

    @Override
    public HSWholesaleDifferenceOrder initHsOrderDifference(Long id) {
        WholesaleShipment wholesaleShipment = wholesaleShipmentMapper.selectByPrimaryKey(id);
        List<WholesaleShipmentDetail> detailList = this.findListByShipmentId(id);
        List<HSWholesaleDifferenceDetail> differenceDetailList = Lists.newArrayList();
        detailList.forEach(detail -> {
            Integer lackQty = NumberUtil.INTEGER_ZERO;
            if (ShipmentStatusEnum.APPROVED.getCode().equals(wholesaleShipment.getShipmentStatus()) || ShipmentStatusEnum.PENDING.getCode().equals(wholesaleShipment.getShipmentStatus())) {
                lackQty = detail.getApplyQuantity() - detail.getAuditQuantity();
            }
            if (ShipmentStatusEnum.SHIPPED.getCode().equals(wholesaleShipment.getShipmentStatus()) || ShipmentStatusEnum.PENDING.getCode().equals(wholesaleShipment.getShipmentStatus())) {
                lackQty = detail.getAuditQuantity() - (Objects.isNull(detail.getShipmentQuantity()) ? NumberUtil.INTEGER_ZERO : detail.getShipmentQuantity());
            }
            if (lackQty.equals(NumberUtil.INTEGER_ZERO)) {
                return;
            }
            HSWholesaleDifferenceDetail hsWholesaleDifferenceDetail = new HSWholesaleDifferenceDetail();
            hsWholesaleDifferenceDetail.setGoodsCode(detail.getGoodsCode());
            hsWholesaleDifferenceDetail.setBarCode(detail.getBarCode());
            hsWholesaleDifferenceDetail.setLine(detail.getLine());
            hsWholesaleDifferenceDetail.setQty(new BigDecimal(lackQty));
            hsWholesaleDifferenceDetail.setIsGift(Objects.nonNull(detail.getIsGift()) ? detail.getIsGift().toString() : null);
            differenceDetailList.add(hsWholesaleDifferenceDetail);
        });
        if (CollectionUtils.isEmpty(differenceDetailList)) {
            return null;
        }
        HSWholesaleDifferenceOrder hsWholesaleDifferenceOrder = new HSWholesaleDifferenceOrder();
        hsWholesaleDifferenceOrder.setErpOrderNo(wholesaleShipment.getShipmentNo());
        hsWholesaleDifferenceOrder.setOrderNo(wholesaleShipment.getSourceNo());
//        hsWholesaleDifferenceOrder.setReturnType(NumberUtil.INTEGER_TWO.toString());
        hsWholesaleDifferenceOrder.setBizOrgCode(wholesaleShipment.getBizOrgCode());
        hsWholesaleDifferenceOrder.setDetailList(differenceDetailList);
        return hsWholesaleDifferenceOrder;
    }

    @Override
    public List<TransferShipmentPushPurchaseOut> findNeedDelayPushPurDetailList(QueryPushPurIn queryPushPurIn) {
        return wholesaleShipmentDetailMapper.findNeedDelayPushPurDetailList(queryPushPurIn);
    }
}
