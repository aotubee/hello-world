package com.edc.erp.returnnoticeorder.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.service.AppUserService;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNoticeGoods;
import com.edc.erp.returnnoticeorder.listener.OrdDisReturnGoodsListener;
import com.edc.erp.returnnoticeorder.mapper.OrdDisReturnNoticeGoodsMapper;
import com.edc.erp.returnnoticeorder.model.in.ImportOrdReturnNoticeGoods;
import com.edc.erp.returnnoticeorder.model.in.OrdReturnNoticeDetailIn;
import com.edc.erp.returnnoticeorder.model.in.ReturnNoticeGoodsIn;
import com.edc.erp.returnnoticeorder.model.out.ExcelReturnNoticeGoodsOut;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.erp.returnnoticeorder.service.OrdReturnNoticeGoodsService;
import com.edc.erp.returnnoticeorder.service.OrdReturnNoticeStoreService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.uc.authority.util.UserUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 退货通知单与商品表(DisReturnNoticeGoods)表服务实现类
 *
 * @author yaojinpeng
 * @since 2022-10-21 10:55:55
 */
@Service
@RequiredArgsConstructor
public class OrdReturnNoticeGoodsServiceImpl extends BaseServiceImpl<OrdDisReturnNoticeGoods> implements OrdReturnNoticeGoodsService {

    private final OrdDisReturnNoticeGoodsMapper ordDisReturnNoticeGoodsMapper;

    private final AppUserService userService;

    private final OrderGoodsServer orderGoodsServer;

    private final FileService fileService;

    private final OrdReturnNoticeStoreService ordReturnNoticeStoreService;

    @Override
    public Page<OrdReturnNoticeGoodsOut> findReturnNoticeGoodsOutForPage(ReturnNoticeGoodsIn returnNoticeGoodsIn) {
        List<OrdReturnNoticeGoodsOut> ordDisReturnNoticeGoods = ordDisReturnNoticeGoodsMapper.findGoodsOutByPage(returnNoticeGoodsIn);
        ordDisReturnNoticeGoods.forEach( returnNoticeOut -> {
            returnNoticeOut.setSortName(returnNoticeOut.getSortName()+"【"+returnNoticeOut.getSort()+"】");
        });
        Page<OrdReturnNoticeGoodsOut> page = new Page<>(returnNoticeGoodsIn);
        page.setList(ordDisReturnNoticeGoods);
        return page;
    }

    @Override
    public String exportOrdReturnGoods(ReturnNoticeGoodsIn returnNoticeGoodsIn) {
        Page<OrdReturnNoticeGoodsOut> outForPage = this.findReturnNoticeGoodsOutForPage(returnNoticeGoodsIn);
        List<ExcelReturnNoticeGoodsOut> excelReturnNoticeGoodsOuts = parseToExcel(outForPage.getList());
        byte[] bytes = FileExportUtil.getFileBytesByData(excelReturnNoticeGoodsOuts, "退货通知单-商品信息", "退货通知单-商品信息", ExcelReturnNoticeGoodsOut.class, true);
        return fileService.uploadFile("退货通知单-商品信息"+".xlsx",bytes, SystemConstant.SYSTEM_CODE,SystemConstant.SYSTEM_NAME);
    }

