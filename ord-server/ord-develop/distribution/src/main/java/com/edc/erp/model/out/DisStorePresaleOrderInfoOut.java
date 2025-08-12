package com.edc.erp.model.out;

import com.edc.erp.presale.entity.OrdDisPresaleOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName DisStorePresaleOrderInfoOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/30 15:59
 **/
@Data
public class DisStorePresaleOrderInfoOut extends OrdDisPresaleOrder implements Serializable {
    private static final long serialVersionUID = 643505507553075789L;

    @ApiModelProperty(name = "statusStr", value = "状态中文")
    private String statusStr;

    @ApiModelProperty(value = "所属区域")
    private String belongArea;

    @ApiModelProperty(name = "detailInfoOutList", value = "明细集合")
    private List<StorePresaleOrderDetailInfoOut> detailInfoOutList;
}
