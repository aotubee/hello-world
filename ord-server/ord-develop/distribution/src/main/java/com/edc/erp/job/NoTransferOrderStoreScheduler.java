package com.edc.erp.job;


import com.alibaba.fastjson.JSON;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.NoticeNoTransferOrderStoreTemplateEnum;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.model.in.warning.RobotMarkDownMessageIn;
import com.edc.erp.common.service.DingTalkService;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.common.model.in.warning.QueryPaidTransferOrderIn;
import com.edc.erp.handle.DisOrderConfigHandle;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.edc.plugins.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 未定中转单门店定时器
 */
@Component
@Slf4j
public class NoTransferOrderStoreScheduler {

    @Autowired
    RedisService redisService;

    @Autowired
    TaskLockUtils taskLockUtils;

    @Autowired
    private StoreCenterService storeCenterService;

    /**
     * 关闭查询未定中转单门店定时器的值
     */
    private final static String CLOSE_NO_TRANSFER_ORDER_KEY = "OPEN";

    @Autowired
    private DisOrderHandle orderHandle;

    @Autowired
    private DisOrderConfigHandle disOrderConfigHandle;

    @Autowired
    private DingTalkService dingTalkService;


    @Value("${noTransferOrderStoreUrl}")
    private String noTransferOrderStoreUrl;

    /**
     * 未定中转单门店定时器
     */
//    @Scheduled(cron = "0 30 8 * * ?")
//    @Scheduled(cron = "0 0/10 * * * ?")
    public void sendMessageForNoTransferOrder() {
        String closeNoTransferOrderSwitch = redisService.get(SystemConstant.CLOSE_NO_TRANSFER_ORDER_SWITCH_KEY);
        if (CLOSE_NO_TRANSFER_ORDER_KEY.equals(closeNoTransferOrderSwitch)) {
            log.info("查询未定中转单门店定时器开关未打开......");
            return;
        }
        log.info("开始执行查询特许加盟未定中转单门店任务");
        if (taskLockUtils.lock(SystemConstant.FRANCHISE_NO_TRANSFER_ORDER_SWITCH, 60 * 3 * 1000)) {
            try {
                // 查询 APP用户
                List<String> storeCodeList = storeCenterService.findEnableAppStoreCodeListByPropertyAndOrg(StoreConstant.StoreProperty.FRANCHISE.getMytValue(), OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode());
                if (CollectionUtils.isEmpty(storeCodeList)) {
                    log.info("特许加盟未定中转单门店定时器未查到源门店");
                    return;
                }
                List<Long> orderTypeIdList = disOrderConfigHandle.findOrderTypeIdByParameter("distributionType", DistributionWaysEnum.TRANSFER.getType(), OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode());
                if (CollectionUtils.isEmpty(orderTypeIdList)) {
                    log.info("特许加盟未查到中转门店订单类型配置");
                    return;
                }
                QueryPaidTransferOrderIn queryPaidTransferOrderIn = new QueryPaidTransferOrderIn();
                queryPaidTransferOrderIn.setOrderTypeConfigIdList(orderTypeIdList);
                queryPaidTransferOrderIn.setBizOrgCode(OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode());
                queryPaidTransferOrderIn.setTruncationDateTimeBegin(DateUtils.format(LocalDate.now()) + " 00:00:00");
                queryPaidTransferOrderIn.setTruncationDateTimeEnd(DateUtils.format(LocalDate.now()) + " 23:59:59");
                // 获取待付款已付款中转门店列表
                List<String> paidTransferOrderStoreCodeList = orderHandle.findPaidTransferOrderStoreCodeList(queryPaidTransferOrderIn);
                List<String> noTransferStoreCodeList;
                if (CollectionUtils.isEmpty(paidTransferOrderStoreCodeList)) {
                    noTransferStoreCodeList = Lists.newArrayList(storeCodeList);
                } else {
                    Map<String, String> paidTransferOrderStoreCodeMap = paidTransferOrderStoreCodeList.stream().collect(Collectors.toMap(Function.identity(), Function.identity(),
                            (key1, key2) -> key2));
                    noTransferStoreCodeList = storeCodeList.stream().filter(s -> Objects.isNull(paidTransferOrderStoreCodeMap.get(s))).collect(Collectors.toList());
                }
                String content;
                if (CollectionUtils.isNotEmpty(noTransferStoreCodeList)) {
                    String storeCodes = noTransferStoreCodeList.stream().map(String::valueOf).collect(Collectors.joining(","));
                    content = MessageFormat.format(NoticeNoTransferOrderStoreTemplateEnum.NO_TRANSFER_ORDER_ONE.getTemplateMessage(), "特许加盟", noTransferStoreCodeList.size(), storeCodes);
                } else {
                    content = MessageFormat.format(NoticeNoTransferOrderStoreTemplateEnum.NO_TRANSFER_ORDER_TWO.getTemplateMessage(), "特许加盟", "0");
                }
                RobotMarkDownMessageIn robotMarkDownMessageIn = new RobotMarkDownMessageIn();
                robotMarkDownMessageIn.setTitle(NoticeNoTransferOrderStoreTemplateEnum.NO_TRANSFER_ORDER_ONE.getKey());
                robotMarkDownMessageIn.setText(content);
                robotMarkDownMessageIn.setIsAtAll(true);
                try {
                    if (StringUtils.isNotEmpty(noTransferOrderStoreUrl)) {
                        for (String url : noTransferOrderStoreUrl.split(",")) {
                            robotMarkDownMessageIn.setUrl(url);
                            Response response = dingTalkService.sendDingTalkRobotMarkDownMessage(robotMarkDownMessageIn);
                            if (null != response && response.isSuccess()) {
                                log.info("发送特许加盟未下中转订单门店信息" + JSON.toJSONString(response));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("发送特许加盟未下中转订单门店信息异常", e);
                }
            } catch (Exception e) {
                log.error("执行查询特许加盟未定中转单门店任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.FRANCHISE_NO_TRANSFER_ORDER_SWITCH);
            }
        } else {
            log.info("查询特许加盟未定中转单门店定时器定时器锁还未释放：{}", SystemConstant.FRANCHISE_NO_TRANSFER_ORDER_SWITCH);
        }
        log.info("查询特许加盟未定中转单门店任务执行结束");
    }
}
