package com.edc.erp.common.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName AsyncTaskQueryOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/3/2 10:04
 **/
@Data
public class AsyncTaskQueryOut implements Serializable {
    private static final long serialVersionUID = 2327603169685013326L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    private Integer id;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "类型中文")
    private String typeStr;

    @ApiModelProperty(value = "数据")
    private String messageJson;

    @ApiModelProperty(value = "业务单号")
    private String businessNo;

    @ApiModelProperty(value = "异常信息")
    private String exceptionMessage;

    @ApiModelProperty(value = "执行状态")
    private String execStatus;

    @ApiModelProperty(value = "备注")
    private String remark;
}
