package com.edc.erp.common.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 异步任务执行(AsyncTask)实体类
 *
 * @author zhangdong
 * @since 2022-08-30 17:21:01
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "opt_async_task")
public class AsyncTask implements Serializable{

    private static final long serialVersionUID = -77428488491280197L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "业务主键")
    private Integer bizId;

    @ApiModelProperty(value = "数据")
    private String messageJson;

    @ApiModelProperty(value = "业务单号")
    private String businessNo;

    @ApiModelProperty(value = "重试次数")
    private Integer retryNo;


    @ApiModelProperty(value = "下次重试时间")
    private Date nextRetryTime;

    @ApiModelProperty(value = "返回结果")
    private String returnResult;

    @ApiModelProperty(value = "异常信息")
    private String exceptionMessage;

    @ApiModelProperty(value = "执行状态")
    private String execStatus;

    @ApiModelProperty(value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(value = "备注")
    private String remark;
}
