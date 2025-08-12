package com.edc.erp.common.util;

import com.alibaba.fastjson.JSONArray;
import com.edc.erp.common.model.out.FlashSaleCheckOut;
import com.edc.erp.common.model.out.FlashSaleTimeOut;
import com.edc.erp.common.model.out.FlashSaleWeekOut;
import com.edc.plugins.utils.DateUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-01-08 15:06
 */
public class FlashSaleUtils {

    /**
     * 判断商品是否可以购买
     *
     * @param flashSaleWeekOutList
     * @return
     */
    public static FlashSaleCheckOut getIsCanBuyFlashSaleGoods(List<FlashSaleWeekOut> flashSaleWeekOutList, LocalDateTime targetDateTime) {
        int isCanBuyFlashSale = 1;
        if (CollectionUtils.isNotEmpty(flashSaleWeekOutList)) {
            LocalDateTime nowLocalTimeDate = Objects.isNull(targetDateTime) ? LocalDateTime.now() : targetDateTime;
            Map<Integer, List<FlashSaleTimeOut>> flashSaleWeekOutMap = flashSaleWeekOutList.stream().collect(Collectors.toMap(FlashSaleWeekOut::getWeekNumber,
                    FlashSaleWeekOut::getFlashSaleTimeOutList));
            // 获取当前星期几
            List<FlashSaleTimeOut> flashSaleTimeOutList = flashSaleWeekOutMap.get(nowLocalTimeDate.getDayOfWeek().getValue());
            if (CollectionUtils.isNotEmpty(flashSaleTimeOutList)) {
                flashSaleFlag:
                for (FlashSaleTimeOut flashSaleTimeOut : flashSaleTimeOutList) {
                    String flashSaleBeginDateTimeStr = nowLocalTimeDate.toLocalDate() + " " + flashSaleTimeOut.getBeginTime() + ":00";
                    LocalDateTime beginTime = DateUtils.parseTime(flashSaleBeginDateTimeStr);
                    String flashSaleEndDateTimeStr = nowLocalTimeDate.toLocalDate() + " " + flashSaleTimeOut.getEndTime() + ":59";
                    LocalDateTime endTime = DateUtils.parseTime(flashSaleEndDateTimeStr);
                    if (nowLocalTimeDate.isBefore(beginTime) || nowLocalTimeDate.isAfter(endTime)) {
                        isCanBuyFlashSale = 0;
                        break flashSaleFlag;
                    }
                }
            } else {
                isCanBuyFlashSale = 0;
            }
        }
        FlashSaleCheckOut flashSaleCheckOut = new FlashSaleCheckOut();
        flashSaleCheckOut.setIsCanBuyFlashSale(isCanBuyFlashSale);
        flashSaleCheckOut.setFlashSaleWeekOutList(flashSaleWeekOutList);
        return flashSaleCheckOut;
    }

    public static List<FlashSaleWeekOut> getFlashSaleWeekOut(Object redisFlashSaleObject) {
        if (null != redisFlashSaleObject) {
            JSONArray flashSaleObj = JSONArray.parseArray(redisFlashSaleObject.toString());
            List<FlashSaleWeekOut> flashSaleWeekOutList = JSONArray.parseArray(flashSaleObj.toJSONString(), FlashSaleWeekOut.class);
            return flashSaleWeekOutList;
        }
        return null;
    }
}
