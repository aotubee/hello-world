package com.edc.erp.presale.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @ClassName ExtendOrderDateIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/8 9:12
 **/
@Data
public class ExtendOrderDateIn implements Serializable {

    @ApiModelProperty(name = "id", required = true, value = "预售活动id")
    private Long id;

    @ApiModelProperty(name = "endOrderDate", required = true, value = "可订货结束时间")
    @NotBlank(message = "可订货结束时间不能为空")
    private String endOrderDate;
}
