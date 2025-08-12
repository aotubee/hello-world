package com.edc.erp.common.util;

import com.edc.erp.common.enumeration.StoreDeliveryCycleEnum;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.utils.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 门店预计配货日期工具类
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020年04月28日 11:05
 */
public class StoreDeliveryDateUtil {

    /**
     * 获取预计到货日期
     *
     * @param storeDeliveryType 配货类型
     * @param cutoffTimeStr     截单时间（格式 - 11:20）
     * @return
     */
    public static String getStoreDeliveryDate(String storeDeliveryType, String cutoffTimeStr) {
        String cutoffDateTimeStr = LocalDate.now() + " " + cutoffTimeStr + ":00";
        LocalDateTime cutoffTime = DateUtils.parseTime(cutoffDateTimeStr);
        String storeDeliveryDate = null;
        Integer nowDay = LocalDate.now().getDayOfMonth();
        // 获取最后一天号数
        Integer lastDay = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
        // 单日配
        if (StoreDeliveryCycleEnum.ODD_DAYS.getCode().equals(storeDeliveryType)
                || StoreDeliveryCycleEnum.DIRECTLY_ODD_DAYS.getCode().equals(storeDeliveryType)) {
            boolean isLastDayPlural = false;
            // 判断当前月最后一天是否奇数还是偶数
            if (lastDay % NumberUtil.INTEGER_TWO == 0) {
                isLastDayPlural = true;
            }
            // 当前日期是偶数，固定+1
            if (nowDay % NumberUtil.INTEGER_TWO == 0) {
                storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(1));
            } else {
                // 当前日期是奇数,当前时间 < 截单时间
                if (LocalDateTime.now().isBefore(cutoffTime)) {
                    storeDeliveryDate = DateUtils.format(LocalDate.now());
                } else {
                    // 当前日期是奇数， 比截单时间晚，根据当前月月底日期（单双日）计算增量日期
                    Integer plusDays = isLastDayPlural ? 2 : nowDay < lastDay ? 2 : 1;
                    storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
                }
            }
        }
        // 双日配
        if (StoreDeliveryCycleEnum.ALTERNATE_DAYS.getCode().equals(storeDeliveryType)
                || StoreDeliveryCycleEnum.DIRECTLY_ALTERNATE_DAYS.getCode().equals(storeDeliveryType)) {
            boolean isPlural = false;
            // 判断当前月最后一天是否奇数还是偶数
            if (lastDay % NumberUtil.INTEGER_TWO == 0) {
                isPlural = true;
            }
            // 当前日期是偶数
            if (nowDay % NumberUtil.INTEGER_TWO == 0) {
                // 当前日期是奇数,当前时间 < 截单时间
                if (LocalDateTime.now().isBefore(cutoffTime)) {
                    storeDeliveryDate = DateUtils.format(LocalDate.now());
                } else {
                    // 当前日期是偶数， 当前小时数 > 截单时间小时数，根据当前月月底日期（单双日）计算增量日期
                    Integer plusDays = isPlural ? 2 : lastDay - nowDay == 1 ? 3 : 2;
                    storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
                }
                // 判断大月份
                if (!isPlural) {
                    // 下个截单周期年月日
                    LocalDate nextCutDate = DateUtils.parseDate(storeDeliveryDate);
                    // 计算当月最后一天
                    LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
                    // 跨月，下个截单周期覆盖为 当前月最后一天
                    if (nextCutDate.isAfter(lastDayOfMonth)) {
                        storeDeliveryDate = DateUtils.format(lastDayOfMonth);
                    }
                }
            } else {
//                // 双日配,月底最后一天也能配货
                if (nowDay.equals(lastDay) && LocalDateTime.now().isBefore(cutoffTime)) {
                    storeDeliveryDate = DateUtils.format(LocalDate.now());
                } else {
                    Integer plusDays = lastDay - nowDay == 0 ? 2 : 1;
                    storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
                }

                // ------ 1
//                Integer plusDays = lastDay - nowDay == 0 ? 2 : 1;
//                storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
//                if (!isPlural) {
//                    // 计算当月最后一天
//                    LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
//                    // 跨月，下个截单周期覆盖为 当前月最后一天
//                    if (LocalDate.now().equals(lastDayOfMonth)) {
//                        if (LocalDateTime.now().isBefore(cutoffTime)) {
//                            storeDeliveryDate = DateUtils.format(LocalDate.now());
//                        }
//                    }
//                }
                // ------ 1
            }

//            // 当月最后一天是奇数
//            if (!isPlural) {
//                // 下个截单周期比月底最后一天晚,下个截单周期被覆盖为：当前月末最后一天
//                // nextCutDate = 11.2  lastDayOfMonth = 10.31
//                // 跨月，下个截单周期覆盖为 当前月最后一天
//                if (nextCutDate.isAfter(lastDayOfMonth)) {
//                    storeDeliveryDate = DateUtils.format(lastDayOfMonth);
//                }
//                // 下单时间是31号，过点了，
//
//
//                // 如果下个截单周期就是当月最后一天，需要判断截单时间点的前与后
//                if (nextCutDate.equals(lastDayOfMonth)) {
//                    if (LocalDateTime.now().isBefore(cutoffTime)) {
//                        storeDeliveryDate = DateUtils.format(LocalDate.now());
//                    } else {
//                        storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(2));
//                    }
//                }
//            }
        }
        // 日配
        if (StoreDeliveryCycleEnum.DAY_WITH.getCode().equals(storeDeliveryType)
                || StoreDeliveryCycleEnum.DIRECTLY_DAY_WITH.getCode().equals(storeDeliveryType)) {
            if (LocalDateTime.now().isBefore(cutoffTime)) {
                storeDeliveryDate = DateUtils.format(LocalDate.now());
            } else {
                storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(1));
            }
        }
        // 每周一三五
        if (StoreDeliveryCycleEnum.MON_WED_FRIDAY.getCode().equals(storeDeliveryType)
                || StoreDeliveryCycleEnum.DIRECTLY_MON_WED_FRIDAY.getCode().equals(storeDeliveryType)) {
            // 获取当前星期几
            Integer dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
            if (NumberUtil.INTEGER_ONE.equals(dayOfWeek) || NumberUtil.INTEGER_THREE.equals(dayOfWeek) || NumberUtil.INTEGER_FIVE.equals(dayOfWeek)) {
                if (LocalDateTime.now().isBefore(cutoffTime)) {
                    storeDeliveryDate = DateUtils.format(LocalDate.now());
                } else {
                    Integer plusDays = NumberUtil.INTEGER_FIVE.equals(dayOfWeek) ? NumberUtil.INTEGER_THREE : NumberUtil.INTEGER_TWO;
                    storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
                }
            } else {
                Integer plusDays = NumberUtil.INTEGER_SIX.equals(dayOfWeek) ? NumberUtil.INTEGER_TWO : NumberUtil.INTEGER_ONE;
                storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
            }
        }
        // 每周四
