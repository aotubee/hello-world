package com.edc.erp.directly.dirfirstorder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirfirstorder.model.excel.ImportFirstOrderDetail;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDirOrderFirstDetailOut;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 铺货单导入
 *
 * @author weichao
 * @since 2022-11-03 17:38:01
 */
@Slf4j
public class FirstDirOrderDetailListener extends ImportListener<ImportFirstOrderDetail> {

    /**
     * 每隔5条存储数据库，实际使用中可以1000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 1000;

    /**
     * 起始行
     */
    private final AtomicInteger atomicInteger = new AtomicInteger(2);

    private final List<OrdDirOrderFirstDetailOut> outDetails = new ArrayList<>();

    private final OrderGoodsServer orderGoodsServer;

    private final String storeCode;

    private final StoreChannelHandle storeChannelHandle;

    List<String> goodsCodes = new ArrayList<>();

    public FirstDirOrderDetailListener(OrderGoodsServer orderGoodsServer
            , String storeCode, List<String> goodsCodes,StoreChannelHandle storeChannelHandle
    ) {
        this.orderGoodsServer = orderGoodsServer;
        this.storeCode = storeCode;
        this.goodsCodes = goodsCodes;
        this.storeChannelHandle = storeChannelHandle;
    }

    public Response<List<OrdDirOrderFirstDetailOut>> getResponse() {
        /**按商品code对导入的数据去重*/
        List<OrdDirOrderFirstDetailOut> detailOuts = outDetails.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(OrdDirOrderFirstDetailOut::getGoodsCode))), ArrayList::new)
        );
        String message = "";
        message += "成功导入" + detailOuts.size() + "条数据。\n";
        int num = Math.max(0, atomicInteger.get() - detailOuts.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return Response.data(detailOuts, message);
    }

    public List<OrdDirOrderFirstDetailOut> getOutDetails() {
        return outDetails;
    }

    @Override
    public void invoke(ImportFirstOrderDetail data, AnalysisContext context) {
        Assert.notNull(data.getGoodsCode(), () -> {
            throw new BusinessException("商品代码不能为空;");
        });
        Assert.notNull(data.getDistributionNum(), () -> {
            throw new BusinessException("铺货数量不能为空;");
        });
        if (Objects.nonNull(data.getDistributionNum()) && data.getDistributionNum() <= 0) {
            throw new BusinessException("铺货数量必须大于0;");
        }
        //判断list中是否存商品代码
        if (outDetails.stream().anyMatch(a -> a.getGoodsCode().equals(data.getGoodsCode()))) {
            throw new BusinessException(data.getGoodsCode() + "：商品代码重复;");
        }

        if (CollectionUtils.isNotEmpty(goodsCodes)){
            boolean result = goodsCodes.stream().anyMatch(a -> data.getGoodsCode().equals(a));
            if (result){
                throw  new BusinessException(data.getGoodsCode()+"商品代码明细列表已存在;");
            }
        }

        //获取商品信息
        OrderGoodsOut orderGoodsOut = this.getGoodsInfo(data);
        //保存明细至结果集
        this.initDetail(data, orderGoodsOut);
        atomicInteger.getAndIncrement();
    }

    /**
     * 保存明细至结果集
     *
     * @param data          excel导入数据
     * @param goodsOut 商品信息
     */
    private void initDetail(ImportFirstOrderDetail data, OrderGoodsOut goodsOut) {
        //明细结果集
        if (Objects.isNull(goodsOut.getDistributionPrice())) {
            throw new BusinessException(data.getGoodsCode() + "：此商品无配货价;");
        }
        OrdDirOrderFirstDetailOut detail = new OrdDirOrderFirstDetailOut();
        detail.setGoodsCode(data.getGoodsCode());
        detail.setGoodsName(goodsOut.getGoodsName());
        detail.setStockCode(goodsOut.getStockCode());
        detail.setStockName(goodsOut.getStockName());
        detail.setSortName(goodsOut.getSortName());
        detail.setDistributionPrice(goodsOut.getDistributionUnitPrice());
        detail.setSort(goodsOut.getSort());
        detail.setBarCode(goodsOut.getBarCode());
        detail.setNum(data.getDistributionNum());
        // 铺货金额
        BigDecimal amount = goodsOut.getDistributionUnitPrice().multiply(BigDecimal.valueOf(data.getDistributionNum()));
        detail.setAmount(amount.setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        BigDecimal specificationNum = null;
        if (Objects.nonNull(goodsOut.getDistributionSpecification())) {
            specificationNum = BigDecimal.valueOf(goodsOut.getDistributionSpecification().getQpc());
            detail.setQpcStr(goodsOut.getDistributionSpecification().getQpcStr());
        }
        // 铺货包装数 （铺货数量/配货规格）
        if (Objects.nonNull(data.getDistributionNum()) && Objects.nonNull(specificationNum)) {
            BigDecimal distributionPackageNum = BigDecimal.valueOf(data.getDistributionNum()).divide(specificationNum, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            detail.setDistributionPackageNum(distributionPackageNum);
        }
        outDetails.add(detail);
    }


    /**
     * 查询商品信息
     *
     * @param data excel批量导入入参类
     * @return
     */
    public OrderGoodsOut getGoodsInfo(ImportFirstOrderDetail data) {
        //查询商品入参类
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(data.getGoodsCode());
        String channelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(storeCode, UserUtil.getBizOrgCode());
        orderGoodsIn.setBizOrgCode(channelBizOrgCode);
        //查询商品信息
        OrderGoodsOut orderGoods = orderGoodsServer.getOrderGoods(orderGoodsIn);
        if (Objects.isNull(orderGoods)) {
            throw new BusinessException(data.getGoodsCode() + "：不存在此商品信息;");
        }
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.FIRST_ORDER.getType());
        orderGoodsIn.setStoreCode(storeCode);
        OrderGoodsOut goodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.isNull(goodsOut)) {
            throw new BusinessException(data.getGoodsCode() + "：不可铺货;");
        }
        return goodsOut;
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if(outDetails.size() >= BATCH_COUNT) {
            outDetails.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
        log.info("FirstDirOrderDetailListener解析完成");
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("FirstDirOrderDetailListener解析失败，发生异常:{}", exception.getMessage(), exception);
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
