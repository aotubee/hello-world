package com.edc.erp.ord.model.in;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName MqTaskRetryIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/7/16 9:11
 **/
@Data
public class MqTaskRetryIn implements Serializable {
    private static final long serialVersionUID = 7158864650455874795L;

    @NotEmpty(message = "业务类型不能为空")
    private String taskType;

    @NotEmpty(message = "业务单号不能为空")
    private String taskBusinessNos;

    @NotNull(message = "业务单号不能为空,1:直营，2：加盟")
    private Integer storeType;
}