    /**
     * 将查询得到的列表集合转换为导出集合
     *
     * @param list
     * @return
     */
    private List<ExcelReturnNoticeGoodsOut> parseToExcel(List<OrdReturnNoticeGoodsOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertDtlExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 将 OrdReturnNoticeGoodsOut 转换为导出 ExcelReturnOut
     *
     * @param ordReturnNoticeGoodsOut
     * @param index
     * @return
     */
    private ExcelReturnNoticeGoodsOut convertDtlExcel(OrdReturnNoticeGoodsOut ordReturnNoticeGoodsOut, int index) {
        ExcelReturnNoticeGoodsOut excelRequestOrderDtlOut = new ExcelReturnNoticeGoodsOut();
        BeanUtils.copy(ordReturnNoticeGoodsOut, excelRequestOrderDtlOut);
        excelRequestOrderDtlOut.setIndex(index + 1);
        excelRequestOrderDtlOut.setGoodsCode(ordReturnNoticeGoodsOut.getGoodsCode());
        excelRequestOrderDtlOut.setGoodsName(ordReturnNoticeGoodsOut.getGoodsName());
        return excelRequestOrderDtlOut;
    }

    @Override
    public Response<List<OrdReturnNoticeGoodsOut>> importReturnNoticeGoods(String fileId,List<String> goodsCodes, String bizOrgCode) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (bytes == null){
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OrdDisReturnGoodsListener listener = new OrdDisReturnGoodsListener(orderGoodsServer,goodsCodes,bizOrgCode);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdReturnNoticeGoods.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        return Response.data(listener.getReturnNoticeGoodsDetail(), listener.message());
    }

    /**
     * 校验商品是否符合退货
     *
     * @param goodsCode
     * @return
     */
    @Override
    public Response<OrdReturnNoticeGoodsOut> checkGoodsCode(String goodsCode) {
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(goodsCode);
        String bizOrgCode = UserUtil.getBizOrgCode();
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        OrderGoodsOut orderGoods = orderGoodsServer.getOrderGoods(orderGoodsIn);
        if(Objects.isNull(orderGoods)){
            return Response.error("商品不存在");
        }
        if(null !=orderGoods.getStatusId()){
          int status =ordDisReturnNoticeGoodsMapper.getGoodsStatusBusinessSwitchByStatusId(orderGoods.getStatusId(),bizOrgCode);
          if(status!=NumberUtil.INTEGER_ONE){
              return Response.error("该商品不可退");
          }
        }
        OrdReturnNoticeGoodsOut ordReturnNoticeGoodsOut = new OrdReturnNoticeGoodsOut();
        BeanUtils.copy(orderGoods,ordReturnNoticeGoodsOut);
        ordReturnNoticeGoodsOut.setSort(orderGoods.getSortName()+"【"+orderGoods.getSort()+"】");
        return Response.data(ordReturnNoticeGoodsOut);
    }

    @Override
    public void deleteByReturnNoticeOrderId(Integer returnNoticeOrderId) {
        OrdDisReturnNoticeGoods ordDisReturnNoticeGoods = new OrdDisReturnNoticeGoods();
        ordDisReturnNoticeGoods.setReturnNoticeOrderId(returnNoticeOrderId);
        ordDisReturnNoticeGoodsMapper.delete(ordDisReturnNoticeGoods);
    }

    @Override
    public List<OrdReturnNoticeGoodsOut> findGoodsStoreInfoByReturnNoticeOrderId(OrdReturnNoticeDetailIn ordReturnNoticeIn) {
        OrdDisReturnNoticeGoods ordDisReturnNoticeGoods = new OrdDisReturnNoticeGoods();
        ordDisReturnNoticeGoods.setReturnNoticeOrderId(ordReturnNoticeIn.getReturnNoticeOrderId());
        if(StringUtils.isNotEmpty(ordReturnNoticeIn.getGoodsCode())){
            ordDisReturnNoticeGoods.setGoodsCode(ordReturnNoticeIn.getGoodsCode());
        }
        List<OrdReturnNoticeGoodsOut> ordReturnNoticeGoodsOutList =new ArrayList<>();
        List<OrdDisReturnNoticeGoods> goodInfoList = ordDisReturnNoticeGoodsMapper.select(ordDisReturnNoticeGoods);
        for (OrdDisReturnNoticeGoods disReturnNoticeGoods : goodInfoList) {
            OrdReturnNoticeGoodsOut ordReturnNoticeGoodsOut = new OrdReturnNoticeGoodsOut();
            ordReturnNoticeGoodsOut.setBrandName(disReturnNoticeGoods.getBrand());
            BeanUtils.copy(disReturnNoticeGoods,ordReturnNoticeGoodsOut);
            ordReturnNoticeGoodsOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(disReturnNoticeGoods.getGoodsType()));
            List<OrdReturnNoticeStoreOut> storeInfo =ordReturnNoticeStoreService.findStoreInfo(ordReturnNoticeIn,disReturnNoticeGoods.getId());
            ordReturnNoticeGoodsOut.setStoreInfo(storeInfo);
            ordReturnNoticeGoodsOutList.add(ordReturnNoticeGoodsOut);
        }
        return ordReturnNoticeGoodsOutList;
    }
}
