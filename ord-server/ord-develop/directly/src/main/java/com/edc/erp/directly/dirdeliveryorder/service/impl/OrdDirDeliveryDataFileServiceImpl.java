package com.edc.erp.directly.dirdeliveryorder.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.enumeration.OrderDeliverFileBusinessTypeEnum;
import com.edc.erp.common.enumeration.OrderDeliverFileTypeEnum;
import com.edc.erp.common.excel.ExportOrderDelivryDataFile;
import com.edc.erp.common.excel.ExportOrderReturnDataFile;
import com.edc.erp.common.excel.ImportOrderDeliveryDataFile;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.OrderDeliveryDataFileService;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.OrderDelivryDataFileUtil;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.model.in.UpdateDirDeliveryDetailIn;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirDeliveryOut;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryDataFileService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
import com.edc.erp.directly.returnorder.enumeration.OrdReturnOrderStatusEnum;
import com.edc.erp.directly.returnorder.service.OrdDirReturnDetailService;
import com.edc.erp.directly.returnorder.service.OrdDirReturnService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 配货、配货退货文件数据上传处理
 */
@Service
@Slf4j
public class OrdDirDeliveryDataFileServiceImpl implements OrdDirDeliveryDataFileService {

    @Autowired
    private OrderDeliveryDataFileService orderDeliveryDataFileService;

    @Autowired
    private FileService fileService;

    @Autowired
    private OrdDirDeliveryService ordDirDeliveryService;

    @Autowired
    private OrdDirReturnService ordDirReturnService;

    @Autowired
    private OrdDirReturnDetailService ordDirReturnDetailService;

    @Autowired
    private StockServer stockServer;

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public boolean execFile(OrdDeliveryDataFile ordDeliveryDataFile) {
        if (StringUtils.isEmpty(ordDeliveryDataFile.getFileId())) {
            log.error("没有查询到导入的文件信息");
            return true;
//            return "没有查询到导入的文件信息";
        }
        if (!ordDeliveryDataFile.getBusinessType().equals(OrderDeliverFileBusinessTypeEnum.DIR.getCode())) {
//            return "类型不正确";
            log.error("{}类型不正确", ordDeliveryDataFile.getFileId());
            return true;
        }
        byte[] fileBytes = fileService.getFileBytesByFileId(ordDeliveryDataFile.getFileId(), SystemConstant.SYSTEM_CODE);
        List<ImportOrderDeliveryDataFile> fileDatas = OrderDelivryDataFileUtil.parseImportFile2List(fileBytes);
        return execAndOverDataFile(fileDatas, ordDeliveryDataFile);
    }

    private boolean execAndOverDataFile(List<ImportOrderDeliveryDataFile> fileDatas, OrdDeliveryDataFile ordDeliveryDataFile) {
        if (CollectionUtils.isEmpty(fileDatas)) {
            ordDeliveryDataFile.setRemark("失败：导入内容中无有效数据");
        } else {
            boolean isExecSuccess = this.execDirFile(fileDatas, ordDeliveryDataFile);
            String remark = ordDeliveryDataFile.getRemark();
            ordDeliveryDataFile.setRemark(isExecSuccess ? "成功" : "部分失败 " + (StringUtils.isBlank(remark) ? "" : remark));
        }
        String title = OrderDeliverFileTypeEnum.getNameByCode(ordDeliveryDataFile.getFileType());
        byte[] byts = OrderDelivryDataFileUtil.initExportByte(fileDatas, title, this.getExportClass(ordDeliveryDataFile.getFileType()));
        String exportFileId = fileService.uploadAndGetId(SystemConstant.SYSTEM_CODE, title + ".xlsx", byts);
        if (StringUtils.isBlank(exportFileId)) {
            ordDeliveryDataFile.setRemark(ordDeliveryDataFile.getRemark() + "生成结果文件失败");
        }
        ordDeliveryDataFile.setErrFileId(exportFileId);
        this.overDataAFile(ordDeliveryDataFile);
//        return "执行成功";
        return true;
    }

    @Override
    public void overDataAFile(OrdDeliveryDataFile ordDeliveryDataFile) {
        orderDeliveryDataFileService.over(ordDeliveryDataFile);
    }

