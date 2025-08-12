package com.edc.erp.presale.model.out;

import com.edc.erp.presale.entity.OrdDisPresaleActivity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @ClassName CheckExtendEndOrderDateOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/8 10:47
 **/
@Data
public class CheckExtendEndOrderDateOut implements Serializable {

    private OrdDisPresaleActivity ordDisPresaleActivity;

    @ApiModelProperty(name = "oldEndOrderDate", value = "原可订货结束时间")
    private LocalDateTime oldEndOrderDate;
}
