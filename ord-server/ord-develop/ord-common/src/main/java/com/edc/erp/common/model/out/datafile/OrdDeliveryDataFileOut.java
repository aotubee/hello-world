package com.edc.erp.common.model.out.datafile;

import com.edc.erp.common.entity.OrdDeliveryDataFile;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrdDeliveryDataFileOut extends OrdDeliveryDataFile implements Serializable {

    @ApiModelProperty(name = "execStatusStr", value = "执行状态中文")
    private String execStatusStr;

    @ApiModelProperty(name = "fileTypeStr", value = "文件类型中文")
    private String fileTypeStr;
}
