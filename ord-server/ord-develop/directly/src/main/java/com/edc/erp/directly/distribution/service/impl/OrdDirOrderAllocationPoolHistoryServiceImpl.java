package com.edc.erp.directly.distribution.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.handle.EmpowerGroupHandle;
import com.edc.erp.common.model.in.StoreAreaIn;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.directly.distribution.entity.OrdDirOrderAllocationPoolHistory;
import com.edc.erp.directly.distribution.excel.ExportOrdDirOrderAllocationPool;
import com.edc.erp.directly.distribution.excel.ExportOrdDirOrderAllocationPoolHistory;
import com.edc.erp.directly.distribution.excel.ExportOrdDirOrderDetail;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderAllocationPoolHistoryMapper;
import com.edc.erp.directly.distribution.model.in.QueryAllocationPoolPageIn;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderAllocationPoolPageOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderAllocationPoolHistoryService;
import com.edc.erp.directly.distribution.util.FileExportUtil;
import com.edc.erp.directly.enumeration.OrderAllocationPoolStatusEnum;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @ClassName OrdDirOrderAllocationPoolHistoryServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/1/8 9:17
 **/
@Service
@RequiredArgsConstructor
public class OrdDirOrderAllocationPoolHistoryServiceImpl implements OrdDirOrderAllocationPoolHistoryService {

    private final OrdDirOrderAllocationPoolHistoryMapper ordDirOrderAllocationPoolHistoryMapper;

    private final StockServer stockServer;

    private final StoreCenterService storeCenterService;

    private final FileService fileService;

    private final EmpowerGroupHandle empowerGroupHandle;

