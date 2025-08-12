package com.edc.erp.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * 星期枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021年1月8日 14:52
 */
public enum WeekEnum {

    MONDAY("MONDAY", 1, "星期一"),
    TUESDAY("TUESDAY", 2, "星期二"),
    WEDNESDAY("WEDNESDAY", 3, "星期三"),
    THURSDAY("THURSDAY", 4, "星期四"),
    FRIDAY("FRIDAY", 5, "星期五"),
    SATURDAY("SATURDAY", 6, "星期六"),
    SUNDAY("SUNDAY", 7, "星期天");

    private String code;
    private Integer weekDay;
    private String des;


    WeekEnum(String code, Integer weekDay, String des) {
        this.code = code;
        this.weekDay = weekDay;
        this.des = des;
    }

    public String getCode() {
        return code;
    }

    public Integer getWeekDay() {
        return weekDay;
    }

    public String getDes() {
        return des;
    }

    /**
     * 根据code 得到对应的 周（几）
     *
     * @param code
     * @return
     */
    public static Integer getWeekDayByCode(String code) {
        Integer result = null;
        for (WeekEnum order : WeekEnum.values()) {
            if (StringUtils.equals(order.getCode(), code)) {
                result = order.getWeekDay();
                break;
            }
        }
        return result;
    }

    /**
     * 根据code 得到对应的 周
     *
     * @param weekDay
     * @return
     */
    public static String getDesByWeekDay(Integer weekDay) {
        String result = null;
        for (WeekEnum order : WeekEnum.values()) {
            if (order.getWeekDay().equals(weekDay)) {
                result = order.getDes();
                break;
            }
        }
        return result;
    }

}
