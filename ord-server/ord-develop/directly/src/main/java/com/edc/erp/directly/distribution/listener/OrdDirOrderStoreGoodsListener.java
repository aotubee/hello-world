package com.edc.erp.directly.distribution.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.model.entity.GoodsStatusBusinessSwitch;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.StandardSpecTransInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.distribution.model.in.ImportOrdOrderStoreGoodsVO;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderDistributionDetailOut;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分货门店商品数量导入监听器
 *
 * @author lx
 * @since 2022-11-24 15:44:11
 */
@Slf4j
@Data
public class OrdDirOrderStoreGoodsListener extends ImportListener<ImportOrdOrderStoreGoodsVO>{

    /**
     * 每隔5条存储数据库，实际使用中可以1000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 1000;

    /**
     * 临时存放数据
     */
    List<OrdDirOrderDistributionDetailOut> distributionStoreGoodsList = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(2);

    private StoreCenterService storeCenterService;

    private OrderGoodsServer orderGoodsServer;

    Map<String, List<String>> storeGoodsMap = new HashMap<>();

    Map<String, Map<String, ImportOrdOrderStoreGoodsVO>> storeDataMap = new HashMap<>();

    public OrdDirOrderStoreGoodsListener(StoreCenterService storeCenterService, OrderGoodsServer orderGoodsServer) {
        this.storeCenterService = storeCenterService;
        this.orderGoodsServer = orderGoodsServer;
    }

    @Override
    public void invoke(ImportOrdOrderStoreGoodsVO data, AnalysisContext context) {
        Assert.notNull(data.getStoreCode(), () -> {
            throw new BusinessException("门店代码不能为空;");
        });
        Assert.notNull(data.getSkuCode(), () -> {
            throw new BusinessException("商品代码不能为空;");
        });
        Assert.notNull(data.getDistributionQuantity(),() -> {
            throw new BusinessException("分货数量不能为空;");
        });
        Assert.isTrue(Objects.nonNull(data.getDistributionQuantity()) && BigDecimal.ZERO.compareTo(data.getDistributionQuantity()) != NumberUtil.INTEGER_ZERO,() -> {
            throw new BusinessException("分货数量不能为0;");
        });
        Assert.isTrue(NumberUtil.INTEGER_ZERO < data.getDistributionQuantity().compareTo(BigDecimal.ZERO),() -> {
            throw new BusinessException("分货数量不能为负数;");
        });

        //判断list中是否存商品代码
        if (distributionStoreGoodsList.stream().anyMatch(a -> (a.getStoreCode() + "&" + a.getGoodsCode()).equals(data.getStoreCode() + "&" + data.getSkuCode()))) {
            throw new BusinessException(data.getStoreCode() + "&" + data.getSkuCode() + "：当前集合中的门店代码和商品代码组合重复;");
        }

        //校验门店是否存在
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(data.getStoreCode());
        Assert.notNull(storeOut,() -> {
            throw new BusinessException("门店" + data.getStoreCode() + "不存在;");
        });

        //校验门店是否是配货
        Assert.isTrue(StoreConstant.StoreProperty.DIRECTLY.getMytValue().equals(storeOut.getStoreType()),() ->{
            throw new BusinessException(data.getStoreCode() + "门店是加盟门店，不可在直营订货管理分货！;");
        });

        List<String> codes;
        Map<String, ImportOrdOrderStoreGoodsVO> datas;
        if (storeGoodsMap.containsKey(data.getStoreCode())) {
            codes = storeGoodsMap.get(data.getStoreCode());
            datas = storeDataMap.get(data.getStoreCode());
        } else {
            codes = new ArrayList<>();
            datas = new HashMap<>();
        }
        codes.add(data.getSkuCode());
        storeGoodsMap.put(data.getStoreCode(), codes);
        datas.put(data.getSkuCode(), data);
        storeDataMap.put(data.getStoreCode(), datas);
        atomicInteger.getAndIncrement();
    }


