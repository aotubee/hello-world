/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.in.unipush;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OptAppNotificationIn入参查询类
 *
 * @author liwenqiang
 */
@Data
public class OptAppNotificationIn implements Serializable {

    private Integer id;
    /**
     * 接收人
     */
    @ApiModelProperty(name = "receiver", value = "接收人")
    private String receiver;

    /**
     * 标题
     */
    @ApiModelProperty(name = "title", value = "标题")
    private String title;

    /**
     * 内容
     */
    @ApiModelProperty(name = "content", value = "内容")
    private String content;

    /**
     * 透传数据
     */
    @ApiModelProperty(name = "content", value = "透传数据")
    private NotifyPayload notifyPayload;

    /**
     * 通知类型
     */
    @ApiModelProperty(name = "type", value = "通知类型")
    private String type;

    /**
     * 发送时间
     */
    @ApiModelProperty(name = "sendTime", value = "发送时间")
    private LocalDateTime sendTime;

    /**
     * url消息链接
     */
    @ApiModelProperty(name = "url", value = "url消息链接")
    private String url;

    /**
     * 接收人
     */
    @ApiModelProperty(name = "sender", value = "发送人")
    @NotBlank
    private String sender;
}
