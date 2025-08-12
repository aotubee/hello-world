package com.edc.erp.wholesale.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName ApiWholesaleOrderIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/10 10:14
 **/
@Data
public class ApiWholesaleOrderIn implements Serializable {

    private static final long serialVersionUID = 3159242145032679181L;

    @ApiModelProperty(name = "erpOrderNo", value = "erp单号")
    private String erpOrderNo;

    @ApiModelProperty(name = "sourceNo", value = "三方来源单号")
    private String sourceNo;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode", value = "客户代码")
    private String clientCode;

    /** 价格组代码 */
    @ApiModelProperty(name = "priceGroupCode", value = "价格组代码")
    private String priceGroupCode;

    /** 出库仓储 */
    @ApiModelProperty(name = "warehouseCode", value = "仓储")
    private String warehouseCode;

    /** 出库仓位 */
    @ApiModelProperty(name = "stockCode", value = "仓位")
    private String stockCode;

    /** 收货人 */
    @ApiModelProperty(name = "consignee", value = "收货人")
    private String consignee;

    /** 收货人电话 */
    @ApiModelProperty(name = "consigneePhone", value = "收货人电话")
    private String consigneePhone;

    /** 详细地址 */
    @ApiModelProperty(name = "addressDetail", value = "详细地址")
    private String addressDetail;

    @ApiModelProperty(name = "distributionType", value = "配货方式")
    private String distributionType;

    @ApiModelProperty(value = "订单优先级")
    private String orderPriority;

    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    @ApiModelProperty(name = "detailList", value = "明细集合")
    private List<ApiWholesaleDetailIn> detailList;

    @ApiModelProperty(name = "type", value = "方法类型")
    private String type;

    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "客户ID")
    private Integer clientId;

    /** 司机姓名 */
    @ApiModelProperty(name = "driverName", value = "司机姓名", required = true)
    @NotBlank(message = "司机姓名不能为空!")
    private String driverName;

    /** 司机联系电话 */
    @ApiModelProperty(name = "driverPhone", value = "司机联系电话", required = true)
    @NotBlank(message = "司机联系电话不能为空!")
    private String driverPhone;
}
