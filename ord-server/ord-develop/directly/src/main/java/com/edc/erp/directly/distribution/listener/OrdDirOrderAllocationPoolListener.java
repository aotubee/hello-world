package com.edc.erp.directly.distribution.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.out.store.StoreInfoOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.distribution.entity.OrdDirOrderAllocationPool;
import com.edc.erp.directly.distribution.model.in.ImportOrdAllocationPoolGoodsVO;
import com.edc.erp.directly.distribution.model.in.ImportOrdOrderStoreGoodsVO;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderDistributionDetailOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderAllocationPoolService;
import com.edc.erp.directly.enumeration.OrderAllocationPoolStatusEnum;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
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
public class OrdDirOrderAllocationPoolListener extends ImportListener<ImportOrdAllocationPoolGoodsVO> {

    /**
     * 每隔5条存储数据库，实际使用中可以1000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 2000;

    /**
     * 临时存放数据
     */
    List<OrdDirOrderDistributionDetailOut> distributionStoreGoodsList = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(2);

    private OrdDirOrderAllocationPoolService ordDirOrderAllocationPoolService;

    private OrderGoodsServer orderGoodsServer;

    Map<String, List<String>> storeGoodsMap = new HashMap<>();

    Map<String, Integer> importMap = new HashMap<>();

    Map<String, Map<String, ImportOrdOrderStoreGoodsVO>> storeDataMap = new HashMap<>();

    List<OrdDirOrderAllocationPool> importList = Lists.newArrayList();

    private Map<String, StoreInfoOut> checkStoreMap;

    public OrdDirOrderAllocationPoolListener(OrdDirOrderAllocationPoolService ordDirOrderAllocationPoolService, Map<String, StoreInfoOut> checkStoreMap) {
        this.ordDirOrderAllocationPoolService = ordDirOrderAllocationPoolService;
        this.checkStoreMap = checkStoreMap;
    }

    @Override
    public void invoke(ImportOrdAllocationPoolGoodsVO data, AnalysisContext context) {
        String mapKey = "第【" + atomicInteger + "】行：";
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        if (StringUtils.isBlank(data.getStoreCode())) {
            errorJoiner.add("门店代码不能为空");
        }
        if (StringUtils.isBlank(data.getGoodsCode())) {
            errorJoiner.add("商品代码不能为空");
        }
        if (Objects.isNull(data.getSupplementQuantity())) {
            errorJoiner.add("补单量不能为空");
        }
        boolean existFlag = true;
        if (null != checkStoreMap) {
            existFlag = checkStoreMap.containsKey(data.getStoreCode());
            if (!existFlag) {
                errorJoiner.add("门店不在当前账号数据权限范围内");
            }
        }
        OrdDirOrderAllocationPool existOne = null;
        if (existFlag) {
            if (StringUtils.isNotBlank(data.getStoreCode()) && StringUtils.isNotBlank(data.getGoodsCode())) {
                existOne = new OrdDirOrderAllocationPool();
                existOne.setStoreCode(data.getStoreCode());
                existOne.setGoodsCode(data.getGoodsCode());
                existOne.setIsDelete(ModelConst.DELETE.NO);
                existOne = ordDirOrderAllocationPoolService.selectOne(existOne);
                if (Objects.isNull(existOne)) {
                    errorJoiner.add("调配台不存在此门店商品记录");
                } else {
                    if (Objects.nonNull(data.getSupplementQuantity()) && Objects.nonNull(existOne)) {
                        if (data.getSupplementQuantity() < NumberUtil.INTEGER_ZERO) {
                            errorJoiner.add("补单数量必须为0或者规格整数倍");
                        }
                        if (data.getSupplementQuantity() > NumberUtil.INTEGER_ZERO) {
                            int remainder = data.getSupplementQuantity() % existOne.getDistributionSpecNum();
                            if (remainder != NumberUtil.INTEGER_ZERO) {
                                errorJoiner.add("补单数量必须为0或者规格整数倍");
                            }
                        }
                        LocalDateTime truncationDateTime = existOne.getTruncationDateTime();
                        if (Objects.isNull(truncationDateTime)) {
                            errorJoiner.add("当前时间距截单时间未计算，无法比对是否允许更新补单量");
                        } else {
                            Duration duration = Duration.between(LocalDateTime.now(), truncationDateTime);
                            if (duration.toMinutes() <= 60) {
                                errorJoiner.add("当前时间距截单时间不允许更新补单量");
                            }
                        }
                    }
                }
                String existKey = data.getStoreCode() + SystemConstant.WAIT + data.getGoodsCode();
                if (importMap.containsKey(existKey)) {
                    errorJoiner.add(data.getStoreCode() + "&" + data.getGoodsCode() + "已存在");
                } else {
                    importMap.put(existKey, data.getSupplementQuantity());
                }
            }
            if (StringUtils.isNotBlank(data.getRemark()) && data.getRemark().length() > 30) {
                errorJoiner.add("备注已超出30个字符");
            }
        }
        if (errorJoiner.length() > 0) {
            totalErrorMap.put(mapKey, errorJoiner);
        } else {
            existOne.setSupplementQuantity(new BigDecimal(data.getSupplementQuantity()));
            existOne.setSupplementPackageQuantity(existOne.getSupplementQuantity().divide(new BigDecimal(existOne.getDistributionSpecNum()), NumberUtil.INTEGER_ZERO, RoundingMode.DOWN));
            existOne.setStatus(OrderAllocationPoolStatusEnum.DONE.getKey());
            existOne.setRemark(data.getRemark());
            importList.add(existOne);
        }
        atomicInteger.getAndIncrement();
    }

    public List<OrdDirOrderAllocationPool> getImportGoodsOutList() {
        return this.importList;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (importList.size() >= BATCH_COUNT) {
            importList.clear();
            throw new BusinessException("超最大导入限制" + BATCH_COUNT + "条");
        }
        log.info("OrdDirOrderAllocationPoolListener解析完成");
    }
}
