package com.edc.erp.directly.returnnoticeorder.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNotice;
import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNoticeStore;
import com.edc.erp.directly.returnnoticeorder.enumeration.OrdReturnNoticeTypeEnum;
import com.edc.erp.directly.returnnoticeorder.mapper.OrdDirReturnNoticeStoreMapper;
import com.edc.erp.directly.returnnoticeorder.model.excel.ExcelReturnNoticeStoreNotQtyOut;
import com.edc.erp.directly.returnnoticeorder.model.excel.ExcelReturnNoticeStoreOut;
import com.edc.erp.directly.returnnoticeorder.model.in.OrdReturnNoticeDetailIn;
import com.edc.erp.directly.returnnoticeorder.model.in.OrdReturnNoticeStoreIn;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.erp.directly.returnnoticeorder.service.OrdDirReturnNoticeService;
import com.edc.erp.directly.returnnoticeorder.service.OrdDirReturnNoticeStoreService;
import com.edc.erp.directly.util.FileExportUtil;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 退货通知单与门店表(OrdDirReturnNoticeStore)表服务实现类
 *
 * @author
 * @since 2022-11-18 19:09:03
 */
@Service
@RequiredArgsConstructor
public class OrdDirReturnNoticeStoreServiceImpl extends BaseServiceImpl<OrdDirReturnNoticeStore> implements OrdDirReturnNoticeStoreService {
     
     private final OrdDirReturnNoticeStoreMapper ordDirReturnNoticeStoreMapper;

     private final FileService fileService;


     @Override
     public List<OrdReturnNoticeStoreOut> findStoreInfo(OrdReturnNoticeDetailIn ordReturnNoticeIn, Integer id) {
          OrdDirReturnNoticeStore ordDirReturnNoticeStore = new OrdDirReturnNoticeStore();
          ordDirReturnNoticeStore.setReturnNoticeOrderId(ordReturnNoticeIn.getReturnNoticeOrderId());
          ordDirReturnNoticeStore.setReturnNoticeGoodsId(id);
          if(StringUtils.isNotBlank(ordReturnNoticeIn.getStoreCode())){
               ordDirReturnNoticeStore.setStoreCode(ordReturnNoticeIn.getStoreCode());
          }
          List<OrdReturnNoticeStoreOut> storeInfo = ordDirReturnNoticeStoreMapper.findStoreInfo(ordDirReturnNoticeStore);
          return storeInfo;
     }

     /**
      * 根据退货通知单主键删除门店信息
      *
      * @param returnNoticeOrderId
      */
     @Override
     @Transactional(rollbackFor = Exception.class)
     public void deleteByReturnNoticeOrderId(Integer returnNoticeOrderId) {
          OrdDirReturnNoticeStore ordDirReturnNoticeStore = new OrdDirReturnNoticeStore();
          ordDirReturnNoticeStore.setReturnNoticeOrderId(returnNoticeOrderId);
          ordDirReturnNoticeStoreMapper.delete(ordDirReturnNoticeStore);
     }

     /**
      * 根据退货通知单主键和商品主键查门店信息
      *
      * @param returnNoticeOrderId
      * @param returnNoticeGoodsId
      * @return
      */
     @Override
     public List<OrdReturnNoticeStoreOut> findByReturnNoticeIdAndReturnGoodsId(Integer returnNoticeOrderId, Integer returnNoticeGoodsId) {
          OrdDirReturnNoticeStore query = new OrdDirReturnNoticeStore();
          query.setReturnNoticeOrderId(returnNoticeOrderId);
          query.setReturnNoticeGoodsId(returnNoticeGoodsId);
          return ordDirReturnNoticeStoreMapper.findStoreInfo(query);
     }

     /**
      * 导出门店信息
      *
      * @param returnNoticeStoreIn
      * @return
      */
     @Override
     public String exportOrdReturnStore(OrdReturnNoticeStoreIn returnNoticeStoreIn) {
          Page<OrdReturnNoticeStoreOut> outForPage = this.findReturnNoticeStoreOutForPage(returnNoticeStoreIn);
          byte[] bytes ;
          if(returnNoticeStoreIn.getReturnType().equals(OrdReturnNoticeTypeEnum.LIMITED_RETURN.getKey())){
               List<ExcelReturnNoticeStoreOut> excelReturnNoticeOuts = parseToExcel(
                       outForPage.getList());
                bytes = FileExportUtil.getFileBytesByData(
                       excelReturnNoticeOuts,
                       "导出退货单门店明细",
                       "导出退货单门店明细",
                       ExcelReturnNoticeStoreOut.class,
                       true);
          }else {
               List<ExcelReturnNoticeStoreNotQtyOut> excelReturnNoticeOuts = notQtyParseToExcel(
                       outForPage.getList());
               bytes = FileExportUtil.getFileBytesByData(
                       excelReturnNoticeOuts,
                       "导出退货单门店明细",
                       "导出退货单门店明细",
                       ExcelReturnNoticeStoreNotQtyOut.class,
                       true);
          }

          return fileService.uploadFile("导出退货通知单门店明细"+".xlsx",bytes, SystemConstant.SYSTEM_CODE,SystemConstant.SYSTEM_NAME);
     }

     private Page<OrdReturnNoticeStoreOut> findReturnNoticeStoreOutForPage(OrdReturnNoticeStoreIn returnNoticeStoreIn) {
          List<OrdReturnNoticeStoreOut> returnNoticeStoreOutList = ordDirReturnNoticeStoreMapper.findReturnNoticeStoresByPage(returnNoticeStoreIn);
          Page<OrdReturnNoticeStoreOut> resultPage = new Page<>(returnNoticeStoreIn);
          resultPage.setList(returnNoticeStoreOutList);
          return resultPage;
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

     private List<ExcelReturnNoticeStoreNotQtyOut> notQtyParseToExcel(List<OrdReturnNoticeStoreOut> list) {
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

     private ExcelReturnNoticeStoreNotQtyOut NotQtyConvertDtlExcel(OrdReturnNoticeStoreOut ordReturnNoticeStoreOut, int index) {

          ExcelReturnNoticeStoreNotQtyOut excelReturnNoticeStoreNotQtyOut = new ExcelReturnNoticeStoreNotQtyOut();
          BeanUtils.copy(ordReturnNoticeStoreOut, excelReturnNoticeStoreNotQtyOut);
          excelReturnNoticeStoreNotQtyOut.setIndex(index + 1);
          return excelReturnNoticeStoreNotQtyOut;
     }
}
