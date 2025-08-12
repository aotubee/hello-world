package com.edc.erp.returnnoticeorder.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.enumeration.OrderNoPreEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.InvBizRsnTransOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.*;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNotice;
import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNoticeGoods;
import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNoticeStore;
import com.edc.erp.returnnoticeorder.enumeration.OrdDisReturnNoticeStatusEnum;
import com.edc.erp.returnnoticeorder.enumeration.OrdReturnNoticeTypeEnum;
import com.edc.erp.returnnoticeorder.listener.OrdDisReturnListener;
import com.edc.erp.returnnoticeorder.mapper.OrdDisReturnNoticeMapper;
import com.edc.erp.returnnoticeorder.model.in.*;
import com.edc.erp.returnnoticeorder.model.out.*;
import com.edc.erp.returnnoticeorder.service.OrdDisReturnNoticeService;
import com.edc.erp.returnnoticeorder.service.OrdReturnNoticeGoodsService;
import com.edc.erp.returnnoticeorder.service.OrdReturnNoticeStoreService;
import com.edc.erp.returnorder.entity.OrdDisReturn;
import com.edc.erp.returnorder.mapper.OrdDisReturnMapper;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 退货通知单表(DisReturnNotice)表服务实现类
 *
 * @author yaojinpeng
 * @since 2022-10-21 10:55:46
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdDisReturnNoticeServiceImpl extends BaseServiceImpl<OrdDisReturnNotice> implements OrdDisReturnNoticeService {

    private final OrdDisReturnNoticeMapper ordDisReturnNoticeMapper;

    private final OrdReturnNoticeGoodsService ordReturnNoticeGoodsService;

    private final UniqueUtils uniqueUtils;

    private final OrdReturnNoticeStoreService ordReturnNoticeStoreService;

    private final OrderGoodsServer orderGoodsServer;

    private final AsyncLogService asyncLogService;

    private final OrdDisReturnMapper ordDisReturnMapper;

    private final EquipmentBusinessReasonServer equipmentBusinessReasonServer;

    private final WarehouseServer warehouseServer;

    private final StockServer stockServer;

    private final FileService fileService;

    private final StoreCenterService storeCenterService;


    @Override
    public int insertReturnNotice(OrdDisReturnNotice ordReturnNotice) {
        ordReturnNotice.setIsDelete(ModelConst.DELETE.NO);
        ordReturnNotice.setReturnNoticeOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.XZ.getCode(), ordReturnNotice.getBizOrgCode(), uniqueUtils, 4));
        return ordDisReturnNoticeMapper.insertSelective(ordReturnNotice);
    }

    @Override
    public Page<OrdReturnNoticeOrderOut> findReturnNoticeOrderOutForPage(OrdReturnNoticeIn ordReturnNoticeIn) {
        List<OrdReturnNoticeOrderOut> ordReturnNoticeOrderOuts = ordDisReturnNoticeMapper.findOrdReturnNoticeByPage(ordReturnNoticeIn);
        ordReturnNoticeOrderOuts.forEach(orderOut -> {
            orderOut.setReturnTypeValue(OrdReturnNoticeTypeEnum.getValueByKey(orderOut.getReturnType()));
            orderOut.setStatusStr(OrdDisReturnNoticeStatusEnum.getValueByKey(orderOut.getStatus()));
            InvBizRsnTransOut invBizRsnTransOut = equipmentBusinessReasonServer.getStoreInvBizRsnTransByCode(orderOut.getReturnWhy(), ordReturnNoticeIn.getBizOrgCode(), NumberUtil.INTEGER_ZERO);
            orderOut.setReturnWhyValue(Objects.isNull(invBizRsnTransOut) ? "" : invBizRsnTransOut.getBusinessReasonName());
            if (LocalDateTime.now().isBefore(orderOut.getReturnDeadline())) {
                orderOut.setReturnNoticeStatusValue("进行中");
            } else {
                orderOut.setReturnNoticeStatusValue("已过期");
            }
        });
        Page<OrdReturnNoticeOrderOut> resultPage = new Page<>(ordReturnNoticeIn);
        resultPage.setList(ordReturnNoticeOrderOuts);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int invalidatedOrdReturnNotice(OrdDisReturnNotice ordDisReturnNotice) {
        ordDisReturnNotice.setStatus(OrdDisReturnNoticeStatusEnum.INVALID.getKey());
        ordDisReturnNoticeMapper.updateByPrimaryKeySelective(ordDisReturnNotice);

        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE.getName(), String.valueOf(ordDisReturnNotice.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE.getCode(), OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE_INVALID.getName(), new Date(), ordDisReturnNotice.getCreator());

        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return ordDisReturnNotice.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response auditOrdDisReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice) {
        OrdDisReturnNotice ordDisReturnNotice = this.getReturnNoticeOrderById(saveOrdReturnNotice.getReturnNoticeOrderId());
        if (null == ordDisReturnNotice) {
            return Response.error("无效的退货通知单");
        }
        if (OrdDisReturnNoticeStatusEnum.INVALID.getKey().equals(ordDisReturnNotice.getStatus())) {
            return Response.error("退货通知单已作废");
        }
        if (OrdDisReturnNoticeStatusEnum.PROCESSED.getKey().equals(ordDisReturnNotice.getStatus())) {
            return Response.error("退货通知单已生效");
        }
        if (OrdDisReturnNoticeStatusEnum.APPROVED.getKey().equals(ordDisReturnNotice.getStatus())) {
            return Response.error("退货通知单已审核");
        }

        if (CollectionUtils.isEmpty(saveOrdReturnNotice.getGoodsDetailed())) {
            return Response.error("明细列表不能为空");
        }
        //添加数据
        this.saveOrdReturnNotice(saveOrdReturnNotice);


       /* List<ReturnGoodsAndStoreIn> goodsDetailedList = saveOrdReturnNotice.getGoodsDetailed();
        List<OrdDisReturnNoticeGoods> ordDisReturnNoticeGoodsList=ordDisReturnNoticeGoodsMapper.findByReturnNoticeId(saveOrdReturnNotice.getReturnNoticeOrderId());
        //商品取交集
        List<ReturnGoodsAndStoreIn> equalGoodsCodeList = goodsDetailedList.stream().filter(goodsDetailed ->
                ordDisReturnNoticeGoodsList.stream().map(OrdDisReturnNoticeGoods::getGoodsCode).collect(Collectors.toList()).contains(goodsDetailed.getGoodsCode())
        ).collect(Collectors.toList());
      //门店取交集
        List<ReturnStoreInfoIn> storeInfoList = new ArrayList<>();
        List<ReturnStoreInfoIn> equalStoreCodeList = new ArrayList<>();
        List<ReturnGoodsAndStoreIn> list =new ArrayList<>();
        for (ReturnGoodsAndStoreIn returnGoodsAndStoreIn : equalGoodsCodeList) {
             storeInfoList = returnGoodsAndStoreIn.getStoreInfo();
            OrdDisReturnNoticeGoods ordDisReturnNoticeGoods = ordDisReturnNoticeGoodsMapper.getByReturnNoticeIdAndGoodsCode(saveOrdReturnNotice.getReturnNoticeOrderId(), returnGoodsAndStoreIn.getGoodsCode());
            List<OrdReturnNoticeStoreOut> ordReturnNoticeStoreOutList = ordDisReturnNoticeStoreMapper.findByReturnNoticeIdAndReturnGoodsId(saveOrdReturnNotice.getReturnNoticeOrderId(), ordDisReturnNoticeGoods.getId());
            equalStoreCodeList = storeInfoList.stream().filter(storeInfo ->
                    ordReturnNoticeStoreOutList.stream().map(OrdDisReturnNoticeStore::getStoreCode).collect(Collectors.toList()).contains(storeInfo.getStoreCode())
            ).collect(Collectors.toList());

            //比较
             if(equalStoreCodeList.size()!=storeInfoList.size()){
                 returnGoodsAndStoreIn.setStoreInfo(equalStoreCodeList);
                 returnGoodsAndStoreIn.setGoodsCode(ordDisReturnNoticeGoods.getGoodsCode());
             }
            list.add(returnGoodsAndStoreIn);
        }
        if(list.size()!=goodsDetailedList.size() || equalStoreCodeList.size()!=storeInfoList.size()){
            saveOrdReturnNotice.setGoodsDetailed(list);
            return Response.data(saveOrdReturnNotice);
        }*/
        OrdDisReturnNotice returnNotice = ordDisReturnNoticeMapper.selectByPrimaryKey(saveOrdReturnNotice.getReturnNoticeOrderId());
        if (saveOrdReturnNotice.getIsEffectiveImmediately().equals(NumberUtil.INTEGER_ONE)) {
            returnNotice.setTakeEffectTime(LocalDateTime.now());
            returnNotice.setStatus(OrdDisReturnNoticeStatusEnum.PROCESSED.getKey());
            ordDisReturnNoticeMapper.updateByPrimaryKeySelective(returnNotice);
            addLog(returnNotice, OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE_APPROVED.getName());
            addLog(returnNotice, OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE_PROCESSED.getName());
        } else {
            returnNotice.setTakeEffectTime(saveOrdReturnNotice.getTakeEffectTime());
            returnNotice.setStatus(OrdDisReturnNoticeStatusEnum.APPROVED.getKey());
            ordDisReturnNoticeMapper.updateByPrimaryKeySelective(returnNotice);
            addLog(returnNotice, OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE_APPROVED.getName());
        }
        return Response.data(returnNotice.getId(), "审核成功");
    }


    private void addLog(OrdDisReturnNotice ordDisReturnNotice, String name) {

        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE.getName(), String.valueOf(ordDisReturnNotice.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE.getCode(), name, new Date(), ordDisReturnNotice.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Response saveOrdReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice) {
        if (CollectionUtils.isEmpty(saveOrdReturnNotice.getGoodsDetailed())) {
            return Response.error("明细不能为空");
        }
        int returnNoticeId;
        if (null != saveOrdReturnNotice.getReturnNoticeOrderId()) {
            returnNoticeId = updateOrdReturnNotice(saveOrdReturnNotice);
        } else {
            returnNoticeId = insertOrdReturnNotice(saveOrdReturnNotice);
        }
        if (returnNoticeId > 0) {
            return Response.data(returnNoticeId, "保存成功");
        }
        return Response.error("保存失败");
    }


    /**
     * 查询退货通知单详情
     *
     * @param ordReturnNoticeIn
     * @return
     */
    @Override
    public OrdBackHeaderReturnNoticeOrderOut findReturnNoticeOrderDetailOutForPage(OrdReturnNoticeDetailIn ordReturnNoticeIn) {
        OrdDisReturnNotice returnNoticeOrder = this.getReturnNoticeOrderById(ordReturnNoticeIn.getReturnNoticeOrderId());
        OrdBackHeaderReturnNoticeOrderOut ordBackHeaderReturnNoticeOrderOut = new OrdBackHeaderReturnNoticeOrderOut();
        BeanUtils.copy(returnNoticeOrder, ordBackHeaderReturnNoticeOrderOut);
        ordBackHeaderReturnNoticeOrderOut.setReturnNoticeOrderId(ordReturnNoticeIn.getReturnNoticeOrderId());
        ordBackHeaderReturnNoticeOrderOut.setTakeEffectTime(null);
        ordBackHeaderReturnNoticeOrderOut.setStatus(OrdDisReturnNoticeStatusEnum.getValueByKey(returnNoticeOrder.getStatus()));
        if (ordBackHeaderReturnNoticeOrderOut.getIsEffectiveImmediately().equals(NumberUtil.INTEGER_ZERO)) {
            ordBackHeaderReturnNoticeOrderOut.setTakeEffectTime(returnNoticeOrder.getTakeEffectTime());
        }
        //查商品和门店
        List<OrdReturnNoticeGoodsOut> goodsOutList = ordReturnNoticeGoodsService.findGoodsStoreInfoByReturnNoticeOrderId(ordReturnNoticeIn);
        ordBackHeaderReturnNoticeOrderOut.setGoodsOutList(goodsOutList);
        return ordBackHeaderReturnNoticeOrderOut;
    }

    /**
     * 根据单号校验退货通知单是否存在以及是否已生效状态
     *
     * @param returnNoticeOrderNo
     * @param bizOrgCode
     * @return
     */
    @Override
    public Response checkReturnNoticeNo(String returnNoticeOrderNo, String bizOrgCode) {
        if (StringUtils.isBlank(returnNoticeOrderNo)) {
            return Response.error("退货通知单号为空");
        }
        OrdDisReturnNotice ordDisReturnNotice = ordDisReturnNoticeMapper.checkReturnNoticeNo(returnNoticeOrderNo, bizOrgCode);
        if (Objects.isNull(ordDisReturnNotice)) {
            return Response.error(returnNoticeOrderNo + "退货通知单不存在");
        }
        if (!ordDisReturnNotice.getStatus().equals(OrdDisReturnNoticeStatusEnum.PROCESSED.getKey())) {
            return Response.error(returnNoticeOrderNo + "退货通知单不是已生效状态");
        }
        return Response.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertOrdReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice) {

        //校验数据
        String errMsg = this.checkData(saveOrdReturnNotice);
        if (StringUtils.isNotEmpty(errMsg)) {
            throw new BusinessException(errMsg);
        }
        OrdDisReturnNotice ordDisReturnNotice = new OrdDisReturnNotice();
        BeanUtils.copy(saveOrdReturnNotice, ordDisReturnNotice);
        ordDisReturnNotice.setSkuCount(saveOrdReturnNotice.getGoodsDetailed().size());
        ordDisReturnNotice.setStatus(OrdDisReturnNoticeStatusEnum.SUBMITTED.getKey());
        if (saveOrdReturnNotice.getIsEffectiveImmediately().equals(NumberUtil.INTEGER_ONE)) {
            ordDisReturnNotice.setTakeEffectTime(LocalDateTime.now());
        } else {
            ordDisReturnNotice.setTakeEffectTime(ordDisReturnNotice.getTakeEffectTime());
        }
        int insertCount = insertReturnNotice(ordDisReturnNotice);
        if (NumberUtil.INTEGER_ZERO < insertCount) {
            //添加数据
            this.insertBatch(saveOrdReturnNotice, ordDisReturnNotice);
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE.getName(), String.valueOf(ordDisReturnNotice.getId()),
                    OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE.getCode(), OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE_SAVE.getName(), new Date(), ordDisReturnNotice.getCreator());

            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return ordDisReturnNotice.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateOrdReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice) {
        Integer returnNoticeOrderId = saveOrdReturnNotice.getReturnNoticeOrderId();
        OrdDisReturnNotice ordDisReturnNotice = ordDisReturnNoticeMapper.selectByPrimaryKey(returnNoticeOrderId);
        if (OrdDisReturnNoticeStatusEnum.APPROVED.getKey().equals(ordDisReturnNotice.getStatus())) {
            throw new BusinessException("当前状态不可操作");
        }
        //校验数据
        String errMsg = this.checkData(saveOrdReturnNotice);
        if (StringUtils.isNotEmpty(errMsg)) {
            throw new BusinessException(errMsg);
        }
        BeanUtils.copy(saveOrdReturnNotice, ordDisReturnNotice);
        ordDisReturnNotice.setSkuCount(saveOrdReturnNotice.getGoodsDetailed().size());
        ordDisReturnNotice.setStatus(OrdDisReturnNoticeStatusEnum.SUBMITTED.getKey());
        int updateCount = updateByPrimaryKeySelective(ordDisReturnNotice);
        if (updateCount > 0) {
            ordReturnNoticeGoodsService.deleteByReturnNoticeOrderId(returnNoticeOrderId);
            ordReturnNoticeStoreService.deleteByReturnNoticeOrderId(returnNoticeOrderId);
            //添加数据
            this.insertBatch(saveOrdReturnNotice, ordDisReturnNotice);
        }
        return ordDisReturnNotice.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertBatch(SaveOrdReturnNotice saveOrdReturnNotice, OrdDisReturnNotice ordDisReturnNotice) {
        List<ReturnGoodsAndStoreIn> goodsDetailedList = saveOrdReturnNotice.getGoodsDetailed();
        for (ReturnGoodsAndStoreIn returnGoodsAndStoreIn : goodsDetailedList) {
            OrdDisReturnNoticeGoods ordDisReturnNoticeGoods = new OrdDisReturnNoticeGoods();
            BeanUtils.copy(returnGoodsAndStoreIn, ordDisReturnNoticeGoods);
            ordDisReturnNoticeGoods.setReturnNoticeOrderId(ordDisReturnNotice.getId());
            ordDisReturnNoticeGoods.setBrand(returnGoodsAndStoreIn.getBrandName());
            ordReturnNoticeGoodsService.insertSelective(ordDisReturnNoticeGoods);
            List<ReturnStoreInfoIn> storeInfoList = returnGoodsAndStoreIn.getStoreInfo();
            for (ReturnStoreInfoIn returnStoreInfoIn : storeInfoList) {
                OrdDisReturnNoticeStore ordDisReturnNoticeStore = new OrdDisReturnNoticeStore();
                ordDisReturnNoticeStore.setReturnNoticeOrderId(ordDisReturnNotice.getId());
                ordDisReturnNoticeStore.setReturnNoticeGoodsId(ordDisReturnNoticeGoods.getId());
                ordDisReturnNoticeStore.setStoreCode(returnStoreInfoIn.getStoreCode());
                ordDisReturnNoticeStore.setStoreName(returnStoreInfoIn.getStoreName());
                ordDisReturnNoticeStore.setReturnDeadline(saveOrdReturnNotice.getReturnDeadline());
                ordDisReturnNoticeStore.setQty(returnStoreInfoIn.getReturnNum());
                ordDisReturnNoticeStore.setClient(returnStoreInfoIn.getClient());
                ordReturnNoticeStoreService.insertSelective(ordDisReturnNoticeStore);
            }
        }
    }

    private String checkData(SaveOrdReturnNotice saveOrdReturnNotice) {
        StringJoiner sj = new StringJoiner(",");
        List<ReturnGoodsAndStoreIn> goodsDetailedList = saveOrdReturnNotice.getGoodsDetailed();
        for (ReturnGoodsAndStoreIn returnGoodsAndStoreIn : goodsDetailedList) {
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setGoodsCode(returnGoodsAndStoreIn.getGoodsCode());
            List<ReturnStoreInfoIn> storeInfoList = returnGoodsAndStoreIn.getStoreInfo();
            if (CollectionUtils.isEmpty(storeInfoList)) {
                sj.add(returnGoodsAndStoreIn.getGoodsCode() + returnGoodsAndStoreIn.getGoodsName() + "未配置关联门店");
            }
            for (ReturnStoreInfoIn returnStoreInfoIn : storeInfoList) {
                orderGoodsIn.setStoreCode(returnStoreInfoIn.getStoreCode());
                orderGoodsIn.setBizOrgCode(saveOrdReturnNotice.getBizOrgCode());
                orderGoodsIn.setIsShelves(NumberUtil.INTEGER_ZERO);
                OrderGoodsOut orderGoods = orderGoodsServer.getOrderGoods(orderGoodsIn);
                if (Objects.isNull(orderGoods)) {
                    sj.add(returnGoodsAndStoreIn.getGoodsCode() + returnGoodsAndStoreIn.getGoodsName() + "不在" + returnStoreInfoIn.getStoreCode() + returnStoreInfoIn.getStoreName() + "的经营或配送范围内");
                }
                if (!OrdReturnNoticeTypeEnum.LIMITED_RETURN.getKey().equals(saveOrdReturnNotice.getReturnType())) {
                    continue;
                }
                if (Objects.isNull(returnStoreInfoIn.getReturnNum())) {
                    sj.add(returnGoodsAndStoreIn.getGoodsCode() + returnGoodsAndStoreIn.getGoodsName() + "可退数量不能为空");
                }
                if (Objects.nonNull(returnStoreInfoIn.getReturnNum()) && returnStoreInfoIn.getReturnNum().compareTo(BigDecimal.ZERO) < 1) {
                    sj.add(returnGoodsAndStoreIn.getGoodsCode() + returnGoodsAndStoreIn.getGoodsName() + "可退数量不能为零或负数");
                }
            }
        }
        return sj.toString();
    }


    @Override
    public OrdDisReturnNotice getReturnNoticeOrderById(Integer returnNoticeOrderId) {
        return ordDisReturnNoticeMapper.selectByPrimaryKey(returnNoticeOrderId);
    }

    /**
     * 根据退货通知单查询可退商品信息(App)
     *
     * @param ordReturnNoticeIn
     * @return
     */
    @Override
    public OrdBackHeaderReturnNoticeOrderOut getReturnNoticeOrderDetailById(OrdReturnNoticeDetailIn ordReturnNoticeIn) {
        OrdBackHeaderReturnNoticeOrderOut returnNoticeOrderDetailOut = this.initBaseReturnNoticeOrder(ordReturnNoticeIn.getReturnNoticeOrderId(), ordReturnNoticeIn.getStoreCode());
        if (null == returnNoticeOrderDetailOut) {
            return null;
        }
        List<OrdReturnNoticeGoodsOut> returnNoticeGoodsInfoOutList = ordDisReturnNoticeMapper.findReturnNoticeGoodsByReturnNoticeOrderId(ordReturnNoticeIn.getReturnNoticeOrderId(), ordReturnNoticeIn.getStoreCode());
        OrdDisReturn returnOrder = null;
        if (null != ordReturnNoticeIn.getReturnOrderId()) {
            returnOrder = new OrdDisReturn();
            returnOrder.setId(ordReturnNoticeIn.getReturnOrderId());
            returnOrder.setBizOrgCode(ordReturnNoticeIn.getBizOrgCode());
            returnOrder.setIsDelete(ModelConst.DELETE.NO);
            returnOrder = ordDisReturnMapper.selectOne(returnOrder);
            if (Objects.isNull(returnOrder)) {
                throw new BusinessException("未找到指定退货单");
            }
        }
        OrdDisReturn finalReturnOrder = returnOrder;
        returnNoticeGoodsInfoOutList.forEach(returnNoticeGoodsInfoOut -> {
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setGoodsCode(returnNoticeGoodsInfoOut.getGoodsCode());
            orderGoodsIn.setStoreCode(ordReturnNoticeIn.getStoreCode());
            orderGoodsIn.setBizOrgCode(ordReturnNoticeIn.getBizOrgCode());
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS_FAST.getType());
            OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
            if (Objects.isNull(orderGoodsOut)) {
                throw new BusinessException(returnNoticeGoodsInfoOut.getGoodsCode() + "不可退");
            }
            returnNoticeGoodsInfoOut.setIsManageValidityPeriod(orderGoodsOut.getIsManageValidityPeriod());
            int isCanReturn = this.initContinueReturnNoticeOrderDetail(finalReturnOrder, orderGoodsOut);
            returnNoticeGoodsInfoOut.setIsCanReturn(isCanReturn);
            BigDecimal stockNum = warehouseServer.getStockNum(ordReturnNoticeIn.getStoreCode(), returnNoticeGoodsInfoOut.getGoodsCode(), ordReturnNoticeIn.getBizOrgCode());
            returnNoticeGoodsInfoOut.setStoreInventory(stockNum);
            if (Objects.isNull(returnNoticeGoodsInfoOut.getQty()) || BigDecimal.ZERO.compareTo(returnNoticeGoodsInfoOut.getQty()) == NumberUtil.INTEGER_ZERO) {
                returnNoticeGoodsInfoOut.setQty(null);
            }
        });
        returnNoticeOrderDetailOut.setGoodsOutList(returnNoticeGoodsInfoOutList);
        return returnNoticeOrderDetailOut;
    }

    @Override
    public boolean isOvertimeForSubmit(Integer returnNoticeOrderId) {
        OrdDisReturnNotice disReturnNotice = new OrdDisReturnNotice();
        disReturnNotice.setId(returnNoticeOrderId);
        disReturnNotice.setIsDelete(0);
        disReturnNotice = ordDisReturnNoticeMapper.selectOne(disReturnNotice);
        if (Objects.isNull(disReturnNotice)) {
            throw new BusinessException("不存在的配销退货通知单");
        }
        return LocalDateTime.now().isAfter(disReturnNotice.getReturnDeadline());
    }

    @Async
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Response<String> importDetail(ImportNoticeDetailIn importNoticeDetailIn) {
        OrdDisReturnNotice noticeOrder = this.getOrSaveReturnNoticeOrderByImport(importNoticeDetailIn);
        if (Objects.isNull(noticeOrder) || ModelConst.DELETE.isDelete(noticeOrder.getIsDelete()) || !importNoticeDetailIn.getBizOrgCode().equals(noticeOrder.getBizOrgCode())) {
            log.error("退货通知单不存在");
            return Response.error("退货通知单不存在");
        }
        if (!OrdDisReturnNoticeStatusEnum.SUBMITTED.getKey().equals(noticeOrder.getStatus())) {
            log.error("退货通知单状态不可导入明细");
            return Response.error("退货通知单状态不可导入明细");
        }
        byte[] bytes = fileService.getFileBytesByFileId(importNoticeDetailIn.getFileId(), SystemConstant.SYSTEM_CODE);
        if (bytes == null) {
            return Response.error("无效的Excel模板");
        }
        // 准备数据
        OrdReturnNoticeDetailIn ordReturnNoticeIn = new OrdReturnNoticeDetailIn();
        ordReturnNoticeIn.setReturnNoticeOrderId(noticeOrder.getId());
        ordReturnNoticeIn.setBizOrgCode(noticeOrder.getBizOrgCode());
        List<OrdReturnNoticeGoodsOut> goodsOutList = ordReturnNoticeGoodsService.findGoodsStoreInfoByReturnNoticeOrderId(ordReturnNoticeIn);
        Map<String, Map<String, String>> detailMap = new HashMap<>();
        Map<String, Integer> goodsIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(goodsOutList)) {
            for (OrdReturnNoticeGoodsOut goodsOut : goodsOutList) {
                Map<String, String> storeCodeMap = CollectionUtils.isEmpty(goodsOut.getStoreInfo()) ? new HashMap<>()
                        : goodsOut.getStoreInfo().stream().collect(Collectors.toMap(OrdReturnNoticeStoreOut::getStoreCode, OrdReturnNoticeStoreOut::getStoreCode));
                detailMap.put(goodsOut.getGoodsCode(), storeCodeMap);
            }
            goodsIdMap = goodsOutList.stream().collect(Collectors.toMap(OrdReturnNoticeGoodsOut::getGoodsCode, OrdReturnNoticeGoodsOut::getId));
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OrdDisReturnListener listener = new OrdDisReturnListener(storeCenterService, orderGoodsServer, noticeOrder.getBizOrgCode(), noticeOrder.getReturnType(), detailMap);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdReturnNoticeDetail.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        List<OrdReturnNoticeGoodsOut> noticeGoodsOuts = listener.getReturnNoticeGoodsDetail();
        if (CollectionUtils.isEmpty(noticeGoodsOuts)) {
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE.getName(), String.valueOf(noticeOrder.getId()),
                    OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE.getCode(), listener.message(), new Date(), "导入");
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.error(listener.message());
        }
        // 保存数据
        for (OrdReturnNoticeGoodsOut goodsOut : noticeGoodsOuts) {
            List<OrdReturnNoticeStoreOut> storeInfoList = goodsOut.getStoreInfo();
            if (CollectionUtils.isEmpty(storeInfoList)) {
                continue;
            }
            OrdDisReturnNoticeGoods ordDisReturnNoticeGoods = new OrdDisReturnNoticeGoods();
            if (goodsIdMap.containsKey(goodsOut.getGoodsCode())) {
                ordDisReturnNoticeGoods.setId(goodsIdMap.get(goodsOut.getGoodsCode()));
            } else {
                BeanUtils.copy(goodsOut, ordDisReturnNoticeGoods);
                ordDisReturnNoticeGoods.setReturnNoticeOrderId(noticeOrder.getId());
                ordDisReturnNoticeGoods.setBrand(goodsOut.getBrandName());
                ordReturnNoticeGoodsService.insertSelective(ordDisReturnNoticeGoods);
            }
            for (OrdReturnNoticeStoreOut storeOut : storeInfoList) {
                OrdDisReturnNoticeStore ordDisReturnNoticeStore = new OrdDisReturnNoticeStore();
                ordDisReturnNoticeStore.setReturnNoticeOrderId(noticeOrder.getId());
                ordDisReturnNoticeStore.setReturnNoticeGoodsId(ordDisReturnNoticeGoods.getId());
                ordDisReturnNoticeStore.setStoreCode(storeOut.getStoreCode());
                ordDisReturnNoticeStore.setStoreName(storeOut.getStoreName());
                ordDisReturnNoticeStore.setReturnDeadline(noticeOrder.getReturnDeadline());
                ordDisReturnNoticeStore.setQty(storeOut.getReturnNum());
                ordDisReturnNoticeStore.setClient(storeOut.getClient());
                ordReturnNoticeStoreService.insertSelective(ordDisReturnNoticeStore);
            }
        }
        noticeOrder.setUpdater(importNoticeDetailIn.getOperator());
        noticeOrder.setSkuCount(ordReturnNoticeGoodsService.count(OrdDisReturnNoticeGoods.builder().returnNoticeOrderId(noticeOrder.getId()).build()));
        ordDisReturnNoticeMapper.updateByPrimaryKeySelective(noticeOrder);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE.getName(), String.valueOf(noticeOrder.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN_NOTICE.getCode(), listener.message(), new Date(), noticeOrder.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success(listener.message());
    }

    @Override
    public String export(Integer noticeOrderId, String bizOrgCode) {
        OrdReturnNoticeDetailIn ordReturnNoticeIn = new OrdReturnNoticeDetailIn();
        ordReturnNoticeIn.setReturnNoticeOrderId(noticeOrderId);
        ordReturnNoticeIn.setBizOrgCode(bizOrgCode);
        List<OrdReturnNoticeGoodsOut> goodsOutList = ordReturnNoticeGoodsService.findGoodsStoreInfoByReturnNoticeOrderId(ordReturnNoticeIn);
        List<ExcelReturnNoticeDetail> excelReturnNoticeDetails = parseToExcel(goodsOutList);
        byte[] bytes = FileExportUtil.getFileBytesByData(excelReturnNoticeDetails, "退货通知单-明细信息", "退货通知单-明细信息", ExcelReturnNoticeDetail.class, true);
        return fileService.uploadFile("退货通知单-明细信息" + ".xlsx", bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    @Override
    public int getUnreturnedCount(String storeCode, String bizOrgCode) {
        return ordDisReturnNoticeMapper.getUnreturnedCount(storeCode, bizOrgCode);
    }

    /**
     * 将查询得到的列表集合转换为导出集合
     *
     * @param list
     * @return
     */
    private List<ExcelReturnNoticeDetail> parseToExcel(List<OrdReturnNoticeGoodsOut> list) {
        int i = 0;
        List<ExcelReturnNoticeDetail> details = new ArrayList<>();
        for (OrdReturnNoticeGoodsOut goodsOut : list) {
            if (CollectionUtils.isEmpty(goodsOut.getStoreInfo())) {
                continue;
            }
            for (OrdReturnNoticeStoreOut store : goodsOut.getStoreInfo()) {
                details.add(convertDtlExcel(goodsOut, store, i));
                i++;
            }
        }
        return details;
    }


    /**
     * 将 OrdReturnNoticeGoodsOut 转换为导出 ExcelReturnOut
     *
     * @param ordReturnNoticeGoodsOut
     * @param index
     * @return
     */
    private ExcelReturnNoticeDetail convertDtlExcel(OrdReturnNoticeGoodsOut ordReturnNoticeGoodsOut, OrdReturnNoticeStoreOut store, int index) {
        ExcelReturnNoticeDetail detail = new ExcelReturnNoticeDetail();
        detail.setIndex(index + 1);
        detail.setGoodsCode(ordReturnNoticeGoodsOut.getGoodsCode());
        detail.setGoodsName(ordReturnNoticeGoodsOut.getGoodsName());
        detail.setSpecification(ordReturnNoticeGoodsOut.getSpecification());
        detail.setSortName(ordReturnNoticeGoodsOut.getSortName());
        detail.setStoreCode(store.getStoreCode());
        detail.setStoreName(store.getStoreName());
        detail.setReturnQty(store.getReturnNum());
        return detail;
    }


    /**
     * 获取表头信息，如没有则新增
     * @param importNoticeDetailIn
     * @return
     */
    private OrdDisReturnNotice getOrSaveReturnNoticeOrderByImport(ImportNoticeDetailIn importNoticeDetailIn) {
        OrdDisReturnNotice noticeOrder;
        if (Objects.isNull(importNoticeDetailIn.getNoticeOrderId())) {
            noticeOrder = new OrdDisReturnNotice();
            BeanUtils.copy(importNoticeDetailIn, noticeOrder);
            noticeOrder.setSkuCount(NumberUtils.INTEGER_ZERO);
            noticeOrder.setStatus(OrdDisReturnNoticeStatusEnum.SUBMITTED.getKey());
            if (importNoticeDetailIn.getIsEffectiveImmediately().equals(NumberUtil.INTEGER_ONE)) {
                noticeOrder.setTakeEffectTime(LocalDateTime.now());
            } else {
                noticeOrder.setTakeEffectTime(noticeOrder.getTakeEffectTime());
            }
            noticeOrder.setUpdater(importNoticeDetailIn.getOperator());
            noticeOrder.setCreator(importNoticeDetailIn.getOperator());
            insertReturnNotice(noticeOrder);
        } else {
            noticeOrder = ordDisReturnNoticeMapper.selectByPrimaryKey(importNoticeDetailIn.getNoticeOrderId());
        }
        return noticeOrder;
    }

    private int initContinueReturnNoticeOrderDetail(OrdDisReturn returnOrder, OrderGoodsOut orderGoodsOut) {
        if (Objects.isNull(returnOrder)) {
            return 1;
        }
        // 根据商品物流仓位查找对应对货仓位(退货仓位)
        StockInfoOut stockInfoOut = stockServer.getTransInfo(orderGoodsOut.getBackStockCode());
        if (null == stockInfoOut || StringUtils.isEmpty(stockInfoOut.getStockCode())) {
            throw new BusinessException(orderGoodsOut.getGoodsCode() + "退货仓位不存在");
        }
        String returnPositionCode = stockInfoOut.getStockCode();
        return returnPositionCode.equals(returnOrder.getStockCode()) ? 1 : 0;
    }

    private OrdBackHeaderReturnNoticeOrderOut initBaseReturnNoticeOrder(Integer returnNoticeOrderId, String storeCode) {
        OrdBackHeaderReturnNoticeOrderOut baseReturnNoticeOrder = ordDisReturnNoticeMapper.selectOneByIdAndStoreCode(returnNoticeOrderId, storeCode);
        if (null == baseReturnNoticeOrder) {
            return null;
        }
        baseReturnNoticeOrder.setReturnTypeValue(OrdReturnNoticeTypeEnum.getValueByKey(baseReturnNoticeOrder.getReturnType()));
        return baseReturnNoticeOrder;
    }

}
