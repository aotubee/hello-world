package com.edc.erp.directly.returnnoticeorder.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNoticeGoods;
import com.edc.erp.directly.returnnoticeorder.mapper.OrdDirReturnNoticeGoodsMapper;
import com.edc.erp.directly.returnnoticeorder.model.excel.ExcelReturnNoticeGoodsOut;
import com.edc.erp.directly.returnnoticeorder.model.in.OrdReturnNoticeDetailIn;
import com.edc.erp.directly.returnnoticeorder.model.in.ReturnNoticeGoodsIn;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.erp.directly.returnnoticeorder.service.OrdDirReturnNoticeGoodsService;
import com.edc.erp.directly.returnnoticeorder.service.OrdDirReturnNoticeStoreService;
import com.edc.erp.directly.util.FileExportUtil;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 退货通知单与商品表(OrdDirReturnNoticeGoods)表服务实现类
 *
 * @author
 * @since 2022-11-18 19:09:03
 */
@Service
@RequiredArgsConstructor
public class OrdDirReturnNoticeGoodsServiceImpl extends BaseServiceImpl<OrdDirReturnNoticeGoods> implements OrdDirReturnNoticeGoodsService {
     
     private final OrdDirReturnNoticeGoodsMapper ordDirReturnNoticeGoodsMapper;

     private final OrdDirReturnNoticeStoreService ordDirReturnNoticeStoreService;

     private final FileService fileService;

     /**
      * 根据退货通知单主键查单据下商品和门店信息
      *
      * @param ordReturnNoticeIn
      * @return
      */
     @Override
     public List<OrdReturnNoticeGoodsOut> findGoodsStoreInfoByReturnNoticeOrderId(OrdReturnNoticeDetailIn ordReturnNoticeIn) {
          OrdDirReturnNoticeGoods ordDirReturnNoticeGoods = new OrdDirReturnNoticeGoods();
          ordDirReturnNoticeGoods.setReturnNoticeOrderId(ordReturnNoticeIn.getReturnNoticeOrderId());
          if(StringUtils.isNotEmpty(ordReturnNoticeIn.getGoodsCode())){
               ordDirReturnNoticeGoods.setGoodsCode(ordReturnNoticeIn.getGoodsCode());
          }
          List<OrdReturnNoticeGoodsOut> ordReturnNoticeGoodsOutList =new ArrayList<>();
          List<OrdDirReturnNoticeGoods> goodInfoList = ordDirReturnNoticeGoodsMapper.select(ordDirReturnNoticeGoods);
          for (OrdDirReturnNoticeGoods dirReturnNoticeGoods : goodInfoList) {
               OrdReturnNoticeGoodsOut ordReturnNoticeGoodsOut = new OrdReturnNoticeGoodsOut();
               ordReturnNoticeGoodsOut.setBrandName(dirReturnNoticeGoods.getBrand());
               BeanUtils.copy(dirReturnNoticeGoods,ordReturnNoticeGoodsOut);
               ordReturnNoticeGoodsOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(dirReturnNoticeGoods.getGoodsType()));
               List<OrdReturnNoticeStoreOut> storeInfo =ordDirReturnNoticeStoreService.findStoreInfo(ordReturnNoticeIn,dirReturnNoticeGoods.getId());
               ordReturnNoticeGoodsOut.setStoreInfo(storeInfo);
               ordReturnNoticeGoodsOutList.add(ordReturnNoticeGoodsOut);
          }
          return ordReturnNoticeGoodsOutList;
     }

     /**
      * 根据退货通知单主键删除商品信息
      *
      * @param returnNoticeOrderId
      */
     @Override
     @Transactional(rollbackFor = Exception.class)
     public void deleteByReturnNoticeOrderId(Integer returnNoticeOrderId) {
          OrdDirReturnNoticeGoods ordDirReturnNoticeGoods = new OrdDirReturnNoticeGoods();
          ordDirReturnNoticeGoods.setReturnNoticeOrderId(returnNoticeOrderId);
          ordDirReturnNoticeGoodsMapper.delete(ordDirReturnNoticeGoods);
     }

     /**
      * 根据
      * @param returnNoticeOrderId
      * @return
      */
     @Override
     public List<OrdDirReturnNoticeGoods> findByReturnNoticeId(Integer returnNoticeOrderId) {
          OrdDirReturnNoticeGoods ordDirReturnNoticeGoods = new OrdDirReturnNoticeGoods();
          ordDirReturnNoticeGoods.setReturnNoticeOrderId(returnNoticeOrderId);
          return  ordDirReturnNoticeGoodsMapper.select(ordDirReturnNoticeGoods);
     }

     /**
      * 导出商品信息
      *
      * @param returnNoticeGoodsIn
      * @return
      */
     @Override
     public String exportOrdReturnGoods(ReturnNoticeGoodsIn returnNoticeGoodsIn) {
          Page<OrdReturnNoticeGoodsOut> outForPage = this.findReturnNoticeGoodsOutForPage(returnNoticeGoodsIn);
          List<ExcelReturnNoticeGoodsOut> excelReturnNoticeOuts = parseToExcel(outForPage.getList());
          byte[] bytes = FileExportUtil.getFileBytesByData(excelReturnNoticeOuts, "导出退货单商品明细", "导出退货单商品明细", ExcelReturnNoticeGoodsOut.class, true);
          return fileService.uploadFile("导出退货通知单商品明细"+".xlsx",bytes, SystemConstant.SYSTEM_CODE,SystemConstant.SYSTEM_NAME);
     }

     private Page<OrdReturnNoticeGoodsOut> findReturnNoticeGoodsOutForPage(ReturnNoticeGoodsIn returnNoticeGoodsIn) {
          List<OrdReturnNoticeGoodsOut> ordDisReturnNoticeGoods = ordDirReturnNoticeGoodsMapper.findGoodsOutByPage(returnNoticeGoodsIn);
          ordDisReturnNoticeGoods.forEach( returnNoticeOut -> {
               returnNoticeOut.setSortName(returnNoticeOut.getSortName()+"【"+returnNoticeOut.getSort()+"】");
          });
          Page<OrdReturnNoticeGoodsOut> page = new Page<>(returnNoticeGoodsIn);
          page.setList(ordDisReturnNoticeGoods);
          return page;
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
          excelRequestOrderDtlOut.setIndex(index + 1);
          excelRequestOrderDtlOut.setGoodsCode(ordReturnNoticeGoodsOut.getGoodsCode());
          excelRequestOrderDtlOut.setGoodsName(ordReturnNoticeGoodsOut.getGoodsName());
          excelRequestOrderDtlOut.setSpecification(ordReturnNoticeGoodsOut.getSpecification());
          excelRequestOrderDtlOut.setSortName(ordReturnNoticeGoodsOut.getSortName()+"【"+ordReturnNoticeGoodsOut.getSort()+"】");
          return excelRequestOrderDtlOut;
     }
}