//        if (StoreDeliveryCycleEnum.THURSDAY.getCode().equals(storeDeliveryType)) {
//            // 获取当前星期几
//            Integer dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
//            if (dayOfWeek == 4) {
//                if (LocalDateTime.now().isBefore(cutoffTime)) {
//                    storeDeliveryDate = DateUtils.format(LocalDate.now());
//                } else {
//                    LocalDate nextThursdayDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
//                    storeDeliveryDate = DateUtils.format(nextThursdayDate);
//                }
//            } else {
//                LocalDate nextThursdayDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
//                storeDeliveryDate = DateUtils.format(nextThursdayDate);
//            }
//        }
        // 每周三五
        if (StoreDeliveryCycleEnum.WED_FRIDAY.getCode().equals(storeDeliveryType)) {
            // 获取当前星期几
            Integer dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
            if (NumberUtil.INTEGER_THREE.equals(dayOfWeek) || NumberUtil.INTEGER_FIVE.equals(dayOfWeek)) {
                if (LocalDateTime.now().isBefore(cutoffTime)) {
                    storeDeliveryDate = DateUtils.format(LocalDate.now());
                } else {
                    Integer plusDays = NumberUtil.INTEGER_FIVE.equals(dayOfWeek) ? NumberUtil.INTEGER_FIVE : NumberUtil.INTEGER_TWO;
                    storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
                }
            } else {
                Integer plusDays = NumberUtil.INTEGER_ZERO;
                if (dayOfWeek < NumberUtil.INTEGER_THREE) {
                    plusDays = NumberUtil.INTEGER_THREE - dayOfWeek;
                }
                if (NumberUtil.INTEGER_FOUR.equals(dayOfWeek)) {
                    plusDays = NumberUtil.INTEGER_FIVE - dayOfWeek;
                }
                if (dayOfWeek > NumberUtil.INTEGER_FIVE) {
                    plusDays = NumberUtil.INTEGER_SIX.equals(dayOfWeek) ? NumberUtil.INTEGER_FOUR : NumberUtil.INTEGER_THREE;
                }
                storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
            }
        }
        if (StringUtils.isBlank(storeDeliveryDate)) {
            throw new BusinessException("未指定的门店配货类型");
        }
        return storeDeliveryDate + " " + cutoffTimeStr;
    }

