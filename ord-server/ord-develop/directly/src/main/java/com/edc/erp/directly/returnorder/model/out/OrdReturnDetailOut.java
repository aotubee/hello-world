package com.edc.erp.directly.returnorder.model.out;

import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 退货单明细出参对象
 *
 * @author yaojinpeng
 * @since 2022/10/27 22:17
 */
@Data
public class OrdReturnDetailOut extends OrdDirReturnDetail {

    /**
     * 包装规格
     */
    private String specification;

    /**
     * 单位
     */
    private String unity;

    /**
     * 退货单号
     */
    @ApiModelProperty(name = "returnOrderNo", value = "退货单号")
    private String returnOrderNo;

    /**
     * 退货状态
     */
    @ApiModelProperty(name = "returnStatus", value = "退货状态")
    private String returnStatus;

    /**
     * 退货状态中文值
     */
    @ApiModelProperty(name = "returnStatusValue", value = "退货状态中文值")
    private String returnStatusValue;

    /**
     * 退货类型
     */
    @ApiModelProperty(name = "returnType", value = "退货类型")
    private String returnType;

    /**
     * 退货类型中文值
     */
    @ApiModelProperty(name = "returnTypeValue", value = "退货类型中文值")
    private String returnTypeValue;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 商品集合
     */
    @ApiModelProperty(name = "goodsInfo", value = "商品集合")
    private List<OrdDirReturnDetail> goodsInfo;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remak", value = "备注")
    private String remark;

    @ApiModelProperty(name = "hdReturnNo", value = "海鼎退货单号")
    private String hdReturnNo;

    /** 品类属性中文 */
    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性中文")
    private String goodsTypeStr;

    @ApiModelProperty(name = "invoiceTypeStr", value = "发票类型中文")
    private String invoiceTypeStr;

    @ApiModelProperty(value = "商品是否管理效期")
    private Integer isManageValidityPeriod;
}
