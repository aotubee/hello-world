package com.edc.erp.directly.distribution.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionDetail;
import com.edc.erp.directly.distribution.handle.DirDistributionOrderImportHandle;
import com.edc.erp.directly.distribution.listener.DirOverallDistributionAsyncListener;
import com.edc.erp.directly.distribution.model.out.*;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionDetailService;
import com.edc.erp.directly.distribution.service.OrdDirOverallDistributionService;
import com.edc.erp.directly.enumeration.OrderDistributionOrderStatusEnum;
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
public class OrdDirOverallDistributionServiceImpl implements OrdDirOverallDistributionService {

    private final FileService fileService;

    private final DirDistributionOrderImportHandle dirDistributionOrderImportHandle;

    private final OrdDirOrderDistributionDetailService ordDirOrderDistributionDetailService;

    private final AsyncExportHandle asyncExportHandle;

    @Override
    public Response<Long> asyncImportOverallDistributionDetail(String fileId, OrdDirOrderDistribution ordDirOrderDistribution, String loginUsername) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (Objects.isNull(bytes)) {
            throw new BusinessException("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DirOverallDistributionAsyncListener listener = new DirOverallDistributionAsyncListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, listener).headRowNumber(0).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > 0) {
            return Response.success(listener.getImportErrorMessage(totalErrorMap));
        }
        Map<String, OrdDirDistributionImportResultOut> importResultOutMap = this.changeImportDetail(listener.getDataList());
        if (importResultOutMap.size() == 0) {
            return Response.error("没有可导入的分货信息");
        }
        // 异步导入入库
        dirDistributionOrderImportHandle.handleAsyncDirDistribution(importResultOutMap, ordDirOrderDistribution.getId(), loginUsername, "统筹");
        return Response.data(ordDirOrderDistribution.getId(), "文件导入中，稍后刷新查看！");
    }

    @Override
    public OrdDirOverallDistributionDetailOut getOverallDistributionDetailList(OrdDirOrderDistribution ordDirOrderDistribution) {
        List<OrdDirOrderDistributionDetail> detailList = ordDirOrderDistributionDetailService.getDetailByOrdDistributionOrderId(ordDirOrderDistribution.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            return new OrdDirOverallDistributionDetailOut();
        }
        Map<String, List<OrdDirOrderDistributionDetail>> storeDetailMap = detailList.stream().collect(Collectors.groupingBy(OrdDirOrderDistributionDetail::getStoreCode));
        // 按商品集合长度排序，找出最大商品列表清单
        HashMap<String, List<OrdDirOrderDistributionDetail>> sortStoreDetailMap = this.sortMapByValues(storeDetailMap);
        List<OrdDirOrderDistributionDetail> maxGoodsCodeList = sortStoreDetailMap.values().stream().findFirst().get();
        List<OrdDirOverallDistributionStoreOut> resultList = Lists.newArrayList();
        storeDetailMap.entrySet().forEach(entry -> {
            // 该门店下所有已存在明细Map
            Map<String, OrdDirOrderDistributionDetail> existDetailMap = entry.getValue().stream().collect(Collectors.toMap(OrdDirOrderDistributionDetail::getGoodsCode, Function.identity()));
            List<OrdDirOverallDistributionGoodsOut> overallDetailList = Lists.newArrayList();
            // 循环最大商品清单
            maxGoodsCodeList.forEach(maxDetail -> {
                // 校验已存在明细Map是否存在清单中该商品
                OrdDirOrderDistributionDetail checkExistDetail = existDetailMap.get(maxDetail.getGoodsCode());
                // 已存在则跳过
                if (Objects.isNull(checkExistDetail)) {
                    // 不存在则虚拟占坑商品
                    checkExistDetail = new OrdDirOrderDistributionDetail();
                    checkExistDetail.setGoodsCode(maxDetail.getGoodsCode());
                    checkExistDetail.setGoodsName(maxDetail.getGoodsName());
                }
                OrdDirOverallDistributionGoodsOut ordDisOverallDistributionGoodsOut = new OrdDirOverallDistributionGoodsOut();
                BeanUtils.copy(checkExistDetail, ordDisOverallDistributionGoodsOut);
                ordDisOverallDistributionGoodsOut.setDetailId(checkExistDetail.getId());
                overallDetailList.add(ordDisOverallDistributionGoodsOut);
            });
            OrdDirOrderDistributionDetail infoDetail = entry.getValue().get(0);
            OrdDirOverallDistributionStoreOut ordDisOverallDistributionStoreOut = new OrdDirOverallDistributionStoreOut();
            ordDisOverallDistributionStoreOut.setStoreCode(infoDetail.getStoreCode());
            ordDisOverallDistributionStoreOut.setStoreName(infoDetail.getStoreName());
            ordDisOverallDistributionStoreOut.setStoreArea(infoDetail.getStoreArea());
            ordDisOverallDistributionStoreOut.setDetailList(overallDetailList);
            resultList.add(ordDisOverallDistributionStoreOut);
        });
        List<OrdDirOverallDistributionStoreOut> overallDistributionStoreOuts = resultList.stream().sorted(Comparator.comparing(OrdDirOverallDistributionStoreOut::getStoreCode)).collect(Collectors.toList());
        OrdDirOverallDistributionDetailOut ordDirOverallDistributionDetailOut = new OrdDirOverallDistributionDetailOut();
        BeanUtils.copy(ordDirOrderDistribution, ordDirOverallDistributionDetailOut);
        ordDirOverallDistributionDetailOut.setDistributionOrderId(ordDirOrderDistribution.getId());
        ordDirOverallDistributionDetailOut.setDistributionOrderStatusStr(OrderDistributionOrderStatusEnum.getValueByKey(ordDirOrderDistribution.getDistributionOrderStatus()));
        ordDirOverallDistributionDetailOut.setDistributionStoreOutList(overallDistributionStoreOuts);
        return ordDirOverallDistributionDetailOut;
    }

    private HashMap<String, List<OrdDirOrderDistributionDetail>> sortMapByValues(Map<String, List<OrdDirOrderDistributionDetail>> map) {
        // 蓝要用LinkedHashMap 能产
        HashMap<String, List<OrdDirOrderDistributionDetail>> finalMap = new LinkedHashMap<>();
        //歌出map链值对Entry<K,>，然后按照值排序，最后组成一个新的列表集台
        List<Map.Entry<String, List<OrdDirOrderDistributionDetail>>> list = map.entrySet()
                .stream().sorted((o1, o2) -> o2.getValue().size() - o1.getValue().size()).collect(Collectors.toList());
        //遍历集台，将煤好序的链值对Entry<K,V放入新map并返回
        list.forEach(ele -> finalMap.put(ele.getKey(), ele.getValue()));
        return finalMap;
    }

    private Map<String, OrdDirDistributionImportResultOut> changeImportDetail(List<Map<Integer, String>> dataList) {
        Map<String, OrdDirDistributionImportResultOut> importResultMap = new HashMap<>();
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
                OrdDirDistributionImportResultOut importResultOut = new OrdDirDistributionImportResultOut();
                List<OrdDirDistributionImportGoodsOut> importGoodsList = Lists.newArrayList();
                storeGoodsQuantityMap.forEach((indexKey, typeValue) -> {
                    // 门店代码
                    if (indexKey.equals(NumberUtil.INTEGER_ZERO)) {
                        importResultOut.setStoreCode(typeValue);
                        return;
                    }
                    // 封装商品与数量
                    String goodsCode = columnIndexGoodsCodeMap.get(indexKey);
                    OrdDirDistributionImportGoodsOut importGoodsOut = new OrdDirDistributionImportGoodsOut();
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
