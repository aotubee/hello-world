/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.entity;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * opt_app_notification数据库实体类
 *
 * @author liwenqiang
 */
@Data
@Table(name = "opt_app_notification")
public class OptAppNotification extends BaseEntity implements java.io.Serializable {

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 接收人
     */
    @ApiModelProperty(name = "receiver", value = "接收人")
    private String receiver;

    /**
     * 任务ID
     */
    @ApiModelProperty(name = "taskId", value = "任务ID")
    private String taskId;

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
     * 状态
     */
    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    /**
     * 状态
     */
    @ApiModelProperty(name = "type", value = "类型")
    private String type;

    /**
     * 发送时间
     */
    @ApiModelProperty(name = "sendTime", value = "发送时间")
    private LocalDateTime sendTime;

    /**
     * 是否删除
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;


}
