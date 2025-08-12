//package com.edc.erp.returnorder.listener;
//
//import com.alibaba.excel.context.AnalysisContext;
//import com.alibaba.excel.exception.ExcelDataConvertException;
//import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
//import com.edc.erp.common.enumeration.StoreConstant;
//import com.edc.erp.common.model.in.goods.OrderGoodsIn;
//import com.edc.erp.common.model.out.goods.OrderGoodsOut;
//import com.edc.erp.common.model.out.goods.OrgGoodsTransInfo;
//import com.edc.erp.common.model.out.stock.StockInfoOut;
//import com.edc.erp.common.model.out.store.StoreOut;
//import com.edc.erp.common.model.out.warehouse.WarehouseInfoOut;
//import com.edc.erp.common.service.OrderGoodsServer;
//import com.edc.erp.common.service.StockServer;
//import com.edc.erp.common.service.StoreCenterService;
//import com.edc.erp.common.service.WarehouseServer;
//import com.edc.erp.common.util.ImportListener;
//import com.edc.erp.common.util.NumberUtil;
//import com.edc.erp.returnorder.model.in.ImportOrdReturnOrderVO;
//import com.edc.plugins.common.exception.BusinessException;
//import lombok.NoArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//
//import java.math.BigDecimal;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
///**
// * 退货单导入监听器
// * @author lh
// */
//@Slf4j
//@NoArgsConstructor
//public class OrdReturnOrderListener extends ImportListener<ImportOrdReturnOrderVO> {
//    /**
//     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
//     */
//    private static final int BATCH_COUNT = 3000;
//
//    /**
//     * 临时存放数据 OrdReturnDetailOut
//     */
//    //List<BaseReturnOrderOut> baseReturnOrderOutList = new ArrayList<>();
//
//
//    List<ImportOrdReturnOrderVO> importOrdReturnOrderList  = new ArrayList<>();
//
//    private AtomicInteger atomicInteger = new AtomicInteger(2);
//
//
//    private StoreCenterService  storeCenterService;
//    private String bizOrgCode;
//    private StockServer stockServer;
//    private WarehouseServer warehouseServer;
//    private OrderGoodsServer orderGoodsServer;
//
//
//
//
//    public OrdReturnOrderListener(OrderGoodsServer orderGoodsServer,String bizOrgCode,StoreCenterService storeCenterService,StockServer stockServer,
//                              WarehouseServer warehouseServer ) {
//        this.orderGoodsServer=orderGoodsServer;
//        this.bizOrgCode = bizOrgCode;
//        this.storeCenterService=storeCenterService;
//        this.stockServer = stockServer;
//        this.warehouseServer =warehouseServer;
//    }
//
//    @Override
//    public void invoke(ImportOrdReturnOrderVO data, AnalysisContext context) {
//        String storeCode = data.getStoreCode();
//        String stockCode = data.getStockCode();
//        String warehouseCode = data.getWarehouseCode();
//        String goodsCode = data.getGoodsCode();
//        BigDecimal applyReturnQuantity = data.getApplyReturnQuantity();
//        //校验数据
//        checkNoNullData(storeCode,stockCode,warehouseCode,goodsCode,applyReturnQuantity);
//
//        importOrdReturnOrderList.add(data);
//        atomicInteger.getAndIncrement();
//    }
//
//    private void checkNoNullData( String storeCode,String stockCode,String warehouseCode,String goodsCode, BigDecimal applyReturnQuantity) {
//        if(StringUtils.isBlank(storeCode)){
//            throw new BusinessException("门店代码不能为空;");
//        }
//        if(StringUtils.isBlank(stockCode)){
//           throw  new BusinessException("仓位代码不能为空;");
//        }
//        if(StringUtils.isBlank(warehouseCode)){
//            throw  new BusinessException("仓储代码不能为空;");
//        }
//        if(StringUtils.isBlank(goodsCode)){
//            throw  new BusinessException("商品代码不能为空;");
//        }
//        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(storeCode);
//        if(Objects.isNull(storeOut) || !StoreConstant.StoreProperty.FRANCHISE.getMytValue().equals(storeOut.getStoreType())){
//           throw new BusinessException("门店不存在或类型不匹配;");
//        }
//
//        StockInfoOut stockInfoOut = stockServer.getByCode(stockCode,bizOrgCode);
//        if(Objects.nonNull(stockInfoOut)){
//            if( !NumberUtil.INTEGER_ONE.equals(stockInfoOut.getIsEnable())){
//                throw  new BusinessException("仓位状态为禁用;");
//            }
//        }else {
//           throw  new BusinessException("仓位代码不存在;");
//        }
//
//        boolean flag = stockServer.getReturnStockStatus(StoreConstant.StoreProperty.FRANCHISE.getMytValue(), stockCode, bizOrgCode);
//        if(!flag){
//            throw  new BusinessException(stockCode+"仓位不允许配销退货;");
//        }
//
//        WarehouseInfoOut warehouseInfoOut = warehouseServer.getByCode(warehouseCode, bizOrgCode);
//        if(Objects.nonNull(warehouseInfoOut)){
//            if( !NumberUtil.INTEGER_ONE.equals(warehouseInfoOut.getIsEnable())){
//                throw  new BusinessException("仓储状态为禁用;");
//            }
//        }else {
//            throw  new BusinessException("仓储代码不存在;");
//        }
//        boolean b = warehouseServer.checkStockIsWarehouseExist(warehouseCode, stockCode, bizOrgCode);
//        if (!b){
//            throw new BusinessException(stockCode+"仓位不在"+warehouseCode+"仓储下");
//        }
//
//        OrgGoodsTransInfo goodsOut = orderGoodsServer.getGoodsOut(goodsCode, bizOrgCode);
//        if(Objects.isNull(goodsOut)){
//            throw  new BusinessException("商品代码不存在;");
//        }
//
//        if( applyReturnQuantity.equals(NumberUtil.INTEGER_ZERO) ||applyReturnQuantity==null){
//            throw  new BusinessException("申请数量不能为空或者为0;");
//        }
//        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
//        orderGoodsIn.setGoodsCode(goodsCode);
//        orderGoodsIn.setStoreCode(storeCode);
//        orderGoodsIn.setBizOrgCode(bizOrgCode);
//        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
//        OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoodsForBackReturn(orderGoodsIn);
//        if(Objects.isNull(orderGoodsOut)){
//            throw  new BusinessException("门店或者商品状态不允许做配销退货;");
//        }
//        if(!warehouseCode.equals(orderGoodsOut.getReturnWarehouseCode())){
//           throw  new BusinessException (orderGoodsIn.getGoodsCode()+"商品退货仓储和所选退货仓储不符;");
//        }
//        if(!stockCode.equals(orderGoodsOut.getBackStockCode())){
//            throw  new BusinessException(orderGoodsIn.getGoodsCode()+"商品退货仓位和所选仓位不符;");
//        }
//    /*    BigDecimal num = applyReturnQuantity.divide(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()));
//        if( !isIntegerValue(num)){
//            throw  new BusinessException(goodsCode+"商品包装数是小数;");
//        }*/
//        //是否重复
//        List<ImportOrdReturnOrderVO> list = importOrdReturnOrderList.stream().filter(e -> e.getWarehouseCode().equals(warehouseCode) && e.getStockCode().equals(stockCode)
//                    && e.getStoreCode().equals(storeCode) && e.getGoodsCode().equals(goodsCode)).collect(Collectors.toList());
//        if (!CollectionUtils.isEmpty(list)){
//            throw new BusinessException("仓储代码"+warehouseCode+",仓位代码"+stockCode+",门店代码"+storeCode+
//                   ".商品代码"+goodsCode+"重复;");
//        }
//    }
//
//    @Override
//    public void doAfterAllAnalysed(AnalysisContext context) {
//        if (importOrdReturnOrderList.size() >= BATCH_COUNT) {
//            importOrdReturnOrderList.clear();
//        }
//    }
//
//    public List<ImportOrdReturnOrderVO> getImportOrdReturnOrderList(){
//        return importOrdReturnOrderList;
//    }
//
//    public String message(){
//        String message = "";
//        message += "成功导入" + importOrdReturnOrderList.size() + "条数据。";
//        int num = Math.max(0, atomicInteger.get() - importOrdReturnOrderList.size() - 2);
//        message += "失败" + num + "条数据。;";
//        message += String.join("\n", this.getErrorDate());
//        return message;
//    }
//
//
//    public String errorEessage(){
//        String message = "";
//        int num = Math.max(0, atomicInteger.get() - importOrdReturnOrderList.size() - 2);
//        message += "失败" + num + "条数据。;";
//        message += String.join("\n", this.getErrorDate());
//        return message;
//    }
//
//    @Override
//    public void onException(Exception exception, AnalysisContext context)  {
//        log.error("OrdDisReturnGoodsListener解析失败，发生异常:{}", exception.getMessage(), exception);
//        if (exception instanceof ExcelDataConvertException) {
//            this.encapsulateErrorMap("数据格式错误；");
//        } else if (exception instanceof BusinessException) {
//            this.encapsulateErrorMap(exception.getMessage());
//        } else {
//            this.encapsulateErrorMap("未知错误；");
//        }
//    }
//
//    private void encapsulateErrorMap(String error) {
//        Map<String, String> map = new LinkedHashMap<>(2);
//        map.put("key", "第【" + atomicInteger.get() + "】行：");
//        map.put("value", error);
//        this.errorList.add(map);
//        atomicInteger.getAndIncrement();
//    }
//
//    private boolean isIntegerValue(BigDecimal bd) {
//        return bd.stripTrailingZeros().scale() <= 0;
//    }
//
//}
