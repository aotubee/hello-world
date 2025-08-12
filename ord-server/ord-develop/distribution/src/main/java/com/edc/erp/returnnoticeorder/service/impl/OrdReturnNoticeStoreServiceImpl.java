package com.edc.erp.returnnoticeorder.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNoticeStore;
import com.edc.erp.returnnoticeorder.enumeration.OrdReturnNoticeTypeEnum;
import com.edc.erp.returnnoticeorder.listener.*;
import com.edc.erp.returnnoticeorder.mapper.OrdDisReturnNoticeStoreMapper;
import com.edc.erp.returnnoticeorder.model.in.ImportOrdReturnNoticeStore;
import com.edc.erp.returnnoticeorder.model.in.OrdReturnNoticeDetailIn;
import com.edc.erp.returnnoticeorder.model.in.OrdReturnNoticeStoreIn;
import com.edc.erp.returnnoticeorder.model.out.ExcelReturnNoticeStoreNoQtyOut;
import com.edc.erp.returnnoticeorder.model.out.ExcelReturnNoticeStoreOut;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.erp.returnnoticeorder.service.OrdReturnNoticeStoreService;

import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 退货通知单与门店表(DisReturnNoticeStore)表服务实现类
 *
 * @author yaojinpeng
 * @since 2022-10-21 10:55:56
 */
@Service
@RequiredArgsConstructor
public class OrdReturnNoticeStoreServiceImpl extends BaseServiceImpl<OrdDisReturnNoticeStore> implements OrdReturnNoticeStoreService {
    private final OrdDisReturnNoticeStoreMapper ordDisReturnNoticeStoreMapper;

    private final FileService fileService;

    private final StoreCenterService storeCenterService;

    @Override
    public Page<OrdReturnNoticeStoreOut> findReturnNoticeStoreOutForPage(OrdReturnNoticeStoreIn returnNoticeStoreIn) {
        List<OrdReturnNoticeStoreOut> returnNoticeStoreOutList = ordDisReturnNoticeStoreMapper.findPpReturnNoticeStoresByPage(returnNoticeStoreIn);
        Page<OrdReturnNoticeStoreOut> resultPage = new Page<>(returnNoticeStoreIn);
        resultPage.setList(returnNoticeStoreOutList);
        return resultPage;
    }

    @Override
    public String exportOrdReturnStore(OrdReturnNoticeStoreIn returnNoticeStoreIn) {
        Page<OrdReturnNoticeStoreOut> outForPage = this.findReturnNoticeStoreOutForPage(returnNoticeStoreIn);
        byte[] bytes;
        if(returnNoticeStoreIn.getReturnType().equals(OrdReturnNoticeTypeEnum.LIMITED_RETURN.getKey())){
            List<ExcelReturnNoticeStoreOut> excelReturnNoticeOuts = parseToExcel(outForPage.getList());
            bytes = FileExportUtil.getFileBytesByData(excelReturnNoticeOuts,
                    "退货通知单-门店信息",
                    "退货通知单-门店信息",
                    ExcelReturnNoticeStoreOut.class,
                    true);
        }else {
            List<ExcelReturnNoticeStoreNoQtyOut> excelReturnNoticeOuts = NotQtyParseToExcel(outForPage.getList());
            bytes = FileExportUtil.getFileBytesByData(excelReturnNoticeOuts,
                    "退货通知单-门店信息",
                    "退货通知单-门店信息",
                    ExcelReturnNoticeStoreNoQtyOut.class,
                    true);

        }

        return fileService.uploadFile("退货通知单-门店信息"+".xlsx",bytes, SystemConstant.SYSTEM_CODE,SystemConstant.SYSTEM_NAME);
    }

    /**
     * 将查询得到的列表集合转换为导出集合
     *
     * @param list
     * @return
     */
    private List<ExcelReturnNoticeStoreOut> parseToExcel(List<OrdReturnNoticeStoreOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertDtlExcel(list.get(i), i))
                .collect(Collectors.toList());
    }
    private List<ExcelReturnNoticeStoreNoQtyOut> NotQtyParseToExcel(List<OrdReturnNoticeStoreOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> NotQtyConvertDtlExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 将 OrdReturnNoticeStoreOut 转换为导出 ExcelReturnNoticeStoreOut
     *
     * @param ordReturnNoticeStoreOut
     * @param index
     * @return
     */
    private ExcelReturnNoticeStoreOut convertDtlExcel(OrdReturnNoticeStoreOut ordReturnNoticeStoreOut, int index) {
        ExcelReturnNoticeStoreOut excelReturnNoticeStoreOut = new ExcelReturnNoticeStoreOut();
        BeanUtils.copy(ordReturnNoticeStoreOut, excelReturnNoticeStoreOut);
        excelReturnNoticeStoreOut.setIndex(index + 1);
        return excelReturnNoticeStoreOut;
    }
    private ExcelReturnNoticeStoreNoQtyOut NotQtyConvertDtlExcel(OrdReturnNoticeStoreOut ordReturnNoticeStoreOut, int index) {
        ExcelReturnNoticeStoreNoQtyOut excelReturnNoticeStoreNoQtyOut = new ExcelReturnNoticeStoreNoQtyOut();
        BeanUtils.copy(ordReturnNoticeStoreOut, excelReturnNoticeStoreNoQtyOut);
        excelReturnNoticeStoreNoQtyOut.setIndex(index + 1);
        return excelReturnNoticeStoreNoQtyOut;
    }

    @Override
    public Response<List<OrdReturnNoticeStoreOut>> importReturnNoticeStore(String fileId,  String bizOrgCode,String returnType) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (bytes == null){
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OrdDisReturnStoreListener listener = new OrdDisReturnStoreListener(storeCenterService,bizOrgCode,returnType);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdReturnNoticeStore.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        return Response.data(listener.getReturnNoticeStoreDetail(), listener.message());
    }

    @Override
    public void deleteByReturnNoticeOrderId(Integer returnNoticeOrderId) {
        OrdDisReturnNoticeStore ordDisReturnNoticeStore = new OrdDisReturnNoticeStore();
        ordDisReturnNoticeStore.setReturnNoticeOrderId(returnNoticeOrderId);
        ordDisReturnNoticeStoreMapper.delete(ordDisReturnNoticeStore);
    }

    @Override
    public List<OrdReturnNoticeStoreOut> findStoreInfo(OrdReturnNoticeDetailIn ordReturnNoticeIn, Integer returnNoticeGoodsId) {
        OrdDisReturnNoticeStore ordDisReturnNoticeStore = new OrdDisReturnNoticeStore();
        ordDisReturnNoticeStore.setReturnNoticeOrderId(ordReturnNoticeIn.getReturnNoticeOrderId());
        ordDisReturnNoticeStore.setReturnNoticeGoodsId(returnNoticeGoodsId);
        if(StringUtils.isNotBlank(ordReturnNoticeIn.getStoreCode())){
            ordDisReturnNoticeStore.setStoreCode(ordReturnNoticeIn.getStoreCode());
        }
        List<OrdReturnNoticeStoreOut> storeInfo = ordDisReturnNoticeStoreMapper.findStoreInfo(ordDisReturnNoticeStore);
        storeInfo.forEach(item->
                item.setReturnNum(item.getQty()));
        return storeInfo;
    }

    @Override
    public BigDecimal getMaxQtyByParameter(String goodsCode, String storeCode, Integer id) {
        BigDecimal maxQuantity = ordDisReturnNoticeStoreMapper.getMaxQtyByParameter(goodsCode, storeCode, id);
        return null != maxQuantity ? maxQuantity : BigDecimal.ZERO;
    }
}
