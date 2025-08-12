package com.edc.erp.presale.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.out.store.StoreInfoOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.FileExportUtil;
import com.edc.erp.presale.entity.OrdDisPresaleAssets;
import com.edc.erp.presale.mapper.OrdDisPresaleAssetsMapper;
import com.edc.erp.presale.model.excel.ExcelOrdDisPresaleAssetsDetail;
import com.edc.erp.presale.model.in.QueryPresaleAssetsDetailPageIn;
import com.edc.erp.presale.model.in.QueryPresaleAssetsPageIn;
import com.edc.erp.presale.model.in.UpdateDisPresaleAssetsIn;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsDetailOut;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsPageOut;
import com.edc.erp.presale.service.OrdDisPresaleAssetsDetailService;
import com.edc.erp.presale.service.OrdDisPresaleAssetsService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @ClassName OrdDisPresaleAssetsServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/21 18:38
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisPresaleAssetsServiceImpl implements OrdDisPresaleAssetsService {
    private final OrdDisPresaleAssetsMapper ordDisPresaleAssetsMapper;
    private final OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService;
    private final StoreCenterService storeCenterService;
    private final FileService fileService;

    @Override
    public OrdDisPresaleAssets getOrdDisPresaleAssetsByStoreCode(String storeCode) {
        return ordDisPresaleAssetsMapper.getOrdDisPresaleAssetsByStoreCode(storeCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePresaleAssets(UpdateDisPresaleAssetsIn updateDisPresaleAssetsIn) {
        String loginUsername = updateDisPresaleAssetsIn.getLoginUsername();
        Long assetsId = updateDisPresaleAssetsIn.getAssetsId();
        String businessType = updateDisPresaleAssetsIn.getBusinessType();
        String sourceNo = updateDisPresaleAssetsIn.getSourceNo();
        updateDisPresaleAssetsIn.getAssetsGoodsInList().forEach(item -> ordDisPresaleAssetsDetailService.updatePresaleAssetsDetail(item, assetsId, loginUsername, businessType, sourceNo));
    }

    @Override
    public Page<OrdDisPresaleAssetsPageOut> findStorePresaleAssetsForPage(QueryPresaleAssetsPageIn queryPresaleAssetsPageIn) {
        if (StringUtils.isNotEmpty(queryPresaleAssetsPageIn.getStoreArea())) {
            List<String> storeCodeList = new ArrayList<>();
            List<StoreInfoOut> storeList = storeCenterService.getStoreInfoByCode(new StoreInfoIn(queryPresaleAssetsPageIn.getStoreArea()));
            if (CollectionUtils.isEmpty(storeList)) {
                return new Page<>(queryPresaleAssetsPageIn);
            }
            storeList.forEach(storeInfoOut -> storeCodeList.add(storeInfoOut.getErpStoreCode()));
            queryPresaleAssetsPageIn.setStoreCodeList(storeCodeList);
        }
        List<OrdDisPresaleAssetsPageOut> list = ordDisPresaleAssetsMapper.findStorePresaleAssetsByPage(queryPresaleAssetsPageIn);
        Page<OrdDisPresaleAssetsPageOut> page = new Page<>(queryPresaleAssetsPageIn);
        page.setList(list);
        return page;
    }

    @Override
    public String exportStorePresaleAssets(QueryPresaleAssetsPageIn queryPresaleAssetsPageIn) {
        String title = "预售商品资产";
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        Page<OrdDisPresaleAssetsPageOut> resultPage = this.findStorePresaleAssetsForPage(queryPresaleAssetsPageIn);
        List<ExcelOrdDisPresaleAssetsDetail> exportPresaleAssetsDetailList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(resultPage.getList())) {
            resultPage.getList().forEach(assets -> {
                QueryPresaleAssetsDetailPageIn queryPresaleAssetsDetailPageIn = new QueryPresaleAssetsDetailPageIn();
                queryPresaleAssetsDetailPageIn.setAssetsId(assets.getId());
                Page<OrdDisPresaleAssetsDetailOut> detailOutPage = ordDisPresaleAssetsDetailService.findPresaleAssetsDetailForPage(queryPresaleAssetsDetailPageIn);
                if (CollectionUtils.isNotEmpty(detailOutPage.getList())) {
                    detailOutPage.getList().forEach(assetsDetail -> {
                        ExcelOrdDisPresaleAssetsDetail exportPresaleAssetsDetail = new ExcelOrdDisPresaleAssetsDetail();
                        org.springframework.beans.BeanUtils.copyProperties(assetsDetail, exportPresaleAssetsDetail);
                        exportPresaleAssetsDetail.setStoreCode(assets.getStoreCode());
                        exportPresaleAssetsDetail.setStoreName(assets.getStoreName());
                        exportPresaleAssetsDetailList.add(exportPresaleAssetsDetail);
                    });
                }
            });
        }
        byte[] bytes = FileExportUtil.getFileBytesByData(exportPresaleAssetsDetailList, "预售商品资产", "预售商品资产", ExcelOrdDisPresaleAssetsDetail.class, true);
        return fileService.uploadFile(fileName, bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    @Override
    public OrdDisPresaleAssets getOrdDisPresaleAssetsById(Long id) {
        return ordDisPresaleAssetsMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrdDisPresaleAssets(OrdDisPresaleAssets ordDisPresaleAssets) {
        ordDisPresaleAssetsMapper.insert(ordDisPresaleAssets);
    }
}
