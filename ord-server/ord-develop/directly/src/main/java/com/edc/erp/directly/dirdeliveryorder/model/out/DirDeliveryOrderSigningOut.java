package com.edc.erp.directly.dirdeliveryorder.model.out;


import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderSigning;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 配货单签收对象出参
 * @since 2022/10/26 18:15
 */
@Data
public class DirDeliveryOrderSigningOut extends OrdDirDeliveryOrderSigning implements Serializable {
    private static final long serialVersionUID = 1408656770700055975L;

    /**
     * 签收附件
     *
     */
    @ApiModelProperty(name = "signingAttachmentUrlList", value = "签收附件")
    private List<String> signingAttachmentUrlList;
}
