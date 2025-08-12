package com.edc.erp.presale.model.out;

import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName OrdDisPresaleAssetsDetailOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/3 18:28
 **/
@Data
public class OrdDisPresaleAssetsDetailOut extends OrdDisPresaleAssetsDetail implements Serializable {
    private static final long serialVersionUID = -3842802634587224443L;

    @ApiModelProperty(name = "status", value = "活动状态")
    private String status;

    @ApiModelProperty(name = "statusStr", value = "活动状态中文")
    private String statusStr;
}
