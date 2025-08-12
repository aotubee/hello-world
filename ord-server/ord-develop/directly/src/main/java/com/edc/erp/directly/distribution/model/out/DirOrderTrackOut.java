package com.edc.erp.directly.distribution.model.out;

import com.edc.erp.directly.distribution.entity.OrdDirOrderTrack;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author fxw
 * @description: 订单追踪出参实体
 * @since 2022/10/18 14:23
 */
@Data
public class DirOrderTrackOut extends OrdDirOrderTrack implements Serializable {
    private static final long serialVersionUID = -4319343176772875294L;

    @ApiModelProperty(value = "createTimeStr",name = "创建时间中文")
    private String createTimeStr;
}