    /**
     * 配送/配送退货
     *
     * @param fileDatas
     * @return
     */
    private boolean execDirFile(List<ImportOrderDeliveryDataFile> fileDatas, OrdDeliveryDataFile ordDeliveryDataFile) {
        if (ordDeliveryDataFile.getFileType().equals(OrderDeliverFileTypeEnum.DIR_DELIVERY.getCode())) {
            return this.callUpdateDirDeliveryInfo(fileDatas, ordDeliveryDataFile);
        } else if (ordDeliveryDataFile.getFileType().equals(OrderDeliverFileTypeEnum.DIR_RETURN.getCode())) {
            return this.callReturnReceiving(fileDatas, ordDeliveryDataFile);
        }
        return false;
    }

    private Class getExportClass(String fileType) {
        if (OrderDeliverFileTypeEnum.DIR_RETURN.getCode().equals(fileType)) {
            return ExportOrderReturnDataFile.class;
        }
        return ExportOrderDelivryDataFile.class;
    }


    private boolean callUpdateDirDeliveryInfo(List<ImportOrderDeliveryDataFile> fileDatas, OrdDeliveryDataFile ordDeliveryDataFile) {
        Map<String, List<ImportOrderDeliveryDataFile>> groupImportMap = OrderDelivryDataFileUtil.trans2MapNoNUllByOrderNo(fileDatas);
        if (MapUtils.isEmpty(groupImportMap)) {
            return false;
        }
        Set<String> importNos = groupImportMap.keySet();
        Iterator<String> it = importNos.iterator();
        boolean isAllSuccessed = true;
        while (it.hasNext()) {
            String orderNo = it.next();
            List<ImportOrderDeliveryDataFile> importOrderDeliveryGoods = groupImportMap.get(orderNo);
            try {
                OrdDirDeliveryOut out = ordDirDeliveryService.getDeliveryOrderOutByNo(orderNo);
                if (null == out) {
                    OrderDelivryDataFileUtil.initDetailErrRremark(importOrderDeliveryGoods, "单号不存在");
                    isAllSuccessed = false;
                    continue;
                }
//                stockServer.getAndCheckStockInfo(out.getStockCode(), UserUtil.getBizOrgCode(), "配货单数据上传");
                if (!DeliveryOrderEnum.APPROVED.getKey().equals(out.getDeliveryStatusCode())) {
                    OrderDelivryDataFileUtil.initDetailErrRremark(importOrderDeliveryGoods, "此配销单状态为" + DeliveryOrderEnum.getValueByKey(out.getDeliveryStatusCode()) + "不可发货");
                    isAllSuccessed = false;
                    continue;
                }
                List<OrdDirDeliveryDetail> details = out.getDetailList();
                List<UpdateDirDeliveryDetailIn> detailIn = this.initImportDeliveryDetail(importOrderDeliveryGoods, details, ordDeliveryDataFile);
                if (CollectionUtils.isEmpty(detailIn)) {
                    isAllSuccessed = false;
                    continue;
                }
                StockInfoOut stockInfoOut = stockServer.getTransInfo(out.getStockCode());
                if (Objects.isNull(stockInfoOut)) {
                    throw new BusinessException(out.getStockCode() + "仓位不存在");
                }
                ordDirDeliveryService.updateDirDeliveryInfo(out, detailIn, stockInfoOut.getBizOrgCode());
            } catch (Exception e) {
                OrderDelivryDataFileUtil.initDetailErrRremark(importOrderDeliveryGoods, e.getMessage());
                isAllSuccessed = false;
                log.error("{}配货单发货失败，原因是--", orderNo, e);
            }
        }
        return isAllSuccessed;
    }

