package com.edc.erp.directly.returnnoticeorder.model.out;

import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNoticeGoods;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 分页查询退货通知单下商品信息列表出参
 *
 * @author yaojinpeng
 * @since 2022/10/21 16:08
 */
@Data
@ApiModel
public class OrdReturnNoticeGoodsOut extends OrdDirReturnNoticeGoods implements Serializable {



    /**
     * 品牌名称
     */
    @ApiModelProperty(name = "brandName", value = "品牌名称")
    private String brandName;


    /**
     * 品牌
     */
    @ApiModelProperty(name = "id", value = "商品主键")
    private Integer id;

    /**
     * 品牌
     */
    @ApiModelProperty(name = "StoreInfo", value = "门店信息")
    private List<OrdReturnNoticeStoreOut> storeInfo;


    /**
     * 是否可退
     */
    @ApiModelProperty(name = "isCanReturn", value = "是否可退")
    private Integer isCanReturn;

    /**
     * 门店库存
     */
    @ApiModelProperty(name = "storeInventory", value = "门店库存")
    private BigDecimal storeInventory;


    /**
     * 限量退货时可退数量
     */
    @ApiModelProperty(value = "限量退货时可退数量")
    private BigDecimal qty;

    /** 品类属性中文名 */
    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性中文名")
    private String goodsTypeStr;

    @ApiModelProperty(value = "商品是否管理效期")
    private Integer isManageValidityPeriod;
}
