package com.edc.erp.disdeliveryorder.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.handle.DisDeliveryImportHandle;
import com.edc.erp.disdeliveryorder.listener.DisOverallDeliveryAsyncImportListener;
import com.edc.erp.disdeliveryorder.model.in.DisOverallDeliveryAsyncImportIn;
import com.edc.erp.disdeliveryorder.model.in.ImportDisDeliveryOrder;
import com.edc.erp.disdeliveryorder.service.OrdDisOverallDeliveryService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName OrdDisOverallDeliveryServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/11/22 16:27
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class OrdDisOverallDeliveryServiceImpl implements OrdDisOverallDeliveryService {

    private final FileService fileService;

    private final DisDeliveryImportHandle disDeliveryImportHandle;

    private final RedisService redisService;

    @Override
    public Response<String> asyncImportOverallDelivery(DisOverallDeliveryAsyncImportIn disOverallDeliveryAsyncImportIn, String loginUsername, String loginBizOrgCode) {
        String key = DisSystemConstant.CHECK_DIS_ORDER_DELIVERY_OVERALL_IMPORT_ONLY_ONE + loginBizOrgCode;
        if (!redisService.setIfAbsent(key, loginUsername, 10L, TimeUnit.MINUTES)) {
            return Response.error("当前有用户正在异步上传配销单，请稍后尝试");
        }
        byte[] bytes = fileService.getFileBytesByFileId(disOverallDeliveryAsyncImportIn.getFileId(), SystemConstant.SYSTEM_CODE);
        if (Objects.isNull(bytes)) {
            redisService.del(key);
            throw new BusinessException("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DisOverallDeliveryAsyncImportListener listener = new DisOverallDeliveryAsyncImportListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, listener).headRowNumber(NumberUtil.INTEGER_ZERO).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > NumberUtil.INTEGER_ZERO) {
            redisService.del(key);
            return Response.success(listener.getImportErrorMessage(totalErrorMap));
        }
        List<ImportDisDeliveryOrder> importDisDeliveryOrderList = this.changeImportDetail(listener.getDataList(), disOverallDeliveryAsyncImportIn);
        if (CollectionUtils.isEmpty(importDisDeliveryOrderList)) {
            return Response.error("没有可导入的配销信息");
        }
        // 异步导入入库
        disDeliveryImportHandle.handleDeliveryListAsyncImport(importDisDeliveryOrderList, loginUsername, loginBizOrgCode, "统筹", key);
        return Response.success("开始导入，请耐心等待，切勿重复操作！");
    }

    private List<ImportDisDeliveryOrder> changeImportDetail(List<Map<Integer, String>> dataList, DisOverallDeliveryAsyncImportIn disOverallDeliveryAsyncImportIn) {
        List<ImportDisDeliveryOrder> importDisDeliveryOrderList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(dataList)) {
            // 获取表格中列索引商品代码Map,过滤excel文件A1格子无效数据
            Map<Integer, String> columnIndexGoodsCodeMap = dataList.get(0).entrySet().stream()
                    .filter(entry -> Objects.nonNull(entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            // 获取门店和商品数量集合（首列索引为门店代码，其余为商品数量）
            List<Map<Integer, String>> storeGoodsQuantityList = dataList.subList(1, dataList.size());
            if (CollectionUtils.isEmpty(storeGoodsQuantityList)) {
                return importDisDeliveryOrderList;
            }
            // {0:"9200",1:"2",2:"3"}
            storeGoodsQuantityList.forEach(storeGoodsQuantityMap -> {
                // 获取门店代码
                String storeCode = storeGoodsQuantityMap.get(NumberUtil.INTEGER_ZERO);
                storeGoodsQuantityMap.forEach((indexKey, typeValue) -> {
                    // 跳过门店代码
                    if (indexKey.equals(NumberUtil.INTEGER_ZERO)) {
                        return;
                    }
                    // 封装商品与数量
                    if (StringUtils.isBlank(typeValue) || typeValue.equals(NumberUtil.INTEGER_ZERO.toString())) {
                        return;
                    }
                    String goodsCode = columnIndexGoodsCodeMap.get(indexKey);
                    ImportDisDeliveryOrder importDisDeliveryOrder = new ImportDisDeliveryOrder();
                    BeanUtils.copy(disOverallDeliveryAsyncImportIn, importDisDeliveryOrder);
                    importDisDeliveryOrder.setStoreCode(storeCode);
                    importDisDeliveryOrder.setGoodsCode(goodsCode);
                    importDisDeliveryOrder.setDeliveryQuantity(Integer.parseInt(typeValue));
                    importDisDeliveryOrderList.add(importDisDeliveryOrder);
                });
            });
        }
        return importDisDeliveryOrderList;
    }
}