//    public static String getStoreDeliveryDate(String storeDeliveryType, String cutoffTimeStr) {
//        LocalDate nowDate = LocalDate.of(2024,10,31);
//        LocalDateTime nowTime = LocalDateTime.of(2024,10,31,16,10,10);
//        String cutoffDateTimeStr = nowDate + " " + cutoffTimeStr + ":00";
//        LocalDateTime cutoffTime = DateUtils.parseTime(cutoffDateTimeStr);
//        String storeDeliveryDate = null;
//        Integer nowDay = nowDate.getDayOfMonth();
//        // 获取最后一天号数
//        Integer lastDay = nowDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
//        // 单日配
//        if (StoreDeliveryCycleEnum.ODD_DAYS.getCode().equals(storeDeliveryType)
//                || StoreDeliveryCycleEnum.DIRECTLY_ODD_DAYS.getCode().equals(storeDeliveryType)) {
//            boolean isLastDayPlural = false;
//            // 判断当前月最后一天是否奇数还是偶数
//            if (lastDay % NumberUtil.INTEGER_TWO == 0) {
//                isLastDayPlural = true;
//            }
//            // 当前日期是偶数，固定+1
//            if (nowDay % NumberUtil.INTEGER_TWO == 0) {
//                storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(1));
//            } else {
//                // 当前日期是奇数,当前时间 < 截单时间
//                if (LocalDateTime.now().isBefore(cutoffTime)) {
//                    storeDeliveryDate = DateUtils.format(LocalDate.now());
//                } else {
//                    // 当前日期是奇数， 比截单时间晚，根据当前月月底日期（单双日）计算增量日期
//                    Integer plusDays = isLastDayPlural ? 2 : nowDay < lastDay ? 2 : 1;
//                    storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
//                }
//            }
//        }
//        // 双日配
//        if (StoreDeliveryCycleEnum.ALTERNATE_DAYS.getCode().equals(storeDeliveryType)
//                || StoreDeliveryCycleEnum.DIRECTLY_ALTERNATE_DAYS.getCode().equals(storeDeliveryType)) {
//            boolean isPlural = false;
//            // 判断当前月最后一天是否奇数还是偶数
//            if (lastDay % NumberUtil.INTEGER_TWO == 0) {
//                isPlural = true;
//            }
//            // 当前日期是偶数
//            if (nowDay % NumberUtil.INTEGER_TWO == 0) {
//                // 当前日期是奇数,当前时间 < 截单时间
//                if (nowTime.isBefore(cutoffTime)) {
//                    storeDeliveryDate = DateUtils.format(nowDate);
//                } else {
//                    // 当前日期是偶数， 当前小时数 > 截单时间小时数，根据当前月月底日期（单双日）计算增量日期
//                    Integer plusDays = isPlural ? 2 : lastDay - nowDay == 1 ? 3 : 2;
//                    storeDeliveryDate = DateUtils.format(nowDate.plusDays(plusDays));
//                }
//            } else {
//                // 双日配,月底最后一天也能配货
//                if (nowDay.equals(lastDay) && nowTime.isBefore(cutoffTime)) {
//                    storeDeliveryDate = DateUtils.format(nowDate);
//                } else {
//                    Integer plusDays = lastDay - nowDay == 0 ? 2 : 1;
//                    storeDeliveryDate = DateUtils.format(nowDate.plusDays(plusDays));
//                }
//            }
//        }
//        // 日配
//        if (StoreDeliveryCycleEnum.DAY_WITH.getCode().equals(storeDeliveryType)
//                || StoreDeliveryCycleEnum.DIRECTLY_DAY_WITH.getCode().equals(storeDeliveryType)) {
//            if (LocalDateTime.now().isBefore(cutoffTime)) {
//                storeDeliveryDate = DateUtils.format(LocalDate.now());
//            } else {
//                storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(1));
//            }
//        }
//        // 每周一三五
//        if (StoreDeliveryCycleEnum.MON_WED_FRIDAY.getCode().equals(storeDeliveryType)
//                || StoreDeliveryCycleEnum.DIRECTLY_MON_WED_FRIDAY.getCode().equals(storeDeliveryType)) {
//            // 获取当前星期几
//            Integer dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
//            if (NumberUtil.INTEGER_ONE.equals(dayOfWeek) || NumberUtil.INTEGER_THREE.equals(dayOfWeek) || NumberUtil.INTEGER_FIVE.equals(dayOfWeek)) {
//                if (LocalDateTime.now().isBefore(cutoffTime)) {
//                    storeDeliveryDate = DateUtils.format(LocalDate.now());
//                } else {
//                    Integer plusDays = NumberUtil.INTEGER_FIVE.equals(dayOfWeek) ? NumberUtil.INTEGER_THREE: NumberUtil.INTEGER_TWO;
//                    storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
//                }
//            } else {
//                Integer plusDays = NumberUtil.INTEGER_SIX.equals(dayOfWeek) ? NumberUtil.INTEGER_TWO: NumberUtil.INTEGER_ONE;
//                storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
//            }
//        }
//        // 每周四
////        if (StoreDeliveryCycleEnum.THURSDAY.getCode().equals(storeDeliveryType)) {
////            // 获取当前星期几
////            Integer dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
////            if (dayOfWeek == 4) {
////                if (LocalDateTime.now().isBefore(cutoffTime)) {
////                    storeDeliveryDate = DateUtils.format(LocalDate.now());
////                } else {
////                    LocalDate nextThursdayDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
////                    storeDeliveryDate = DateUtils.format(nextThursdayDate);
////                }
////            } else {
////                LocalDate nextThursdayDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
////                storeDeliveryDate = DateUtils.format(nextThursdayDate);
////            }
////        }
//        // 每周三五
//        if (StoreDeliveryCycleEnum.WED_FRIDAY.getCode().equals(storeDeliveryType)) {
//            // 获取当前星期几
//            Integer dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
//            if ( NumberUtil.INTEGER_THREE.equals(dayOfWeek) || NumberUtil.INTEGER_FIVE.equals(dayOfWeek)) {
//                if (LocalDateTime.now().isBefore(cutoffTime)) {
//                    storeDeliveryDate = DateUtils.format(LocalDate.now());
//                } else {
//                    Integer plusDays = NumberUtil.INTEGER_FIVE.equals(dayOfWeek) ? NumberUtil.INTEGER_FIVE : NumberUtil.INTEGER_TWO;
//                    storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
//                }
//            } else {
//                Integer plusDays = NumberUtil.INTEGER_ZERO;
//                if (dayOfWeek < NumberUtil.INTEGER_THREE) {
//                    plusDays = NumberUtil.INTEGER_THREE - dayOfWeek;
//                }
//                if (NumberUtil.INTEGER_FOUR.equals(dayOfWeek)) {
//                    plusDays = NumberUtil.INTEGER_FIVE - dayOfWeek;
//                }
//                if (dayOfWeek > NumberUtil.INTEGER_FIVE) {
//                    plusDays = NumberUtil.INTEGER_SIX.equals(dayOfWeek) ? NumberUtil.INTEGER_FOUR : NumberUtil.INTEGER_THREE;
//                }
//                storeDeliveryDate = DateUtils.format(LocalDate.now().plusDays(plusDays));
//            }
//        }
//        if (StringUtils.isBlank(storeDeliveryDate)) {
//            throw new BusinessException("未指定的门店配货类型");
//        }
//        return storeDeliveryDate + " " + cutoffTimeStr;
//    }


    /**
     * 判断当前时间是否匹配门店配送周期
     *
     * @param storeDeliveryType 配货类型
     * @return
     */
    public static boolean checkStoreDelivery(String storeDeliveryType) {
        boolean storeDeliveryFlag = false;
        Integer nowDay = LocalDate.now().getDayOfMonth();
        // 单日配
        if (StoreDeliveryCycleEnum.ODD_DAYS.getCode().equals(storeDeliveryType)
                || StoreDeliveryCycleEnum.DIRECTLY_ODD_DAYS.getCode().equals(storeDeliveryType)) {
            // 当前日期是偶数，固定+1
            if (nowDay % NumberUtil.INTEGER_TWO == 0) {
                storeDeliveryFlag = false;
            } else {
                storeDeliveryFlag = true;
            }
        }
        // 双日配
        if (StoreDeliveryCycleEnum.ALTERNATE_DAYS.getCode().equals(storeDeliveryType)
                || StoreDeliveryCycleEnum.DIRECTLY_ALTERNATE_DAYS.getCode().equals(storeDeliveryType)) {
            // 当前日期是偶数
            if (nowDay % NumberUtil.INTEGER_TWO == 0) {
                storeDeliveryFlag = true;
            } else {
                // 获取最后一天号数
                Integer lastDay = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
                if(nowDay.equals(lastDay)){
                    storeDeliveryFlag = true;
                }else {
                    storeDeliveryFlag = false;
                }
            }
        }
        // 日配
        if (StoreDeliveryCycleEnum.DAY_WITH.getCode().equals(storeDeliveryType)
                || StoreDeliveryCycleEnum.DIRECTLY_DAY_WITH.getCode().equals(storeDeliveryType)) {
            storeDeliveryFlag = true;
        }
        // 每周一三五
        if (StoreDeliveryCycleEnum.MON_WED_FRIDAY.getCode().equals(storeDeliveryType)
                || StoreDeliveryCycleEnum.DIRECTLY_MON_WED_FRIDAY.getCode().equals(storeDeliveryType)) {
            // 获取当前星期几
            Integer dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
            if (dayOfWeek.equals(NumberUtil.INTEGER_ONE) || dayOfWeek.equals(NumberUtil.INTEGER_THREE) || dayOfWeek.equals(NumberUtil.INTEGER_FIVE)) {
                storeDeliveryFlag = true;
            } else {
                storeDeliveryFlag = false;
            }
        }
        // 每周四
//        if (StoreDeliveryCycleEnum.THURSDAY.getCode().equals(storeDeliveryType)) {
//            // 获取当前星期几
//            Integer dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
//            if (dayOfWeek == 4) {
//                storeDeliveryFlag = true;
//            } else {
//                storeDeliveryFlag = false;
//            }
//        }
        // 每周三五
        if (StoreDeliveryCycleEnum.WED_FRIDAY.getCode().equals(storeDeliveryType)) {
            // 获取当前星期几
            Integer dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
            if (NumberUtil.INTEGER_THREE.equals(dayOfWeek) || NumberUtil.INTEGER_FIVE.equals(dayOfWeek)) {
                storeDeliveryFlag = true;
            } else {
                storeDeliveryFlag = false;
            }
        }
        return storeDeliveryFlag;
    }

    /**
     * 根据配送周期获取截单时间
     *
     * @param deliveryWeekList
     * @param cutoffTimeStr
     * @return
     */
    public static String getTruncationDateTimeByDeliveryCycle(List<Integer> deliveryWeekList, String cutoffTimeStr) {
        if (CollectionUtils.isEmpty(deliveryWeekList)) {
            throw new BusinessException("配送周期配置为空");
        }
        if (StringUtils.isBlank(cutoffTimeStr)) {
            throw new BusinessException("提单时间点为空");
        }
        deliveryWeekList = deliveryWeekList.stream().sorted().collect(Collectors.toList());
        // 提单时间点转化截单时间
        String cutoffDateTimeStr = LocalDate.now() + " " + cutoffTimeStr + ":00";
        LocalDateTime cutoffTime = DateUtils.parseTime(cutoffDateTimeStr);
        // 获取当前星期几
        AtomicInteger nowDayOfWeek = new AtomicInteger(LocalDateTime.now().getDayOfWeek().getValue());
        AtomicReference<String> storeDeliveryDate = new AtomicReference<>();
        Integer maxWeekValue = Collections.max(deliveryWeekList);
        // 如果当前星期比预设值最大星期大
        if (nowDayOfWeek.get() > maxWeekValue) {
            storeDeliveryDate.set(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.of(deliveryWeekList.get(0)))).toString());
        } else {
            if (deliveryWeekList.contains(nowDayOfWeek.get())) {
                if (LocalDateTime.now().isBefore(cutoffTime)) {
                    storeDeliveryDate.set(DateUtils.format(LocalDate.now()));
                } else {
                    getNextOneDeliveryDate(deliveryWeekList, maxWeekValue, nowDayOfWeek, storeDeliveryDate);
                }
            } else {
                getNextOneDeliveryDate(deliveryWeekList, maxWeekValue, nowDayOfWeek, storeDeliveryDate);
            }
        }
        if (StringUtils.isBlank(storeDeliveryDate.get())) {
            throw new BusinessException("未指定的门店配货类型");
        }
        return storeDeliveryDate.get() + " " + cutoffTimeStr;
    }

    private static void getNextOneDeliveryDate(List<Integer> deliveryWeekList, int maxWeekValue, AtomicInteger nowDayOfWeek, AtomicReference<String> storeDeliveryDate) {
        if (nowDayOfWeek.get() < maxWeekValue) {
            for (Integer value : deliveryWeekList) {
                if (value <= nowDayOfWeek.get()) {
                    continue;
                }
                nowDayOfWeek.set(value);
                break;
            }
        } else {
            nowDayOfWeek.set(Collections.min(deliveryWeekList));
        }
        storeDeliveryDate.set(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.of(nowDayOfWeek.get()))).toString());
    }

    public static boolean checkStoreDeliveryByDeliveryWeekList(List<Integer> deliveryWeekList) {
        Integer dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        Map<Integer, Integer> map = deliveryWeekList.stream().collect(Collectors.toMap(k -> k, v -> v));
        return map.containsKey(dayOfWeek);
    }
}
