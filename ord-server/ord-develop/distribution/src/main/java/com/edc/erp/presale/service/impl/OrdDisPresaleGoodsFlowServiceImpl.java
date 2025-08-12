package com.edc.erp.presale.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.out.store.StoreInfoOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.FileExportUtil;
import com.edc.erp.presale.enumeration.OrdDisPresaleAdjustOrderTypeEnum;
import com.edc.erp.presale.enumeration.OrdDisPresaleFlowBusinessTypeEnum;
import com.edc.erp.presale.mapper.OrdDisPresaleGoodsFlowMapper;
import com.edc.erp.presale.model.excel.ExcelPresaleGoodsFlow;
import com.edc.erp.presale.model.in.PresaleGoodsFlowPageIn;
import com.edc.erp.presale.model.out.PresaleGoodsFlowPageOut;
import com.edc.erp.presale.service.OrdDisPresaleGoodsFlowService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.utils.DateUtils;
import com.edc.uc.authority.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName OrdDisPresaleGoodsFlowServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/21 18:35
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisPresaleGoodsFlowServiceImpl implements OrdDisPresaleGoodsFlowService {
    private final OrdDisPresaleGoodsFlowMapper ordDisPresaleGoodsFlowMapper;
    private final StoreCenterService storeCenterService;
    private final FileService fileService;

    @Override
    public Page<PresaleGoodsFlowPageOut> findPresaleGoodsFlowByPage(PresaleGoodsFlowPageIn presaleGoodsFlowPageIn) {
        if (StringUtils.isBlank(presaleGoodsFlowPageIn.getBizOrgCode())) {
            presaleGoodsFlowPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        }
        if (StringUtils.isNotEmpty(presaleGoodsFlowPageIn.getStoreArea())) {
            List<String> storeCodeList = new ArrayList<>();
            List<StoreInfoOut> storeList = storeCenterService.getStoreInfoByCode(new StoreInfoIn(presaleGoodsFlowPageIn.getStoreArea()));
            if (CollectionUtils.isEmpty(storeList)) {
                return new Page<>(presaleGoodsFlowPageIn);
            }
            storeList.forEach(storeInfoOut -> storeCodeList.add(storeInfoOut.getErpStoreCode()));
            presaleGoodsFlowPageIn.setStoreCodeList(storeCodeList);
        }
        List<PresaleGoodsFlowPageOut> goodsFlowList = ordDisPresaleGoodsFlowMapper.findPresaleGoodsFlowByPage(presaleGoodsFlowPageIn);
        goodsFlowList.forEach(item -> {
            item.setBusinessTypeDesc(OrdDisPresaleFlowBusinessTypeEnum.getValueByKey(item.getBusinessType()));
            // StoreInfo storeInfo = storeCenterService.getStoreByCode(item.getStoreCode(), item.getBizOrgCode());
            // if (Objects.nonNull(storeInfo)) {
            //     item.setBelongArea(storeInfo.getBelongArea());
            // }
        });
        Page<PresaleGoodsFlowPageOut> goodsFlowPage = new Page<>(presaleGoodsFlowPageIn);
        goodsFlowPage.setList(goodsFlowList);
        return goodsFlowPage;
    }

    @Override
    public String exportPresaleGoodsFlowList(PresaleGoodsFlowPageIn presaleGoodsFlowPageIn) {
        String title = "预售商品流水";
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        Page<PresaleGoodsFlowPageOut> resultPage = this.findPresaleGoodsFlowByPage(presaleGoodsFlowPageIn);
        List<ExcelPresaleGoodsFlow> exportPresaleGoodsFlowList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(resultPage.getList())) {
            resultPage.getList().forEach(flow -> {
                ExcelPresaleGoodsFlow exportPresaleGoodsFlow = new ExcelPresaleGoodsFlow();
                BeanUtils.copyProperties(flow, exportPresaleGoodsFlow);
                exportPresaleGoodsFlow.setActualLowering(OrdDisPresaleAdjustOrderTypeEnum.getName(flow.getActualLowering()));
                exportPresaleGoodsFlowList.add(exportPresaleGoodsFlow);
            });
        }
        byte[] bytes = FileExportUtil.getFileBytesByData(exportPresaleGoodsFlowList, "预售商品流水", "预售商品流水", ExcelPresaleGoodsFlow.class, true);
        return fileService.uploadFile(fileName, bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }
}