    @Override
    public Page<OrdDirOrderAllocationPoolPageOut> findHistoryListForPage(QueryAllocationPoolPageIn queryAllocationPoolPageIn) {
        List<String> areaCodeList = empowerGroupHandle.findEmpowerGroupsByType(queryAllocationPoolPageIn.getBizOrgCode(), "operation");
        List<String> queryAreaCodeList = Lists.newArrayList();
//        if (CollectionUtils.isEmpty(areaCodeList) && StringUtils.isBlank(queryAllocationPoolPageIn.getStoreArea())) {
//            queryAllocationPoolPageIn.setStoreCodeList(null);
//        } else if (CollectionUtils.isEmpty(areaCodeList) && StringUtils.isNotBlank(queryAllocationPoolPageIn.getStoreArea())) {
//            queryAreaCodeList.add(queryAllocationPoolPageIn.getStoreArea());
//            queryAllocationPoolPageIn.setStoreCodeList(queryAreaCodeList);
//        } else {
//
//            if (StringUtils.isNotEmpty(queryAllocationPoolPageIn.getStoreArea())) {
//                boolean areaIsExist = areaCodeList.stream().anyMatch(areaCode -> areaCode.equals(queryAllocationPoolPageIn.getStoreArea()));
//                if (!areaIsExist) {
//                    return new Page<>();
//                } else {
//                    queryAreaCodeList.add(queryAllocationPoolPageIn.getStoreArea());
//                }
//            } else {
//                queryAreaCodeList.addAll(areaCodeList);
//            }
//        }
        if (CollectionUtils.isEmpty(areaCodeList) && StringUtils.isBlank(queryAllocationPoolPageIn.getStoreArea())) {
            queryAllocationPoolPageIn.setStoreCodeList(null);
        } else if (CollectionUtils.isEmpty(areaCodeList) && StringUtils.isNotBlank(queryAllocationPoolPageIn.getStoreArea())) {
            queryAreaCodeList.add(queryAllocationPoolPageIn.getStoreArea());
            queryAllocationPoolPageIn.setStoreCodeList(queryAreaCodeList);
        } else {
            StoreAreaIn storeAreaIn = new StoreAreaIn();
            storeAreaIn.setAreaCodes(areaCodeList);
            storeAreaIn.setBizOrgCode(queryAllocationPoolPageIn.getBizOrgCode());
            List<String> childAreasCodeList = storeCenterService.findChildAreasByCodes(storeAreaIn);
            if (StringUtils.isNotBlank(queryAllocationPoolPageIn.getStoreArea())) {
                Optional<String> areaOptional = childAreasCodeList.stream().filter(checkAreaCode -> checkAreaCode.equals(queryAllocationPoolPageIn.getStoreArea())).findFirst();
                if (areaOptional.isPresent()) {
                    queryAreaCodeList.add(queryAllocationPoolPageIn.getStoreArea());
                    queryAllocationPoolPageIn.setStoreCodeList(queryAreaCodeList);
                } else {
                    return new Page<>();
                }
            } else {
                queryAreaCodeList.addAll(childAreasCodeList);
                queryAllocationPoolPageIn.setStoreCodeList(queryAreaCodeList);
            }
        }
        if (CollectionUtils.isNotEmpty(queryAreaCodeList)) {
            StoreInfoIn storeInfoIn = new StoreInfoIn();
            storeInfoIn.setAreaCodes(queryAreaCodeList);
            storeInfoIn.setBizOrgCode(queryAllocationPoolPageIn.getBizOrgCode());
            List<StoreInfoOut> storeInfoOutList = storeCenterService.findByAreaCodeList(storeInfoIn);
            List<String> storeCodeList = new ArrayList<>();
            storeInfoOutList.forEach(storeInfoOut -> storeCodeList.add(storeInfoOut.getErpStoreCode()));
            queryAllocationPoolPageIn.setStoreCodeList(storeCodeList);
        }
//        String loginBizOrgCode = queryAllocationPoolPageIn.getBizOrgCode();
//        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(loginBizOrgCode);
//        Optional<StockInfoOut> anyOneOptional = authOrgStockMap.values().stream().filter(stockInfoOut -> !loginBizOrgCode.equals(stockInfoOut.getBizOrgCode())).findFirst();
//        if (!anyOneOptional.isPresent()) {
//            queryAllocationPoolPageIn.setBizOrgCode("");
//        }
        List<OrdDirOrderAllocationPoolHistory> list = ordDirOrderAllocationPoolHistoryMapper.findHistoryListByPage(queryAllocationPoolPageIn);
        List<OrdDirOrderAllocationPoolPageOut> resultList = list.stream().map(ordDirOrderAllocationPool -> {
            OrdDirOrderAllocationPoolPageOut ordDirOrderAllocationPoolPageOut = new OrdDirOrderAllocationPoolPageOut();
            BeanUtils.copy(ordDirOrderAllocationPool, ordDirOrderAllocationPoolPageOut);
            ordDirOrderAllocationPoolPageOut.setStatusStr(OrderAllocationPoolStatusEnum.getValueByKey(ordDirOrderAllocationPool.getStatus()));
            return ordDirOrderAllocationPoolPageOut;
        }).collect(Collectors.toList());
        Page<OrdDirOrderAllocationPoolPageOut> page = new Page<>(queryAllocationPoolPageIn);
        page.setList(resultList);
        return page;
    }

    @Override
    public String exportAllocationPoolHistory(QueryAllocationPoolPageIn queryAllocationPoolPageIn) {
        //分页查询出货单明细
        Page<OrdDirOrderAllocationPoolPageOut> page = this.findHistoryListForPage(queryAllocationPoolPageIn);
        //获取分页数据
        List<OrdDirOrderAllocationPoolPageOut> detaiList = page.getList();
        //导出Excel实体
        List<ExportOrdDirOrderAllocationPoolHistory> exportDetails = new ArrayList<>();
        for (OrdDirOrderAllocationPoolPageOut detailOut : detaiList) {
            //导出实体
            ExportOrdDirOrderAllocationPoolHistory exportDetail = new ExportOrdDirOrderAllocationPoolHistory();
            BeanUtils.copy(detailOut, exportDetail);
            exportDetail.setTruncationDateTimeStr(DateUtils.format(detailOut.getTruncationDateTime()));
            exportDetail.setUpdateTimeStr(DateUtils.format(detailOut.getUpdateTime()));
            //添加excel导出结果集
            exportDetails.add(exportDetail);
        }
        //导入excel标题
        String title = "订单调配历史";
        byte[] fileBytesByData = FileExportUtil.getFileBytesByData(
                exportDetails,
                title,
                title,
                ExportOrdDirOrderAllocationPoolHistory.class,
                true);

        return fileService.uploadFile(
                title + ".xlsx",
                fileBytesByData,
                SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }
}
