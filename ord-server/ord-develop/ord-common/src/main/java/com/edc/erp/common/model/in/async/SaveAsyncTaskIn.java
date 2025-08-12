package com.edc.erp.common.model.in.async;

import com.edc.erp.common.entity.AsyncTask;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author fxw
 * @description: 异步任务入参
 * @since 2022/10/27 11:06
 */
@Data
public class SaveAsyncTaskIn implements Serializable {
    private static final long serialVersionUID = 4068033652895365422L;
    @ApiModelProperty(name = "asyncTask",value = "异步任务")
    private AsyncTask asyncTask;
}
