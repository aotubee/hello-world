package com.edc.erp.common.model.in.warning;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 预警通知入参
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-08-25 10:46
 */
@Data
public class WarningIn extends BaseEntity {

    /**
     * 系统编号
     */
    @NotNull(message = "系统编号不能为空")
    private String systemNo;

    /**
     * 业务类型
     */
    @NotNull(message = "业务类型不能为空")
    private String businessType;

    /**
     * 错误代码
     */
    @NotNull(message = "错误代码不能为空")
    private String errorCode;

    /**
     * 占位符错误信息不能为空
     */
    @NotNull(message = "占位符错误信息不能为空")
    private String placeholderErrorMessage;

    /**
     * 原始异常
     */
    @NotNull(message = "原始异常不能为空")
    private String originalException;

    /**
     * 业务参数
     */
    private String businessParam;

    /**
     * 组织代码
     */
    @NotNull(message = "组织代码不能为空")
    private String bizOrgCode;

}
