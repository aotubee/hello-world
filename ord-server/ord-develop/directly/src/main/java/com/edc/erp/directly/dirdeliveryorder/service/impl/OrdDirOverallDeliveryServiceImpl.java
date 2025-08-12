package com.edc.erp.directly.dirdeliveryorder.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdeliveryorder.handle.DirDeliveryImportHandle;
import com.edc.erp.directly.dirdeliveryorder.listener.DirOverallDeliveryAsyncImportListener;
import com.edc.erp.directly.dirdeliveryorder.model.in.DirOverallDeliveryAsyncImportIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.ImportDirDeliveryOrder;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirOverallDeliveryService;
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
public class OrdDirOverallDeliveryServiceImpl implements OrdDirOverallDeliveryService {

    private final FileService fileService;

    private final DirDeliveryImportHandle dirDeliveryImportHandle;

    private final AsyncExportHandle asyncExportHandle;

    private final RedisService redisService;

    @Override
    public Response<String> asyncImportOverallDelivery(DirOverallDeliveryAsyncImportIn dirOverallDeliveryAsyncImportIn, String loginUsername, String loginBizOrgCode) {
        String key = DirSystemConstant.CHECK_DIR_ORDER_DELIVERY_OVERALL_IMPORT_ONLY_ONE + loginBizOrgCode;
        if (!redisService.setIfAbsent(key, loginUsername, 10L, TimeUnit.MINUTES)) {
            return Response.error("当前有用户正在异步上传配货单，请稍后尝试");
        }
        byte[] bytes = fileService.getFileBytesByFileId(dirOverallDeliveryAsyncImportIn.getFileId(), SystemConstant.SYSTEM_CODE);
        if (Objects.isNull(bytes)) {
            redisService.del(key);
            throw new BusinessException("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DirOverallDeliveryAsyncImportListener listener = new DirOverallDeliveryAsyncImportListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, listener).headRowNumber(NumberUtil.INTEGER_ZERO).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > NumberUtil.INTEGER_ZERO) {
            redisService.del(key);
            return Response.success(listener.getImportErrorMessage(totalErrorMap));
        }
        List<ImportDirDeliveryOrder> importDirDeliveryOrderList = this.changeImportDetail(listener.getDataList(), dirOverallDeliveryAsyncImportIn);
        if (CollectionUtils.isEmpty(importDirDeliveryOrderList)) {
            return Response.error("没有可导入的配销信息");
        }
        // 异步导入入库
        dirDeliveryImportHandle.handleDeliveryListAsyncImport(importDirDeliveryOrderList, loginUsername, loginBizOrgCode, "统筹", key);
        return Response.success("开始导入，请耐心等待，切勿重复操作！");
    }

    private List<ImportDirDeliveryOrder> changeImportDetail(List<Map<Integer, String>> dataList, DirOverallDeliveryAsyncImportIn dirOverallDeliveryAsyncImportIn) {
        List<ImportDirDeliveryOrder> importDisDeliveryOrderList = Lists.newArrayList();
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
                    ImportDirDeliveryOrder importDirDeliveryOrder = new ImportDirDeliveryOrder();
                    BeanUtils.copy(dirOverallDeliveryAsyncImportIn, importDirDeliveryOrder);
                    importDirDeliveryOrder.setStoreCode(storeCode);
                    importDirDeliveryOrder.setGoodsCode(goodsCode);
                    importDirDeliveryOrder.setDeliveryQuantity(Integer.parseInt(typeValue));
                    importDisDeliveryOrderList.add(importDirDeliveryOrder);
                });
            });
        }
        return importDisDeliveryOrderList;
    }
}
