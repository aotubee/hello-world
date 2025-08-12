package com.edc.erp.disdeliveryorder.model.in.zk;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @ClassName WholesaleShipmentIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/1 16:52
 **/
@Data
public class ZKWholesaleShipmentIn implements Serializable {
    private static final long serialVersionUID = -5891895799590597065L;

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode", value = "客户代码", required = true)
    @NotBlank(message = "客户代码不能为空!")
    private String clientCode;

    /** 来源单号 */
    @ApiModelProperty(name = "sourceNo", value = "中科要货单单号", required = true)
    @NotBlank(message = "来源单号不能为空!")
    private String sourceNo;

    /** 出库仓位 */
    @ApiModelProperty(name = "shipmentStockCode", value = "出库仓位", required = true)
    @NotBlank(message = "出库仓位不能为空!")
    private String shipmentStockCode;

    /** 配货方式 */
    @ApiModelProperty(name = "distributionWay", value = "配货方式（统配、直采统配）", required = true)
    @NotBlank(message = "配货方式不能为空!")
    private String distributionWay;

    /** 司机姓名 */
    @ApiModelProperty(name = "driverName", value = "司机姓名", required = true)
    @NotBlank(message = "司机姓名不能为空!")
    private String driverName;

    /** 司机联系电话 */
    @ApiModelProperty(name = "driverPhone", value = "司机联系电话", required = true)
    @NotBlank(message = "司机联系电话不能为空!")
    private String driverPhone;

//    /** 收货人 */
//    @ApiModelProperty(name = "consignee", value = "收货人（客户资料配送信息）", required = true)
//    @NotBlank(message = "收货人不能为空!")
//    private String consignee;
//
//    /** 收货人电话 */
//    @ApiModelProperty(name = "consigneePhone", value = "收货人电话（客户资料配送信息）", required = true)
//    @NotBlank(message = "收货人手机号不能为空!")
//    private String consigneePhone;

    /** 组织 */
    @ApiModelProperty(name = "orgCode", value = "组织", required = true)
    @NotBlank(message = "组织不能为空!")
    private String orgCode;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人", required = true)
    @NotBlank(message = "创建人不能为空!")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间", required = true)
    @NotEmpty(message = "创建时间不能为空!")
    private LocalDateTime createTime;

    /** 备注 */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;
}
