package com.edc.erp.disdeliveryorder.model.in;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName DisOverallDeliveryAsyncImportIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/11/22 17:21
 **/
@Data
public class DisOverallDeliveryAsyncImportIn implements Serializable {
    private static final long serialVersionUID = -8721710750345900565L;

    @ApiModelProperty(name = "fileId", value = "文件id")
    private String fileId;

    /**
     * 仓储代码
     */
    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String wrhCode;


    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 配销方式
     */
    @ApiModelProperty(name = "distributionType", value = "配销方式")
    private String distributionType;
}
