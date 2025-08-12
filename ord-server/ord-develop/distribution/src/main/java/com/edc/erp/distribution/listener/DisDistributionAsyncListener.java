package com.edc.erp.distribution.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.distribution.model.in.ImportOrdDistributionStoreGoodsVO;
import com.edc.erp.distribution.model.out.OrdDisDistributionImportGoodsOut;
import com.edc.erp.distribution.model.out.OrdDisDistributionImportResultOut;
import com.edc.erp.distribution.model.out.OrdDisOrderDistributionDetailOut;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
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
public class DisDistributionAsyncListener extends ImportListener<ImportOrdDistributionStoreGoodsVO> {

    /**
     * 每隔5条存储数据库，实际使用中可以1000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 2000;

    /**
     * 临时存放数据
     */
    List<OrdDisOrderDistributionDetailOut> distributionStoreGoodsList = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(2);

    Map<String, List<String>> storeGoodsMap = new HashMap<>();

    Map<String, Map<String, ImportOrdDistributionStoreGoodsVO>> storeDataMap = new HashMap<>();

    private Map<String, OrdDisDistributionImportResultOut> importResultMap = new HashMap<>();

    private Map<String, BigDecimal> importMap = new HashMap<>();

    public DisDistributionAsyncListener() {
    }

    public Integer getMaxCount() {
        return atomicInteger.get();
    }

    @Override
    public void invoke(ImportOrdDistributionStoreGoodsVO data, AnalysisContext context) {
        String mapKey = "第【" + atomicInteger + "】行：";
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        if (StringUtils.isBlank(data.getStoreCode())) {
            errorJoiner.add("门店代码不能为空");
        }
        if (StringUtils.isBlank(data.getSkuCode())) {
            errorJoiner.add("商品代码不能为空");
        }
        if (Objects.isNull(data.getDistributionQuantity())) {
            errorJoiner.add("分货数量不能为空");
        }
        if (Objects.nonNull(data.getDistributionQuantity())
                && data.getDistributionQuantity().compareTo(BigDecimal.ZERO) < NumberUtil.INTEGER_ZERO) {
            errorJoiner.add("分货数量必须为正整数");
        }
        if (StringUtils.isNotBlank(data.getStoreCode()) && StringUtils.isNotBlank(data.getSkuCode())) {
            String existKey = data.getStoreCode() + SystemConstant.WAIT + data.getSkuCode();
            if (importMap.containsKey(existKey)) {
                errorJoiner.add(data.getStoreCode() + "&" + data.getSkuCode() + "已存在");
            } else {
                importMap.put(existKey, data.getDistributionQuantity());
            }
        }
        if (errorJoiner.length() > 0) {
            totalErrorMap.put(mapKey, errorJoiner);
        } else {
            OrdDisDistributionImportResultOut ordDirDistributionImportResultOut = importResultMap.get(data.getStoreCode());
            if (Objects.isNull(ordDirDistributionImportResultOut)) {
                ordDirDistributionImportResultOut = new OrdDisDistributionImportResultOut();
                ordDirDistributionImportResultOut.setStoreCode(data.getStoreCode());
                ordDirDistributionImportResultOut.setImportGoodsOutList(Lists.newArrayList());
            }
            OrdDisDistributionImportGoodsOut ordDirDistributionImportGoodsOut = new OrdDisDistributionImportGoodsOut();
            ordDirDistributionImportGoodsOut.setGoodsCode(data.getSkuCode());
            ordDirDistributionImportGoodsOut.setDistributionQuantity(data.getDistributionQuantity());
            ordDirDistributionImportResultOut.getImportGoodsOutList().add(ordDirDistributionImportGoodsOut);

            importResultMap.put(data.getStoreCode(), ordDirDistributionImportResultOut);
        }
        atomicInteger.getAndIncrement();
    }

    public Map<String, OrdDisDistributionImportResultOut> getImportResultMap() {
        return this.importResultMap;
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        List<OrdDisDistributionImportGoodsOut> importList = Lists.newArrayList();
        importResultMap.values().forEach(ordDisDistributionImportResultOut -> {
            if (CollectionUtils.isEmpty(ordDisDistributionImportResultOut.getImportGoodsOutList())) {
                return;
            }
            importList.addAll(ordDisDistributionImportResultOut.getImportGoodsOutList());
        });
        log.info("DisDistributionAsyncListener解析完成");
    }


}