    /**
     * 校验商品
     * @return
     */
    public String checkGoodsInfoAndInitData(){
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, List<String>> storeListEntry : storeGoodsMap.entrySet()) {
            String storeCode = storeListEntry.getKey();
            //查询允许分货商品信息入参
            OrderGoodsIn goodsIn = new OrderGoodsIn();
            goodsIn.setStoreCode(storeCode);
            goodsIn.setBizOrgCode(UserUtil.getBizOrgCode());
            goodsIn.setGoodsCodeList(storeListEntry.getValue());
            //查询允许分货商品信息
            List<OrderGoodsOut> goodsOuts = orderGoodsServer.findGoodsInfoByStoreAndGoodsCodes(goodsIn);
            if (CollectionUtils.isEmpty(goodsOuts)) {
                stringBuilder.append(storeCode).append("门店商品均不可用;");
                continue;
            }
            Map<String, ImportOrdOrderStoreGoodsVO> storeGoodsVOMap = storeDataMap.get(storeCode);
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(storeCode);
            for (OrderGoodsOut goodsOut : goodsOuts) {
                //门店商品上下架
                if(NumberUtil.INTEGER_ONE.equals(goodsOut.getIsShelves())){
                    stringBuilder.append(storeCode).append("门店下的").append(goodsOut.getGoodsCode()).append("商品已下架;");
                    storeGoodsVOMap.remove(goodsOut.getGoodsCode());
                    continue;
                }
                //商品开关信息
                GoodsStatusBusinessSwitch goodsStatusBusinessSwitch = goodsOut.getGoodsStatusBusinessSwitch();
                if (Objects.isNull(goodsStatusBusinessSwitch) || NumberUtil.INTEGER_ZERO.equals(goodsStatusBusinessSwitch.getIsAllot())) {
                    stringBuilder.append("商品").append(goodsOut.getGoodsCode()).append("不可被分货;");
                    storeGoodsVOMap.remove(goodsOut.getGoodsCode());
                    continue;
                }
                //校验规格信息是否存在
                StandardSpecTransInfoOut distributionSpecification = goodsOut.getDistributionSpecification();
                if(Objects.isNull(distributionSpecification)){
                    stringBuilder.append("商品").append(goodsOut.getGoodsCode()).append("配货规格不存在;");
                    storeGoodsVOMap.remove(goodsOut.getGoodsCode());
                    continue;
                }
                //校验配货数量是否合法
                Integer qpc = distributionSpecification.getQpc();
                if(Objects.isNull(qpc)){
                    stringBuilder.append("商品").append(goodsOut.getGoodsCode()).append("配货规格数量不合法;");
                    storeGoodsVOMap.remove(goodsOut.getGoodsCode());
                    continue;
                }
                //校验商品规格是否合法
                String qpcStr = distributionSpecification.getQpcStr();
                if(StringUtils.isBlank(qpcStr)){
                    stringBuilder.append("商品").append(goodsOut.getGoodsCode()).append("配货规格不合法;");
                    storeGoodsVOMap.remove(goodsOut.getGoodsCode());
                    continue;
                }
                if(StringUtils.isBlank(goodsOut.getDistributionWay())){
                    stringBuilder.append("门店").append(storeCode).append("所属配送方案不存在该商品").append(goodsOut.getGoodsCode()).append(";");
                    storeGoodsVOMap.remove(goodsOut.getGoodsCode());
                    continue;
                }
                if(!DistributionWaysEnum.UNIFIEDDIS.getType().equals(goodsOut.getDistributionWay())
                        && !DistributionWaysEnum.TRANSFER.getType().equals(goodsOut.getDistributionWay())){
                    stringBuilder.append("当前配送方式是：").append(DistributionWaysEnum.getNameByType(goodsOut.getDistributionWay())).append(",不是统配或中转").append(";");
                    storeGoodsVOMap.remove(goodsOut.getGoodsCode());
                    continue;
                }
                //校验仓位是否存在
                if(StringUtils.isBlank(goodsOut.getStockCode())){
                    stringBuilder.append("门店").append(storeCode).append("商品").append(goodsOut.getGoodsCode()).append("仓位为空;");
                    storeGoodsVOMap.remove(goodsOut.getGoodsCode());
                    continue;
                }
                //校验配送价
                if(Objects.isNull(goodsOut.getDistributionUnitPrice())){
                    stringBuilder.append("门店").append(storeCode).append("商品").append(goodsOut.getGoodsCode()).append("配送价为空;");
                    storeGoodsVOMap.remove(goodsOut.getGoodsCode());
                    continue;
                }
                ImportOrdOrderStoreGoodsVO dataVo = storeGoodsVOMap.get(goodsOut.getGoodsCode());
                storeGoodsVOMap.remove(goodsOut.getGoodsCode());
                initDetail(dataVo, storeOut, goodsOut);
            }
            if (storeGoodsVOMap.size() > 0) {
                stringBuilder.append("门店").append(storeCode).append("商品");
                for (String sku : storeGoodsVOMap.keySet()) {
                    stringBuilder.append(sku).append(SystemConstant.COMMA);
                }
                stringBuilder.append("不存在;");
            }
        }
        return stringBuilder.toString();
    }
