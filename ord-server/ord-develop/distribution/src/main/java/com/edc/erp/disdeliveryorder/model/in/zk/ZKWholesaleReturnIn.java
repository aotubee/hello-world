package com.edc.erp.disdeliveryorder.model.in.zk;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @ClassName WholesaleReturnIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/1 17:06
 **/
@Data
public class ZKWholesaleReturnIn implements Serializable {
    private static final long serialVersionUID = -6520754223113650512L;

    @ApiModelProperty(name = "wholesaleShipmentNo", value = "原批发出货单单号")
//    @NotBlank(message = "批发出货单单号不能为空!")
    private String wholesaleShipmentNo;

    /**
     * 客户代码
     */
    @ApiModelProperty(name = "clientCode", value = "客户代码", required = true)
    @NotBlank(message = "客户代码不能为空!")
    private String clientCode;

    /**
     * 入库仓位
     */
    @ApiModelProperty(name = "storageStockCode", value = "入库仓位", required = true)
    @NotBlank(message = "入库仓位不能为空!")
    private String storageStockCode;

    /**
     * 来源单号
     */
    @ApiModelProperty(name = "sourceNo", value = "中科配送退货单单号", required = true)
    @NotBlank(message = "来源单号不能为空!")
    private String sourceNo;


    /**
     * 退货原因
     */
    @ApiModelProperty(name = "returnsReason", value = "退货原因", required = true)
    private String returnsReason;

//    /**
//     * 收货人
//     */
//    @ApiModelProperty(name = "consignee", value = "收货人（客户资料中配送信息）")
//    private String consignee;
//
//    /**
//     * 收货人手机号
//     */
//    @ApiModelProperty(name = "consigneePhone", value = "收货人手机号（客户资料配送信息）")
//    private String consigneePhone;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /** 组织 */
    @ApiModelProperty(name = "orgCode", value = "组织", required = true)
    @NotBlank(message = "组织不能为空!")
    private String orgCode;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人", required = true)
    @NotBlank(message = "创建人不能为空!")
    private String creator;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间", required = true)
    @NotEmpty(message = "创建时间不能为空!")
    private LocalDateTime createTime;
}
