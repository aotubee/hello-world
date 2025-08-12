package com.edc.erp.common.stocksupplyrate;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.mapper.GoodsStockSupplyRateMapper;
import com.edc.erp.common.model.out.stock.GoodsStockSupplyRateOut;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.edc.plugins.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsStockSupplyRateJob
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/4/29 17:33
 **/
@Component
@Slf4j
@RequiredArgsConstructor
public class GoodsStockSupplyRateScheduler {

    private final RedisService redisService;

    private final TaskLockUtils taskLockUtils;

    private final GoodsStockSupplyRateMapper goodsStockSupplyRateMapper;

    private final static String CLOSE_CALCULATE_GOODS_STOCK_SUPPLY_RATE_KEY = "OPEN";

    @Scheduled(cron = "0 0/10 * * * ?")
    public void calculateGoodsStockSupplyRateJob() {
        String closeCalculateGoodsStockSupplyRateSwitch = redisService.get(SystemConstant.CLOSE_CALCULATE_GOODS_STOCK_SUPPLY_RATE_SWITCH_KEY);
        if (CLOSE_CALCULATE_GOODS_STOCK_SUPPLY_RATE_KEY.equals(closeCalculateGoodsStockSupplyRateSwitch)) {
            log.info("计算库存满足率开关未打开......");
            return;
        }
        log.info("开始执行计算库存满足率任务");
        if (taskLockUtils.lock(SystemConstant.CALCULATE_GOODS_STOCK_SUPPLY_RATE_SWITCH, 60 * 5 * 1000)) {
            try {
                LocalDateTime calculateTime = LocalDateTime.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), LocalDate.now().getDayOfMonth(), LocalDateTime.now().getHour(), LocalDateTime.now().getMinute(), 0);
                String truncationDateTime = DateUtils.format(calculateTime);
//                truncationDateTime = "2024-05-13 11:40:00";
                String stockCodeStr = redisService.get(SystemConstant.SUPPLY_RATE_STOCK_CODE_KEY);
                if (StringUtils.isBlank(stockCodeStr)) {
                    return;
                }
                List<String> stockList = Arrays.asList(stockCodeStr.split(SystemConstant.COMMA));
                List<GoodsStockSupplyRateOut> goodsStockSupplyRateList = goodsStockSupplyRateMapper.findGoodsStockSupplyRate(truncationDateTime, stockList);
                if (CollectionUtils.isEmpty(goodsStockSupplyRateList)) {
                    return;
                }
                String key = SystemConstant.STOCK_SUPPLY_RATE_TRUNCATION_DATE_TIME_KEY + SystemConstant.COLON + truncationDateTime.replace(SystemConstant.COLON, SystemConstant.SHORT_LINE);
//                log.info("缓存key------{}------goodsStockSupplyRateList长度{}", key, goodsStockSupplyRateList.size());
                Map<String, Object> goodsStockSupplyRateOutMap = goodsStockSupplyRateList.stream()
                        .collect(Collectors.toMap(rate -> rate.getGoodsCode() + SystemConstant.SHORT_LINE + rate.getStockCode(), entry -> JSONObject.toJSONString(entry), (v1, v2) -> v1));
//                goodsStockSupplyRateOutMap.forEach((s, object) -> {
//                    log.info("key----{}-----------value----------->{}", s, object.toString());
//                });
                goodsStockSupplyRateOutMap.entrySet().forEach(entry -> {
                    redisService.hSet(key, entry.getKey(), entry.getValue(), 2l, TimeUnit.DAYS);
                    log.info("{}取出库存缓存key-------------{}--------------------value================{}", key, entry.getKey(), entry.getValue());
                });
            } catch (Exception e) {
                log.error("执行计算库存满足率任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.CALCULATE_GOODS_STOCK_SUPPLY_RATE_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", SystemConstant.CALCULATE_GOODS_STOCK_SUPPLY_RATE_SWITCH);
        }
        log.info("计算库存满足率任务执行结束");
    }
}
