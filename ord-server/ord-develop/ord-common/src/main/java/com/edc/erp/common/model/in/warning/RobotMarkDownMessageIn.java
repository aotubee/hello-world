package com.edc.erp.common.model.in.warning;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.util.List;


/**
 * 预警消息入参
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-08-25 13:56
 */
@Data
public class RobotMarkDownMessageIn extends BaseEntity {

    /**
     * 首屏会话透出的展示内容
     */
    private String title;

    /**
     * markdown格式的消息，建议500字符以内
     */
    private String text;

    /**
     * 钉钉用户手机号
     */
    private List<String> mobileList;

    /**
     * 钉钉机器人通知地址
     */
    private String url;

    /**
     * 是否通知所有人
     */
    private Boolean isAtAll;
}
