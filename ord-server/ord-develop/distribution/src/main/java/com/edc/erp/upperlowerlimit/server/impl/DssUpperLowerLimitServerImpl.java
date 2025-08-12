package com.edc.erp.upperlowerlimit.server.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.model.out.DssOrderInfoOut;
import com.edc.erp.common.service.DssOrderInfoService;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.model.in.CreateOrderSkuIn;
import com.edc.erp.upperlowerlimit.server.DssUpperLowerLimitServer;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;


/**
 * @ClassName DssUpperLowerLimitServerImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/6/18 17:04
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class DssUpperLowerLimitServerImpl implements DssUpperLowerLimitServer {

    private final DisOrderHandle orderHandle;

    /**
     * 线程池数
     */
    private static final int THREAD_COUNT = 10;

    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 4;

    /**
     * 任务执行数量
     */
    private static final int EXECUTED_QUANTITY = 100;

    private final DssOrderInfoService dssOrderInfoService;

    /**
     * @Description: 点三三跑货job多线程
     * @Author: ZhangYao
     * @Date: 2024/6/19 9:21
     * @param storeDssOrderGoodsMap:
     * @param bizOrgCode:
     * @return: void
     **/
    @Override
    public void dssReplenishmentOrderJob(Map<String, List<DssOrderInfoOut>> storeDssOrderGoodsMap, String bizOrgCode) {
        if (null == storeDssOrderGoodsMap && storeDssOrderGoodsMap.size() == 0) {
            return;
        }
        if (storeDssOrderGoodsMap.size() < EXECUTED_QUANTITY) {
            this.executedJob(storeDssOrderGoodsMap, bizOrgCode);
        } else {
            // 计算每个线程处理多少条数据  1000
            int threadDataSize = storeDssOrderGoodsMap.size() / CORE_POOL_SIZE;
            //设置线程池
            ExecutorService threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, THREAD_COUNT, 5L,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
            Map<String, List<DssOrderInfoOut>> needHandleStoreCodeMap;
            for (int i = 0; i < CORE_POOL_SIZE; i++) {
                if (i == CORE_POOL_SIZE - 1) {
                    needHandleStoreCodeMap = dssOrderInfoService.subMap(storeDssOrderGoodsMap, i * threadDataSize, storeDssOrderGoodsMap.size());
                } else {
                    needHandleStoreCodeMap = dssOrderInfoService.subMap(storeDssOrderGoodsMap, i * threadDataSize, threadDataSize * (i + 1));
                }
                // 开启线程
                Map<String, List<DssOrderInfoOut>> finalNeedHandleStoreCodeMap = needHandleStoreCodeMap;
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        log.info("当前线程待执处理的加盟点三三智能跑货有{}条数据........................", finalNeedHandleStoreCodeMap.size());
                        executedJob(finalNeedHandleStoreCodeMap, bizOrgCode);
                    }
                });
            }
            //关闭线程池
            MoreExecutors.shutdownAndAwaitTermination(threadPool, 6, TimeUnit.HOURS);
        }
    }

    /**
     * 执行跑货业务
     *
     * @param bizOrgCode
     */
    private void executedJob(Map<String, List<DssOrderInfoOut>> storeDssOrderGoodsMap, String bizOrgCode) {
//        StringJoiner storeCodesJoiner = new StringJoiner(SystemConstant.COMMA);
        storeDssOrderGoodsMap.entrySet().forEach(entry -> {
            List<CreateOrderSkuIn> createOrderSkuInList = Lists.newArrayList();
            entry.getValue().forEach(dssOrderInfoOut -> {
                CreateOrderSkuIn createOrderSkuIn = new CreateOrderSkuIn();
                createOrderSkuIn.setPackageQuantity(dssOrderInfoOut.getNsl());
                createOrderSkuIn.setGoodsCode(dssOrderInfoOut.getSSpbh());
                createOrderSkuInList.add(createOrderSkuIn);
            });
            orderHandle.createUpAndDownOrder(entry.getKey(), SystemConstant.SYSTEM_USER, bizOrgCode, createOrderSkuInList, BusinessTypeColumnEnum.INTELLIGENT_UP_LOW_DOWN.getType());
        });
    }

//    public static Map<String, List<DssOrderInfoOut>> subMap(Map<String, List<DssOrderInfoOut>> map, int start, int end) {
//        Map<String, List<DssOrderInfoOut>> result = new LinkedHashMap<>();
//        Iterator<Map.Entry<String, List<DssOrderInfoOut>>> iterator = map.entrySet().iterator();
//        for (int i = 0; iterator.hasNext(); i++) {
//            Map.Entry<String, List<DssOrderInfoOut>> entry = iterator.next();
//            if (i >= start && i < end) {
//                result.put(entry.getKey(), entry.getValue());
//            }
//        }
//        return result;
//    }

}
