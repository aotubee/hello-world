package com.edc.erp.distribution.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionDetail;
import com.edc.erp.distribution.handle.DisDistributionOrderImportHandle;
import com.edc.erp.distribution.listener.DisOverallDistributionAsyncListener;
import com.edc.erp.distribution.model.out.*;
import com.edc.erp.distribution.service.OrdDisOrderDistributionDetailService;
import com.edc.erp.distribution.service.OrdDisOverallDistributionService;
import com.edc.erp.enumeration.DistributionIdentificationEnum;
import com.edc.erp.enumeration.OrderDistributionOrderStatusEnum;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName OrdDisOverallDistributionServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/18 11:50
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisOverallDistributionServiceImpl implements OrdDisOverallDistributionService {

    private final FileService fileService;

    private final DisDistributionOrderImportHandle disDistributionOrderImportHandle;

    private final OrdDisOrderDistributionDetailService ordDisOrderDistributionDetailService;

    @Override
    public Response<Long> asyncImportOverallDistributionDetail(String fileId, OrdDisOrderDistribution ordDisOrderDistribution, String loginUsername, String distributionIdentification) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (Objects.isNull(bytes)) {
            throw new BusinessException("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DisOverallDistributionAsyncListener listener = new DisOverallDistributionAsyncListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, listener).headRowNumber(0).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > 0) {
            return Response.success(listener.getImportErrorMessage(totalErrorMap));
        }
        Map<String, OrdDisDistributionImportResultOut> importResultOutMap = this.changeImportDetail(listener.getDataList());
        if (importResultOutMap.size() == 0) {
            return Response.error("没有可导入的分货信息");
        }
        // 异步导入入库
        disDistributionOrderImportHandle.handleAsyncDirDistribution(importResultOutMap, ordDisOrderDistribution.getId(), loginUsername,"统筹", distributionIdentification);
        return Response.data(ordDisOrderDistribution.getId(), "文件导入中，稍后刷新查看！");
    }

    @Override
    public OrdDisOverallDistributionDetailOut getOverallDistributionDetailList(OrdDisOrderDistribution ordDisOrderDistribution) {
        List<OrdDisOrderDistributionDetail> detailList = ordDisOrderDistributionDetailService.getDetailByOrdDistributionOrderId(ordDisOrderDistribution.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            return new OrdDisOverallDistributionDetailOut();
        }
        Map<String, List<OrdDisOrderDistributionDetail>> storeDetailMap = detailList.stream().collect(Collectors.groupingBy(OrdDisOrderDistributionDetail::getStoreCode));
        // 按商品集合长度排序，找出最大商品列表清单
        HashMap<String, List<OrdDisOrderDistributionDetail>> sortStoreDetailMap = this.sortMapByValues(storeDetailMap);
        List<OrdDisOrderDistributionDetail> maxGoodsCodeList = sortStoreDetailMap.values().stream().findFirst().get();
        List<OrdDisOverallDistributionStoreOut> resultList = Lists.newArrayList();
        storeDetailMap.entrySet().forEach(entry -> {
            // 该门店下所有已存在明细Map
            Map<String, OrdDisOrderDistributionDetail> existDetailMap = entry.getValue().stream().collect(Collectors.toMap(OrdDisOrderDistributionDetail::getGoodsCode, Function.identity()));
            List<OrdDisOverallDistributionGoodsOut> overallDetailList = Lists.newArrayList();
            // 循环最大商品清单
            maxGoodsCodeList.forEach(maxDetail -> {
                // 校验已存在明细Map是否存在清单中该商品
                OrdDisOrderDistributionDetail checkExistDetail = existDetailMap.get(maxDetail.getGoodsCode());
                // 已存在则跳过
                if (Objects.isNull(checkExistDetail)) {
                    // 不存在则虚拟占坑商品
                    checkExistDetail = new OrdDisOrderDistributionDetail();
                    checkExistDetail.setGoodsCode(maxDetail.getGoodsCode());
                    checkExistDetail.setGoodsName(maxDetail.getGoodsName());
                }
                OrdDisOverallDistributionGoodsOut ordDisOverallDistributionGoodsOut = new OrdDisOverallDistributionGoodsOut();
                BeanUtils.copy(checkExistDetail, ordDisOverallDistributionGoodsOut);
                ordDisOverallDistributionGoodsOut.setDetailId(checkExistDetail.getId());
                overallDetailList.add(ordDisOverallDistributionGoodsOut);
            });
            OrdDisOrderDistributionDetail infoDetail = entry.getValue().get(0);
            OrdDisOverallDistributionStoreOut ordDisOverallDistributionStoreOut = new OrdDisOverallDistributionStoreOut();
            ordDisOverallDistributionStoreOut.setStoreCode(infoDetail.getStoreCode());
            ordDisOverallDistributionStoreOut.setStoreName(infoDetail.getStoreName());
            ordDisOverallDistributionStoreOut.setStoreArea(infoDetail.getStoreArea());
            ordDisOverallDistributionStoreOut.setDetailList(overallDetailList);
            resultList.add(ordDisOverallDistributionStoreOut);
        });
        List<OrdDisOverallDistributionStoreOut> overallDistributionStoreOuts = resultList.stream().sorted(Comparator.comparing(OrdDisOverallDistributionStoreOut::getStoreCode)).collect(Collectors.toList());
        OrdDisOverallDistributionDetailOut overallDistributionDetailOut = new OrdDisOverallDistributionDetailOut();
        BeanUtils.copy(ordDisOrderDistribution, overallDistributionDetailOut);
        overallDistributionDetailOut.setDistributionOrderId(ordDisOrderDistribution.getId());
        overallDistributionDetailOut.setDistributionOrderStatusStr(OrderDistributionOrderStatusEnum.getValueByKey(ordDisOrderDistribution.getDistributionOrderStatus()));
        overallDistributionDetailOut.setDistributionIdentificationStr(DistributionIdentificationEnum.getNameByCode(ordDisOrderDistribution.getDistributionIdentification()));
        overallDistributionDetailOut.setDistributionStoreOutList(overallDistributionStoreOuts);
        return overallDistributionDetailOut;
    }

    private HashMap<String, List<OrdDisOrderDistributionDetail>> sortMapByValues(Map<String, List<OrdDisOrderDistributionDetail>> map) {
        // 蓝要用LinkedHashMap 能产
        HashMap<String, List<OrdDisOrderDistributionDetail>> finalMap = new LinkedHashMap<>();
        //歌出map链值对Entry<K,>，然后按照值排序，最后组成一个新的列表集台
        List<Map.Entry<String, List<OrdDisOrderDistributionDetail>>> list = map.entrySet()
                .stream().sorted((o1, o2) -> o2.getValue().size() - o1.getValue().size()).collect(Collectors.toList());
        //遍历集台，将煤好序的链值对Entry<K,V放入新map并返回
        list.forEach(ele -> finalMap.put(ele.getKey(), ele.getValue()));
        return finalMap;
    }

    private Map<String, OrdDisDistributionImportResultOut> changeImportDetail(List<Map<Integer, String>> dataList) {
        Map<String, OrdDisDistributionImportResultOut> importResultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(dataList)) {
            // 获取商品代码集合
//            goodsCodeList = dataList.get(0).values().stream().collect(Collectors.toList());

            // 获取表格中列索引商品代码Map,过滤excel文件A1格子无效数据
            Map<Integer, String> columnIndexGoodsCodeMap = dataList.get(0).entrySet().stream()
                    .filter(entry -> Objects.nonNull(entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // 获取门店和商品数量集合
            List<Map<Integer, String>> storeGoodsQuantityList = dataList.subList(1, dataList.size());
            if (CollectionUtils.isEmpty(storeGoodsQuantityList)) {
                return importResultMap;
            }
//            // 获取表格中列索引集合
//            List<Integer> columnIndexList = storeGoodsQuantityList.get(0).keySet().stream().collect(Collectors.toList());

//            List<OrdDisDistributionImportResultOut> testStoreImportInList = Lists.newArrayList();

            // {0:"9200",1:"2",2:"3"}
            storeGoodsQuantityList.forEach(storeGoodsQuantityMap -> {
                OrdDisDistributionImportResultOut importResultOut = new OrdDisDistributionImportResultOut();
                List<OrdDisDistributionImportGoodsOut> importGoodsList = Lists.newArrayList();
                storeGoodsQuantityMap.forEach((indexKey, typeValue) -> {
                    // 门店代码
                    if (indexKey.equals(NumberUtil.INTEGER_ZERO)) {
                        importResultOut.setStoreCode(typeValue);
                        return;
                    }
                    // 封装商品与数量
                    String goodsCode = columnIndexGoodsCodeMap.get(indexKey);
                    OrdDisDistributionImportGoodsOut importGoodsOut = new OrdDisDistributionImportGoodsOut();
                    importGoodsOut.setGoodsCode(goodsCode);
                    importGoodsOut.setDistributionQuantity(new BigDecimal(typeValue));
                    importGoodsList.add(importGoodsOut);
                });
                importResultOut.setImportGoodsOutList(importGoodsList);
                importResultMap.put(importResultOut.getStoreCode(), importResultOut);
//                testStoreImportInList.add(importResultOut);
            });
//            testStoreImportInList.forEach(testStoreImportIn -> {
//                System.out.println(JSONObject.toJSONString(testStoreImportIn));
//            });
        }
        return importResultMap;
    }
}
