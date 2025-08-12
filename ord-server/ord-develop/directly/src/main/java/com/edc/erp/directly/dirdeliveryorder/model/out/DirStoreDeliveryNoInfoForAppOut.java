package com.edc.erp.directly.dirdeliveryorder.model.out;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName StoreDeliveryNoInfoForAppOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/10/18 10:05
 **/
@Data
public class DirStoreDeliveryNoInfoForAppOut implements Serializable {
    private static final long serialVersionUID = -4777967058492493831L;

    @ApiModelProperty(name = "id", value = "配货单ID")
    private Long id;

    @ApiModelProperty(name = "deliveryOrderNo", value = "配货单号")
    private String deliveryOrderNo;

    @ApiModelProperty(name = "detailList", value = "配货明细")
    private List<OrdDirDeliveryDetail> detailList;
}
