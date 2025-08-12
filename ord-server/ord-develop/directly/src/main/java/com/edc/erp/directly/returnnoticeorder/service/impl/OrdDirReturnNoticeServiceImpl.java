package com.edc.erp.directly.returnnoticeorder.service.impl;

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
import com.edc.erp.common.util.FileExportUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNotice;
import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNoticeGoods;
import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNoticeStore;
import com.edc.erp.directly.returnnoticeorder.enumeration.OrdReturnNoticeStatusEnum;
import com.edc.erp.directly.returnnoticeorder.enumeration.OrdReturnNoticeTypeEnum;
import com.edc.erp.directly.returnnoticeorder.listener.OrdDirReturnGoodsListener;
import com.edc.erp.directly.returnnoticeorder.listener.OrdDirReturnListener;
import com.edc.erp.directly.returnnoticeorder.listener.OrdDirReturnStoreListener;
import com.edc.erp.directly.returnnoticeorder.mapper.OrdDirReturnNoticeMapper;
import com.edc.erp.directly.returnnoticeorder.mapper.OrdDirReturnNoticeStoreMapper;
import com.edc.erp.directly.returnnoticeorder.model.excel.ExcelReturnNoticeDetail;
import com.edc.erp.directly.returnnoticeorder.model.excel.ImportOrdReturnNoticeGoods;
import com.edc.erp.directly.returnnoticeorder.model.excel.ImportOrdReturnNoticeStore;
import com.edc.erp.directly.returnnoticeorder.model.in.*;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeOrderDetailOut;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeOrderOut;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.erp.directly.returnnoticeorder.service.OrdDirReturnNoticeGoodsService;
import com.edc.erp.directly.returnnoticeorder.service.OrdDirReturnNoticeService;
import com.edc.erp.directly.returnnoticeorder.service.OrdDirReturnNoticeStoreService;
import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
import com.edc.erp.directly.returnorder.mapper.OrdDirReturnMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 退货通知单表(OrdDirReturnNotice)表服务实现类
 *
 * @author
 * @since 2022-11-18 19:09:03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdDirReturnNoticeServiceImpl extends BaseServiceImpl<OrdDirReturnNotice> implements OrdDirReturnNoticeService {

    private final OrdDirReturnNoticeMapper ordDirReturnNoticeMapper;

    private final OrdDirReturnNoticeGoodsService ordDirReturnNoticeGoodsService;

    @Autowired
    private OrdDirReturnNoticeStoreService ordDirReturnNoticeStoreService;

    private final UniqueUtils uniqueUtils;

    private final AsyncLogService asyncLogService;

    private final OrderGoodsServer orderGoodsServer;

    private final FileService fileService;

    private final StoreCenterService storeCenterService;

    private final OrdDirReturnNoticeStoreMapper ordDirReturnNoticeStoreMapper;

    private final EquipmentBusinessReasonServer equipmentBusinessReasonServer;

    @Autowired
    private OrdDirReturnMapper ordDirReturnMapper;

    private final WarehouseServer warehouseServer;

    private final StockServer stockServer;

    /**
     * 分页查询退货单列表
     *
     * @param ordReturnNoticeIn
     * @return
     */
    @Override
    public Page<OrdReturnNoticeOrderOut> findReturnNoticeOrderOutForPage(OrdReturnNoticeIn ordReturnNoticeIn) {
        List<OrdReturnNoticeOrderOut> ordReturnNoticeOrderOuts = ordDirReturnNoticeMapper.findOrdReturnNoticeByPage(ordReturnNoticeIn);
        ordReturnNoticeOrderOuts.forEach(orderOut -> {
            orderOut.setReturnTypeValue(OrdReturnNoticeTypeEnum.getValueByKey(orderOut.getReturnType()));
            orderOut.setStatusStr(OrdReturnNoticeStatusEnum.getValueByKey(orderOut.getStatus()));
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

    /**
     * 分页查询退货通知单详情
     *
     * @param ordReturnNoticeIn
     * @return
     */
    @Override
    public OrdReturnNoticeOrderDetailOut findReturnNoticeOrderDetailOutForPage(OrdReturnNoticeDetailIn ordReturnNoticeIn) {
        OrdDirReturnNotice returnNoticeOrder = this.getReturnNoticeOrderById(ordReturnNoticeIn.getReturnNoticeOrderId());
        OrdReturnNoticeOrderDetailOut ordReturnNoticeOrderDetailOut = new OrdReturnNoticeOrderDetailOut();
        BeanUtils.copy(returnNoticeOrder, ordReturnNoticeOrderDetailOut);
        ordReturnNoticeOrderDetailOut.setReturnNoticeOrderId(ordReturnNoticeIn.getReturnNoticeOrderId());
        ordReturnNoticeOrderDetailOut.setTakeEffectTime(null);
        ordReturnNoticeOrderDetailOut.setStatus(OrdReturnNoticeStatusEnum.getValueByKey(returnNoticeOrder.getStatus()));
        if (NumberUtil.INTEGER_ZERO.equals(ordReturnNoticeOrderDetailOut.getIsEffectiveImmediately())) {
            ordReturnNoticeOrderDetailOut.setTakeEffectTime(returnNoticeOrder.getTakeEffectTime());
        }
        //查商品和门店
        List<OrdReturnNoticeGoodsOut> goodsOutList = ordDirReturnNoticeGoodsService.findGoodsStoreInfoByReturnNoticeOrderId(ordReturnNoticeIn);
        ordReturnNoticeOrderDetailOut.setGoodsOutList(goodsOutList);
        return ordReturnNoticeOrderDetailOut;
    }

    /**
     * 保存或修改退货通知单及明细
     *
     * @param saveOrdReturnNotice
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Response saveOrUpdateOrdReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice) {
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
     * 审核退货通知单
     *
     * @param saveOrdReturnNotice
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response auditOrdDirReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice) {
        OrdDirReturnNotice ordDirReturnNotice = this.getReturnNoticeOrderById(saveOrdReturnNotice.getReturnNoticeOrderId());
        if (null == ordDirReturnNotice) {
            return Response.error("无效的退货通知单");
        }
        if (OrdReturnNoticeStatusEnum.INVALID.getKey().equals(ordDirReturnNotice.getStatus())) {
            return Response.error("退货通知单已作废");
        }
        if (OrdReturnNoticeStatusEnum.PROCESSED.getKey().equals(ordDirReturnNotice.getStatus())) {
            return Response.error("退货通知单已生效");
        }
        if (OrdReturnNoticeStatusEnum.APPROVED.getKey().equals(ordDirReturnNotice.getStatus())) {
            return Response.error("退货通知单已审核");
        }

        if (CollectionUtils.isEmpty(saveOrdReturnNotice.getGoodsDetailed())) {
            return Response.error("明细列表不能为空");
        }
        //添加数据
        this.saveOrUpdateOrdReturnNotice(saveOrdReturnNotice);
        /*  List<ReturnGoodsAndStoreIn> goodsDetailedList = saveOrdReturnNotice.getGoodsDetailed();
          List<OrdDirReturnNoticeGoods> ordDisReturnNoticeGoodsList=ordDirReturnNoticeGoodsService.findByReturnNoticeId(saveOrdReturnNotice.getReturnNoticeOrderId());
          //商品取交集
          List<ReturnGoodsAndStoreIn> equalGoodsCodeList = goodsDetailedList.stream().filter(goodsDetailed ->
                  ordDisReturnNoticeGoodsList.stream().map(ordDisReturnNoticeGoods -> ordDisReturnNoticeGoods.getGoodsCode()).collect(Collectors.toList()).contains(goodsDetailed.getGoodsCode())
          ).collect(Collectors.toList());
          //门店取交集
          List<ReturnStoreInfoIn> storeInfoList = new ArrayList<>();
          List<ReturnStoreInfoIn> equalStoreCodeList = new ArrayList<>();
          List<ReturnGoodsAndStoreIn> list =new ArrayList<>();
          for (ReturnGoodsAndStoreIn returnGoodsAndStoreIn : equalGoodsCodeList) {
               storeInfoList = returnGoodsAndStoreIn.getStoreInfo();
               List<OrdReturnNoticeStoreOut> ordReturnNoticeStoreOutList = ordDirReturnNoticeStoreService.findByReturnNoticeIdAndReturnGoodsId(saveOrdReturnNotice.getReturnNoticeOrderId(), returnGoodsAndStoreIn.getId());
               equalStoreCodeList = storeInfoList.stream().filter(storeInfo ->
                       ordReturnNoticeStoreOutList.stream().map(ordReturnNoticeStoreOut -> ordReturnNoticeStoreOut.getStoreCode()).collect(Collectors.toList()).contains(storeInfo.getStoreCode())
               ).collect(Collectors.toList());


               if(equalStoreCodeList.size()!=storeInfoList.size()){
                    returnGoodsAndStoreIn.setStoreInfo(equalStoreCodeList);
                    returnGoodsAndStoreIn.setGoodsCode(returnGoodsAndStoreIn.getGoodsCode());
               }
               list.add(returnGoodsAndStoreIn);
          }
          if(list.size()!=goodsDetailedList.size() || equalStoreCodeList.size()!=storeInfoList.size()){
               saveOrdReturnNotice.setGoodsDetailed(list);
               return Response.data(saveOrdReturnNotice);
          }
*/
        OrdDirReturnNotice returnNotice = ordDirReturnNoticeMapper.selectByPrimaryKey(saveOrdReturnNotice.getReturnNoticeOrderId());
        if (NumberUtil.INTEGER_ONE.equals(saveOrdReturnNotice.getIsEffectiveImmediately())) {
            returnNotice.setTakeEffectTime(LocalDateTime.now());
            returnNotice.setStatus(OrdReturnNoticeStatusEnum.PROCESSED.getKey());
            ordDirReturnNoticeMapper.updateByPrimaryKeySelective(returnNotice);
            addLog(returnNotice, OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE_APPROVED.getName());
            addLog(returnNotice, OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE_PROCESSED.getName());
        } else {
            returnNotice.setTakeEffectTime(saveOrdReturnNotice.getTakeEffectTime());
            returnNotice.setStatus(OrdReturnNoticeStatusEnum.APPROVED.getKey());
            ordDirReturnNoticeMapper.updateByPrimaryKeySelective(returnNotice);
            addLog(returnNotice, OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE_APPROVED.getName());
        }

        return Response.data(returnNotice.getId(), "审核成功");
    }


    private void addLog(OrdDirReturnNotice ordDirReturnNotice, String name) {
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getName(), String.valueOf(ordDirReturnNotice.getId()),
                OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getCode(), name, new Date(), ordDirReturnNotice.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 作废退货通知单
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response invalidatedOrdReturnNotice(Integer id) {
        OrdDirReturnNotice ordDisReturnNotice = this.getReturnNoticeOrderById(id);
        if (null == ordDisReturnNotice) {
            return Response.error("无效的退货通知单");
        }
        if (OrdReturnNoticeStatusEnum.INVALID.getKey().equals(ordDisReturnNotice.getStatus())) {
            return Response.error("退货通知单已作废");
        }
        if (OrdReturnNoticeStatusEnum.PROCESSED.getKey().equals(ordDisReturnNotice.getStatus())) {
            return Response.error("退货通知单已生效");
        }
        ordDisReturnNotice.setStatus(OrdReturnNoticeStatusEnum.INVALID.getKey());
        ordDirReturnNoticeMapper.updateByPrimaryKeySelective(ordDisReturnNotice);

        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getName(), String.valueOf(ordDisReturnNotice.getId()),
                OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getCode(), OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE_INVALID.getName(), new Date(), ordDisReturnNotice.getCreator());

        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.data(ordDisReturnNotice.getId(), "作废成功");
    }

    /**
     * 导入商品信息
     *
     * @param fileId
     * @param bizOrgCode
     * @return
     */
    @Override
    public Response<List<OrdReturnNoticeGoodsOut>> importReturnNoticeGoods(String fileId, List<String> goodsCodes, String bizOrgCode) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (bytes == null) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OrdDirReturnGoodsListener listener = new OrdDirReturnGoodsListener(orderGoodsServer, goodsCodes, bizOrgCode);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdReturnNoticeGoods.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        return Response.data(listener.getReturnNoticeGoodsDetail(), listener.message());
    }

    /**goo
     * 导入门店信息
     *
     * @param fileId
     * @param bizOrgCode
     * @param returnType
     * @return
     */
    @Override
    public Response<List<OrdReturnNoticeStoreOut>> importReturnNoticeStore(String fileId, String bizOrgCode, String returnType) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (bytes == null) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OrdDirReturnStoreListener listener = new OrdDirReturnStoreListener(storeCenterService, bizOrgCode, returnType);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdReturnNoticeStore.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        return Response.data(listener.getReturnNoticeStoreDetail(), listener.message());
    }


    /**
     * 第一次保存
     *
     * @param saveOrdReturnNotice
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer insertOrdReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice) {
        //校验数据
        String errMsg = this.checkData(saveOrdReturnNotice);
        if (StringUtils.isNotEmpty(errMsg)) {
            throw new BusinessException(errMsg);
        }
        OrdDirReturnNotice ordDirReturnNotice = new OrdDirReturnNotice();
        BeanUtils.copy(saveOrdReturnNotice, ordDirReturnNotice);
        ordDirReturnNotice.setSkuCount(saveOrdReturnNotice.getGoodsDetailed().size());
        ordDirReturnNotice.setStatus(OrdReturnNoticeStatusEnum.SUBMITTED.getKey());
        if (saveOrdReturnNotice.getIsEffectiveImmediately().equals(NumberUtil.INTEGER_ONE)) {
            ordDirReturnNotice.setTakeEffectTime(LocalDateTime.now());
        } else {
            ordDirReturnNotice.setTakeEffectTime(ordDirReturnNotice.getTakeEffectTime());
        }
        int insertCount = insertReturnNotice(ordDirReturnNotice);
        if (NumberUtil.INTEGER_ZERO < insertCount) {
            //添加数据
            this.insertBatch(saveOrdReturnNotice, ordDirReturnNotice);
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getName(), String.valueOf(ordDirReturnNotice.getId()),
                    OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getCode(), OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE_SAVE.getName(), new Date(), ordDirReturnNotice.getCreator());

            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return ordDirReturnNotice.getId();
    }

    /**
     * 非第一次保存
     *
     * @param saveOrdReturnNotice
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateOrdReturnNotice(SaveOrdReturnNotice saveOrdReturnNotice) {

        Integer returnNoticeOrderId = saveOrdReturnNotice.getReturnNoticeOrderId();
        OrdDirReturnNotice ordDirReturnNotice = ordDirReturnNoticeMapper.selectByPrimaryKey(returnNoticeOrderId);
        if (OrdReturnNoticeStatusEnum.APPROVED.getKey().equals(ordDirReturnNotice.getStatus())) {
            throw new BusinessException("当前状态不可操作");
        }
        //校验数据
        String errMsg = this.checkData(saveOrdReturnNotice);
        if (StringUtils.isNotEmpty(errMsg)) {
            throw new BusinessException(errMsg);
        }
        BeanUtils.copy(saveOrdReturnNotice, ordDirReturnNotice);
        ordDirReturnNotice.setSkuCount(saveOrdReturnNotice.getGoodsDetailed().size());
        ordDirReturnNotice.setStatus(OrdReturnNoticeStatusEnum.SUBMITTED.getKey());
        int updateCount = updateByPrimaryKeySelective(ordDirReturnNotice);
        if (updateCount > 0) {
            ordDirReturnNoticeGoodsService.deleteByReturnNoticeOrderId(returnNoticeOrderId);
            ordDirReturnNoticeStoreService.deleteByReturnNoticeOrderId(returnNoticeOrderId);
            //添加数据
            this.insertBatch(saveOrdReturnNotice, ordDirReturnNotice);
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getName(), String.valueOf(ordDirReturnNotice.getId()),
                OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getCode(), OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE_UPDATE.getName(), new Date(), ordDirReturnNotice.getCreator());

        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return ordDirReturnNotice.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertBatch(SaveOrdReturnNotice saveOrdReturnNotice, OrdDirReturnNotice ordDirReturnNotice) {
        List<ReturnGoodsAndStoreIn> goodsDetailedList = saveOrdReturnNotice.getGoodsDetailed();
        for (ReturnGoodsAndStoreIn returnGoodsAndStoreIn : goodsDetailedList) {
            OrdDirReturnNoticeGoods ordDirReturnNoticeGoods = new OrdDirReturnNoticeGoods();
            BeanUtils.copy(returnGoodsAndStoreIn, ordDirReturnNoticeGoods);
            ordDirReturnNoticeGoods.setReturnNoticeOrderId(ordDirReturnNotice.getId());
            ordDirReturnNoticeGoods.setBrand(returnGoodsAndStoreIn.getBrandName());
            ordDirReturnNoticeGoodsService.insertSelective(ordDirReturnNoticeGoods);
            List<ReturnStoreInfoIn> storeInfoList = returnGoodsAndStoreIn.getStoreInfo();
            for (ReturnStoreInfoIn returnStoreInfoIn : storeInfoList) {
                OrdDirReturnNoticeStore ordDirReturnNoticeStore = new OrdDirReturnNoticeStore();
                ordDirReturnNoticeStore.setReturnNoticeOrderId(ordDirReturnNotice.getId());
                ordDirReturnNoticeStore.setReturnNoticeGoodsId(ordDirReturnNoticeGoods.getId());
                ordDirReturnNoticeStore.setStoreCode(returnStoreInfoIn.getStoreCode());
                ordDirReturnNoticeStore.setStoreName(returnStoreInfoIn.getStoreName());
                ordDirReturnNoticeStore.setReturnDeadline(saveOrdReturnNotice.getReturnDeadline());
                ordDirReturnNoticeStore.setQty(returnStoreInfoIn.getQty());
                ordDirReturnNoticeStoreService.insertSelective(ordDirReturnNoticeStore);
            }
        }
    }

    /**
     * 校验数据
     *
     * @param saveOrdReturnNotice
     * @return
     */
    private String checkData(SaveOrdReturnNotice saveOrdReturnNotice) {
        StringJoiner sj = new StringJoiner(SystemConstant.COMMA);
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
                if (Objects.isNull(returnStoreInfoIn.getQty())) {
                    sj.add(returnGoodsAndStoreIn.getGoodsCode() + returnGoodsAndStoreIn.getGoodsName() + "可退数量不能为空");
                }
                if (Objects.nonNull(returnStoreInfoIn.getQty()) && returnStoreInfoIn.getQty().compareTo(BigDecimal.ZERO) < 1) {
                    sj.add(returnGoodsAndStoreIn.getGoodsCode() + returnGoodsAndStoreIn.getGoodsName() + "可退数量不能为零或负数");
                }
            }
        }
        return sj.toString();
    }

    /**
     * 添加一个退货通知单
     *
     * @param ordDirReturnNotice
     * @return
     */
    private int insertReturnNotice(OrdDirReturnNotice ordDirReturnNotice) {
        ordDirReturnNotice.setIsDelete(ModelConst.DELETE.NO);
        ordDirReturnNotice.setReturnNoticeOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.TT.getCode(), ordDirReturnNotice.getBizOrgCode(), uniqueUtils, 4));
        return ordDirReturnNoticeMapper.insertSelective(ordDirReturnNotice);
    }


    /**
     * 根据退货通知单id查通知单
     *
     * @param id
     * @return
     */
    @Override
    public OrdDirReturnNotice getReturnNoticeOrderById(Integer id) {
        return ordDirReturnNoticeMapper.selectByPrimaryKey(id);
    }

    @Override
    public BigDecimal getMaxQtyByParameter(String goodsCode, String storeCode, Integer id) {
        BigDecimal maxQuantity = ordDirReturnNoticeStoreMapper.getMaxQtyByParameter(goodsCode, storeCode, id);
        return null != maxQuantity ? maxQuantity : BigDecimal.ZERO;
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
        OrdDirReturnNotice ordDirReturnNotice = ordDirReturnNoticeMapper.checkReturnNoticeNo(returnNoticeOrderNo, bizOrgCode);
        if (Objects.isNull(ordDirReturnNotice)) {
            return Response.error(returnNoticeOrderNo + "退货通知单不存在");
        }
        if (!ordDirReturnNotice.getStatus().equals(OrdReturnNoticeStatusEnum.PROCESSED.getKey())) {
            return Response.error(returnNoticeOrderNo + "退货通知单不是已生效状态");
        }
        return Response.success();
    }

    /**
     * 根据退货通知单查询可退商品信息
     *
     * @param ordReturnNoticeIn
     * @return
     */
    @Override
    public OrdReturnNoticeOrderDetailOut getReturnNoticeOrderDetailById(OrdReturnNoticeDetailIn ordReturnNoticeIn) {

        OrdReturnNoticeOrderDetailOut returnNoticeOrderDetailOut = this.initBaseReturnNoticeOrder(ordReturnNoticeIn.getReturnNoticeOrderId(), ordReturnNoticeIn.getStoreCode());
        if (null == returnNoticeOrderDetailOut) {
            return null;
        }
        List<OrdReturnNoticeGoodsOut> returnNoticeGoodsInfoOutList = ordDirReturnNoticeMapper.findReturnNoticeGoodsByReturnNoticeOrderId(ordReturnNoticeIn.getReturnNoticeOrderId(), ordReturnNoticeIn.getStoreCode());
        OrdDirReturn returnOrder = null;
        if (null != ordReturnNoticeIn.getReturnOrderId()) {
            returnOrder = getReturnOrderByIdAndOrgCode(ordReturnNoticeIn.getReturnOrderId(), ordReturnNoticeIn.getBizOrgCode());
            if (Objects.isNull(returnOrder)) {
                throw new BusinessException("未找到指定退货单");
            }
        }
        OrdDirReturn finalReturnOrder = returnOrder;
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
        OrdDirReturnNotice dirReturnNotice = new OrdDirReturnNotice();
        dirReturnNotice.setId(returnNoticeOrderId);
        dirReturnNotice.setIsDelete(0);
        dirReturnNotice = ordDirReturnNoticeMapper.selectOne(dirReturnNotice);
        if (Objects.isNull(dirReturnNotice)) {
            throw new BusinessException("不存在的直营退货通知单");
        }
        return LocalDateTime.now().isAfter(dirReturnNotice.getReturnDeadline());
    }

    @Async
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Response<String> importDetail(ImportNoticeDetailIn importNoticeDetailIn) {
        OrdDirReturnNotice noticeOrder = this.getOrSaveReturnNoticeOrderByImport(importNoticeDetailIn);
        if (Objects.isNull(noticeOrder) || ModelConst.DELETE.isDelete(noticeOrder.getIsDelete()) || !importNoticeDetailIn.getBizOrgCode().equals(noticeOrder.getBizOrgCode())) {
            log.error("退货通知单不存在");
            return Response.error("退货通知单不存在");
        }
        if (!OrdReturnNoticeStatusEnum.SUBMITTED.getKey().equals(noticeOrder.getStatus())) {
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
        List<OrdReturnNoticeGoodsOut> goodsOutList = ordDirReturnNoticeGoodsService.findGoodsStoreInfoByReturnNoticeOrderId(ordReturnNoticeIn);
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
        OrdDirReturnListener listener = new OrdDirReturnListener(storeCenterService, orderGoodsServer, noticeOrder.getBizOrgCode(), noticeOrder.getReturnType(), detailMap);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdReturnNoticeDetail.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        List<OrdReturnNoticeGoodsOut> noticeGoodsOuts = listener.getReturnNoticeGoodsDetail();
        if (CollectionUtils.isEmpty(noticeGoodsOuts)) {
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getName(), String.valueOf(noticeOrder.getId()),
                    OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getCode(), listener.message(), new Date(), "导入");
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.error(listener.message());
        }
        // 保存数据
        for (OrdReturnNoticeGoodsOut goodsOut : noticeGoodsOuts) {
            List<OrdReturnNoticeStoreOut> storeInfoList = goodsOut.getStoreInfo();
            if (CollectionUtils.isEmpty(storeInfoList)) {
                continue;
            }
            OrdDirReturnNoticeGoods ordDisReturnNoticeGoods = new OrdDirReturnNoticeGoods();
            if (goodsIdMap.containsKey(goodsOut.getGoodsCode())) {
                ordDisReturnNoticeGoods.setId(goodsIdMap.get(goodsOut.getGoodsCode()));
            } else {
                BeanUtils.copy(goodsOut, ordDisReturnNoticeGoods);
                ordDisReturnNoticeGoods.setReturnNoticeOrderId(noticeOrder.getId());
                ordDisReturnNoticeGoods.setBrand(goodsOut.getBrandName());
                ordDirReturnNoticeGoodsService.insertSelective(ordDisReturnNoticeGoods);
            }
            for (OrdReturnNoticeStoreOut storeOut : storeInfoList) {
                OrdDirReturnNoticeStore ordDisReturnNoticeStore = new OrdDirReturnNoticeStore();
                ordDisReturnNoticeStore.setReturnNoticeOrderId(noticeOrder.getId());
                ordDisReturnNoticeStore.setReturnNoticeGoodsId(ordDisReturnNoticeGoods.getId());
                ordDisReturnNoticeStore.setStoreCode(storeOut.getStoreCode());
                ordDisReturnNoticeStore.setStoreName(storeOut.getStoreName());
                ordDisReturnNoticeStore.setReturnDeadline(noticeOrder.getReturnDeadline());
                ordDisReturnNoticeStore.setQty(storeOut.getReturnNum());
                ordDirReturnNoticeStoreService.insertSelective(ordDisReturnNoticeStore);
            }
        }
        noticeOrder.setUpdater(importNoticeDetailIn.getOperator());
        noticeOrder.setSkuCount(ordDirReturnNoticeGoodsService.count(OrdDirReturnNoticeGoods.builder().returnNoticeOrderId(noticeOrder.getId()).build()));
        ordDirReturnNoticeMapper.updateByPrimaryKeySelective(noticeOrder);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getName(), String.valueOf(noticeOrder.getId()),
                OrdLogTypeEnum.ORD_DIR_RETURN_NOTICE.getCode(), listener.message(), new Date(), noticeOrder.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success(listener.message());
    }

    @Override
    public String export(Integer noticeOrderId, String bizOrgCode) {
        OrdReturnNoticeDetailIn ordReturnNoticeIn = new OrdReturnNoticeDetailIn();
        ordReturnNoticeIn.setReturnNoticeOrderId(noticeOrderId);
        ordReturnNoticeIn.setBizOrgCode(bizOrgCode);
        List<OrdReturnNoticeGoodsOut> goodsOutList = ordDirReturnNoticeGoodsService.findGoodsStoreInfoByReturnNoticeOrderId(ordReturnNoticeIn);
        List<ExcelReturnNoticeDetail> excelReturnNoticeDetails = parseToExcel(goodsOutList);
        byte[] bytes = FileExportUtil.getFileBytesByData(excelReturnNoticeDetails, "退货通知单-明细信息", "退货通知单-明细信息", ExcelReturnNoticeDetail.class, true);
        return fileService.uploadFile("退货通知单-明细信息" + ".xlsx", bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    @Override
    public int getUnreturnedCount(String storeCode, String bizOrgCode) {
        return ordDirReturnNoticeMapper.getUnreturnedCount(storeCode, bizOrgCode);
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
        detail.setReturnQty(store.getQty());
        return detail;
    }

    /**
     * 获取表头信息，如没有则新增
     * @param importNoticeDetailIn
     * @return
     */
    private OrdDirReturnNotice getOrSaveReturnNoticeOrderByImport(ImportNoticeDetailIn importNoticeDetailIn) {
        OrdDirReturnNotice noticeOrder;
        if (Objects.isNull(importNoticeDetailIn.getNoticeOrderId())) {
            noticeOrder = new OrdDirReturnNotice();
            BeanUtils.copy(importNoticeDetailIn, noticeOrder);
            noticeOrder.setSkuCount(NumberUtils.INTEGER_ZERO);
            noticeOrder.setStatus(OrdReturnNoticeStatusEnum.SUBMITTED.getKey());
            if (importNoticeDetailIn.getIsEffectiveImmediately().equals(NumberUtil.INTEGER_ONE)) {
                noticeOrder.setTakeEffectTime(LocalDateTime.now());
            } else {
                noticeOrder.setTakeEffectTime(noticeOrder.getTakeEffectTime());
            }
            noticeOrder.setCreator(importNoticeDetailIn.getOperator());
            noticeOrder.setUpdater(importNoticeDetailIn.getOperator());
            insertReturnNotice(noticeOrder);
        } else {
            noticeOrder = ordDirReturnNoticeMapper.selectByPrimaryKey(importNoticeDetailIn.getNoticeOrderId());
        }
        return noticeOrder;
    }

    private int initContinueReturnNoticeOrderDetail(OrdDirReturn returnOrder, OrderGoodsOut orderGoodsOut) {
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

    private OrdReturnNoticeOrderDetailOut initBaseReturnNoticeOrder(Integer returnNoticeOrderId, String storeCode) {
        OrdReturnNoticeOrderDetailOut baseReturnNoticeOrder = ordDirReturnNoticeMapper.selectOneByIdAndStoreCode(returnNoticeOrderId, storeCode);
        if (null == baseReturnNoticeOrder) {
            return null;
        }
        baseReturnNoticeOrder.setReturnTypeValue(OrdReturnNoticeTypeEnum.getValueByKey(baseReturnNoticeOrder.getReturnType()));
        return baseReturnNoticeOrder;
    }

    public OrdDirReturn getReturnOrderByIdAndOrgCode(Integer returnOrderId, String bizOrgCode) {
        OrdDirReturn returnOrder = new OrdDirReturn();
        returnOrder.setId(returnOrderId);
        returnOrder.setBizOrgCode(bizOrgCode);
        returnOrder.setIsDelete(ModelConst.DELETE.NO);
        return ordDirReturnMapper.selectOne(returnOrder);
    }
}