    private boolean callReturnReceiving(List<ImportOrderDeliveryDataFile> fileDatas, OrdDeliveryDataFile ordDeliveryDataFile) {
        Map<String, List<ImportOrderDeliveryDataFile>> groupImportMap = OrderDelivryDataFileUtil.trans2MapNoNUllByOrderNo(fileDatas);
        if (MapUtils.isEmpty(groupImportMap)) {
            return false;
        }
        Set<String> importNos = groupImportMap.keySet();
        Iterator<String> it = importNos.iterator();
        boolean isAllSuccessed = true;
        while (it.hasNext()) {
            String orderNo = it.next();
            List<ImportOrderDeliveryDataFile> importOrderDeliveryGoods = groupImportMap.get(orderNo);
            try {
                OrdDirReturn ordDirReturn = ordDirReturnService.getReturnOrderByNo(orderNo);
                if (null == ordDirReturn) {
                    OrderDelivryDataFileUtil.initDetailErrRremark(importOrderDeliveryGoods, "单号不存在");
                    isAllSuccessed = false;
                    continue;
                }
                StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDirReturn.getStockCode(), UserUtil.getBizOrgCode(), "退货单数据上传");
                if (!OrdReturnOrderStatusEnum.APPROVED.getKey().equals(ordDirReturn.getReturnStatus())) {
                    OrderDelivryDataFileUtil.initDetailErrRremark(importOrderDeliveryGoods, "此配货退货单状态为" + OrdReturnOrderStatusEnum.getValueByKey(ordDirReturn.getReturnStatus()) + "不可收货");
                    isAllSuccessed = false;
                    continue;
                }

                List<OrdDirReturnDetail> details = ordDirReturnDetailService.findByReturnOrderId(ordDirReturn.getId());
                boolean ismachedSuccess = this.initImportReturnDetail(importOrderDeliveryGoods, details, ordDeliveryDataFile);
                if (!ismachedSuccess) {
                    isAllSuccessed = false;
                    continue;
                }
                Response<String> response = ordDirReturnService.receiving(details, ordDirReturn, stockInfoOut);
                if (!response.isSuccess()) {
                    OrderDelivryDataFileUtil.initDetailErrRremark(importOrderDeliveryGoods, response.getMessage());
                    isAllSuccessed = false;
                }
            } catch (Exception e) {
                OrderDelivryDataFileUtil.initDetailErrRremark(importOrderDeliveryGoods, e.getMessage());
                log.error("{}配货退货单发货失败，原因是--", orderNo, e);
                isAllSuccessed = false;
            }
        }
        return isAllSuccessed;
    }

    /**
     * 将导入的数据数量填充到发货所需的参数中(同一单号下的)
     *
     * @param uploadList
     * @param details
     * @return
     */
    private List<UpdateDirDeliveryDetailIn> initImportDeliveryDetail(List<ImportOrderDeliveryDataFile> uploadList, List<OrdDirDeliveryDetail> details, OrdDeliveryDataFile ordDeliveryDataFile) {
        if (CollectionUtils.isEmpty(details)) {
            OrderDelivryDataFileUtil.initDetailErrRremark(uploadList, "没有查询到明细");
            return Collections.emptyList();
        }
        List<String> dbGoodsCodeList = details.stream().filter(p -> StringUtils.isNotBlank(p.getGoodsCode())).map(OrdDirDeliveryDetail::getGoodsCode).collect(Collectors.toList());
        if (this.checkDbGoodsRepeat(uploadList, dbGoodsCodeList, ordDeliveryDataFile)) {
            return Collections.emptyList();
        }
        List<String> goodsCodeList = uploadList.stream().filter(p -> StringUtils.isNotBlank(p.getGoodsCode())).map(ImportOrderDeliveryDataFile::getGoodsCode).collect(Collectors.toList());
        Map<String, OrdDirDeliveryDetail> groupMap = details.stream().filter(p -> StringUtils.isNotBlank(p.getGoodsCode())).collect(Collectors.toMap(OrdDirDeliveryDetail::getGoodsCode, item -> item));
        List<UpdateDirDeliveryDetailIn> list = new ArrayList<>();
        boolean isMached = true;
        int uploadTotal = uploadList.size();
        for (int i = 0; i < uploadList.size(); i++) {
            ImportOrderDeliveryDataFile importData = uploadList.get(i);
            OrderDelivryDataFileUtil.dealNullRemark(importData);
            if (importData.getIsSuccessed() == 0) {
                isMached = OrderDelivryDataFileUtil.initErrorData(importData, "");
                continue;
            }
            if (OrderDelivryDataFileUtil.isGoodsCodeRepeated(goodsCodeList, importData.getGoodsCode())) {
                isMached = OrderDelivryDataFileUtil.initErrorData(importData, "商品重复");
                continue;
            }
            OrdDirDeliveryDetail detail = groupMap.get(importData.getGoodsCode());
            if (null == detail) {
                isMached = OrderDelivryDataFileUtil.initErrorData(importData, "导入的商品不存在");
                continue;
            }
            if (null == detail.getDistributionQuantity()) {
                OrderDelivryDataFileUtil.initErrorData(importData, "配货数量未维护，该条数据无效");
                uploadTotal--;
                continue;
            }
            int quantityEq = (importData.getAmount()).compareTo(detail.getDistributionQuantity());
            if (quantityEq == 1) {
                isMached = OrderDelivryDataFileUtil.initErrorData(importData, "实配数不能大于配货数量");
                continue;
            }
            if (isMached) {
                UpdateDirDeliveryDetailIn updateDirDeliveryDetailIn = new UpdateDirDeliveryDetailIn();
                updateDirDeliveryDetailIn.setId(detail.getId());
                updateDirDeliveryDetailIn.setDeliveryQuantity(importData.getAmount());
                list.add(updateDirDeliveryDetailIn);
            }
        }
        //过滤调配数量为空的数据
        Map<String, OrdDirDeliveryDetail> validDbGoodsMap = details.stream().filter(p -> null != p.getDistributionQuantity()).collect(Collectors.toMap(OrdDirDeliveryDetail::getGoodsCode, item -> item));
        if (uploadTotal != validDbGoodsMap.size()) {
            OrderDelivryDataFileUtil.initDetailErrRremark(uploadList, "行数和原单不符");
            return Collections.emptyList();
        }
        if (!isMached) {
            return Collections.emptyList();
        }
        return list;
    }

    /**
     * 将导入的数据数量填充到退货收货所需的参数中
     *
     * @param uploadList
     * @param details
     * @return
     */
    private boolean initImportReturnDetail(List<ImportOrderDeliveryDataFile> uploadList, List<OrdDirReturnDetail> details, OrdDeliveryDataFile ordDeliveryDataFile) {
        if (CollectionUtils.isEmpty(details)) {
            OrderDelivryDataFileUtil.initDetailErrRremark(uploadList, "没有查询到明细");
            return false;
        }
        if (uploadList.size() != details.size()) {
            OrderDelivryDataFileUtil.initDetailErrRremark(uploadList, "行数和原单不符");
            return false;
        }
        List<String> dbGoodsCodeList = details.stream().filter(p -> StringUtils.isNotBlank(p.getGoodsCode())).map(OrdDirReturnDetail::getGoodsCode).collect(Collectors.toList());
        if (this.checkDbGoodsRepeat(uploadList, dbGoodsCodeList, ordDeliveryDataFile)) {
            return false;
        }
        List<String> goodsCodeList = uploadList.stream().filter(p -> StringUtils.isNotBlank(p.getGoodsCode())).map(ImportOrderDeliveryDataFile::getGoodsCode).collect(Collectors.toList());
        Map<String, OrdDirReturnDetail> groupMap = details.stream().filter(p -> StringUtils.isNotBlank(p.getGoodsCode())).collect(Collectors.toMap(OrdDirReturnDetail::getGoodsCode, item -> item));
        boolean isMached = true;
        for (int i = 0; i < uploadList.size(); i++) {
            ImportOrderDeliveryDataFile importData = uploadList.get(i);
            OrderDelivryDataFileUtil.dealNullRemark(importData);
            if (importData.getIsSuccessed() == 0) {
                isMached = false;
                continue;
            }
            if (OrderDelivryDataFileUtil.isGoodsCodeRepeated(goodsCodeList, importData.getGoodsCode())) {
                isMached = OrderDelivryDataFileUtil.initErrorData(importData, "导入商品重复");
                continue;
            }
            OrdDirReturnDetail detail = groupMap.get(importData.getGoodsCode());
            if (null == detail) {
                isMached = OrderDelivryDataFileUtil.initErrorData(importData, "导入商品不存在");
                continue;
            }
            if (null == detail.getApplyReturnQuantity()) {
                isMached = OrderDelivryDataFileUtil.initErrorData(importData, "退货数量为空");
                continue;
            }
            //退货数量不能大于申请数量
//            int quantityEq = (importData.getAmount()).compareTo(detail.getApplyReturnQuantity());
//            if (quantityEq == 1) {
//                isMached = OrderDelivryDataFileUtil.initErrorData(importData, "退货数量不能大于申请数量");
//                continue;
//            }
            int quantityEq = (importData.getAmount()).compareTo(detail.getAuditReturnQuantity());
            if (quantityEq == 1) {
                isMached = OrderDelivryDataFileUtil.initErrorData(importData, "退货数量不能大于审批数量");
                continue;
            }
            if (isMached) {
                detail.setActualReturnQuantity(importData.getAmount());
            }
        }
        return isMached;
    }

    private boolean checkDbGoodsRepeat(List<ImportOrderDeliveryDataFile> uploadList, List<String> dbGoodsCodeList, OrdDeliveryDataFile ordDeliveryDataFile) {
        if (OrderDelivryDataFileUtil.isHasRepeatGoodsCode(uploadList, dbGoodsCodeList, "已维护的商品重复")) {
            return true;
        }
        if (OrderDelivryDataFileUtil.isHasRepeatGoodsCode(dbGoodsCodeList, dbGoodsCodeList)) {
            ordDeliveryDataFile.setRemark("已维护的商品存在重复");
            return true;
        }
        return false;
    }

}
