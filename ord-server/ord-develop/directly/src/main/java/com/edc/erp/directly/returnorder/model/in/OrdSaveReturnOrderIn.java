package com.edc.erp.directly.returnorder.model.in;

import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 保存退货单入参
 *
 * @author yaojinpeng
 * @since 2022/10/24 15:46
 */
@Data
public class OrdSaveReturnOrderIn extends BaseEntity {


    /**
     * 退货单主键
     */
    @ApiModelProperty(name = "returnOrderId", value = "退货单主键")
    private Integer returnOrderId;

    /**
     * 退货通知单主键
     */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "退货通知单主键")
    private Integer returnNoticeOrderId;

    /**
     * 退货商品入参集合
     */
    @ApiModelProperty(name = "returnGoodsInfoInList", value = "退货商品入参集合")
    private List<OrdDirReturnDetail> returnGoodsInfoInList;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;


    /**
     * 仓储代码
     */
    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String warehouseCode;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "returnType", value = "退货类型")
    private String returnType;

    private String bizOrgCode;

    /**
     * 门店区域
     */
    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /**
     * 退货单原因
     */
    @ApiModelProperty(name = "returnOrderReason", value = "退货单原因")
    private String returnOrderReason;

    /**
     * 配送方式
     */
    @ApiModelProperty(name = "distributionType", value = "配送方式")
    private String distributionType;

    @ApiModelProperty(name = "loginUsername", value = "登录人")
    private String loginUsername;

    @ApiModelProperty(name = "deliveryOrderNo", value = "配货单单号")
    private String deliveryOrderNo;

    @ApiModelProperty(name = "imageUrlList", value = "上传图片")
    private List<String> imageUrlList;

    @ApiModelProperty(name = "centerStockBizOrgCode", value = "中心仓业务组织代码")
    private String centerStockBizOrgCode;
}