//    /**
//     * 校验商品
//     * @param data excel导入数据
//     * @return
//     */
//    public OrderGoodsOut checkGoodsInfo(ImportOrdOrderStoreGoodsVO data){
//        //查询允许分货商品信息入参
//        OrderGoodsIn goodsIn = new OrderGoodsIn();
//        goodsIn.setStoreCode(data.getStoreCode());
//        goodsIn.setBizOrgCode(UserUtil.getBizOrgCode());
//        goodsIn.setGoodsCode(data.getSkuCode());
//        //查询允许分货商品信息
//        OrderGoodsOut goodsOut = orderGoodsServer.getSwitchGoodsInfo(goodsIn);
//        Assert.notNull(goodsOut,() -> {
//            throw new BusinessException("商品不存在;");
//        });
//
//        //门店商品上下架
//        if(NumberUtil.INTEGER_ONE.equals(goodsOut.getIsShelves())){
//            throw new BusinessException(data.getStoreCode() + "门店下的" + data.getSkuCode() + "商品已下架;");
//        }
//
//        //商品开关信息
//        GoodsStatusBusinessSwitch goodsStatusBusinessSwitch = goodsOut.getGoodsStatusBusinessSwitch();
//        Assert.notNull(goodsStatusBusinessSwitch,() -> {
//            throw new BusinessException("商品" + data.getSkuCode() + "不可被分货;");
//        });
//        Assert.isTrue(NumberUtil.INTEGER_ONE.equals(goodsStatusBusinessSwitch.getIsAllot()), () ->{
//            throw new BusinessException("商品" + data.getSkuCode() + "不可被分货;");
//        });
//
//
//
//        //校验规格信息是否存在
//        StandardSpecTransInfoOut distributionSpecification = goodsOut.getDistributionSpecification();
//        Assert.notNull(distributionSpecification,() -> {
//            throw new BusinessException("商品" + data.getSkuCode() + "配货规格不存在;");
//        });
//        //校验配货数量是否合法
//        Integer qpc = goodsOut.getDistributionSpecification().getQpc();
//        Assert.notNull(qpc,() -> {
//            throw new BusinessException("商品" + data.getSkuCode() + "配货规格数量不合法");
//        });
//        //校验商品规格是否合法
//        String qpcStr = goodsOut.getDistributionSpecification().getQpcStr();
//        Assert.notNull(qpcStr,() -> {
//            throw new BusinessException("商品" + data.getSkuCode() + "配货规格不合法;");
//        });
//
//        Assert.notNull(goodsOut.getDistributionWay(),() -> {
//            throw new BusinessException("门店" + data.getStoreCode() +"所属配送方案不存在该商品" + data.getSkuCode() + ";");
//        });
//
//        if(!DistributionWaysEnum.UNIFIEDDIS.getType().equals(goodsOut.getDistributionWay())
//                && !DistributionWaysEnum.TRANSFER.getType().equals(goodsOut.getDistributionWay())){
//            throw new BusinessException("当前配送方式是：" + DistributionWaysEnum.getNameByType(goodsOut.getDistributionWay()) + ",不是统配或中转" + ";");
//        }
//
//        //校验仓位是否存在
//        Assert.notNull(goodsOut.getStockCode(),() ->{
//            throw new BusinessException("仓位为空;");
//        });
//        Assert.notNull(goodsOut.getStockName(),() ->{
//            throw new BusinessException("仓位为空;");
//        });
//        //校验配送价
//        Assert.notNull(goodsOut.getDistributionUnitPrice(),() ->{
//            throw new BusinessException("商品" + data.getSkuCode() + "配送价为空;");
//        });
//
//        return goodsOut;
//    }

    /**
     * 保存明细至结果集
     * @param data excel      导入数据
     * @param storeOut        门店信息
     * @param orderGoodsOut   商品信息
     * @param
     */
    private void initDetail(ImportOrdOrderStoreGoodsVO data,StoreOut storeOut,OrderGoodsOut orderGoodsOut) {
        //结果集
        OrdDirOrderDistributionDetailOut detailOut = new OrdDirOrderDistributionDetailOut();
        //门店名称
        detailOut.setStoreName(storeOut.getStoreName());
        //门店代码
        detailOut.setStoreCode(data.getStoreCode());
        //商品代码
        detailOut.setGoodsCode(data.getSkuCode());
        //商品名称
        detailOut.setGoodsName(orderGoodsOut.getGoodsName());
        //分货数量
        detailOut.setDistributionQuantity(data.getDistributionQuantity());
        //包装规格
        detailOut.setDistributionSpecification(orderGoodsOut.getDistributionSpecification().getQpcStr());
        //计算包装数
        Integer qpc = orderGoodsOut.getDistributionSpecification().getQpc();
        //添加包装数
        detailOut.setPackingNumber(data.getDistributionQuantity().divide(new BigDecimal(qpc), 0, RoundingMode.UP));
        //配货单价
        detailOut.setOriginalPrice(orderGoodsOut.getDistributionUnitPrice());
        //当前库存数量
        detailOut.setWrhInvQty(orderGoodsOut.getStockQuantity());
        //分货金额
        detailOut.setDistributionAmount(orderGoodsOut.getDistributionUnitPrice().multiply(data.getDistributionQuantity()));
        //批量添加
        distributionStoreGoodsList.add(detailOut);
    }


    public Response<List<OrdDirOrderDistributionDetailOut>> getResponse() {
        /**按照商品代码和门店代码对导入的数据去重*/
//        List<OrdDirOrderDistributionDetailOut> detailOuts = distributionStoreGoodsList.stream().collect(
//                Collectors.collectingAndThen(
//                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getGoodsCode() + "&" + s.getStoreCode()))), ArrayList::new)
//        );
        String msg = this.checkGoodsInfoAndInitData();

        List<OrdDirOrderDistributionDetailOut> detailOuts = distributionStoreGoodsList;
        String message = "";
        message += "成功导入" + detailOuts.size() + "条数据。\n";
        int num = Math.max(0, atomicInteger.get() - detailOuts.size() - 2);
        message += "失败" + num + "条数据。;\n";
        message += String.join("\n", this.getErrorDate());
        message += String.join("\n", msg);
        return Response.data(detailOuts, message);
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if(distributionStoreGoodsList.size() >= BATCH_COUNT) {
            distributionStoreGoodsList.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
        log.info("ordDirOrderStoreGoodsListener解析完成");
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("ordDirOrderStoreGoodsListener解析发生异常:{}", exception.getMessage(), exception);
        if (exception instanceof ExcelDataConvertException) {
            this.encapsulateErrorMap("数据格式错误；");
        } else if (exception instanceof BusinessException) {
            this.encapsulateErrorMap(exception.getMessage());
        } else {
            this.encapsulateErrorMap("未知错误；");
        }
    }

    private void encapsulateErrorMap(String error) {
        Map<String, String> map = new LinkedHashMap<>(2);
        map.put("key", "第【" + atomicInteger.get() + "】行：");
        map.put("value", error);
        this.errorList.add(map);
        atomicInteger.getAndIncrement();
    }
}
