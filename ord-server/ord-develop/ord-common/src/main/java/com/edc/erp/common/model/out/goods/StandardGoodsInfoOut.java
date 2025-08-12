package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 标准商品表
 * </p>
 *
 * @author gusiyuan
 * @since 2021-12-11
 */
@Data
@ApiModel(value="StandardGoodsInfo对象", description="标准商品表")
public class StandardGoodsInfoOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(value = "商品主条码")
    private String barCode;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "英文名称")
    private String enGoodsName;

    @ApiModelProperty(value = "产地")
    private String origin;

    @ApiModelProperty(value = "保质期")
    private String expirationDate;

    @ApiModelProperty(value = "商品品牌ID")
    private Integer brandId;

    @ApiModelProperty(value = "标准品类代码")
    private String standardSort;

    @ApiModelProperty(value = "进项税率ID")
    private Integer inTaxId;

    @ApiModelProperty(value = "销项税率ID")
    private Integer outTaxId;

    @ApiModelProperty(value = "品类属性")
    private String goodsType;

    @ApiModelProperty(value = "商品来源类型")
    private String sourceType;

    @ApiModelProperty(value = "主图")
    private String imgUrl;

    @ApiModelProperty(value = "创建人")
    private String creator;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改人")
    private String updater;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;

    public StandardGoodsInfoOut(String goodsCode, Integer isDelete) {
        this.goodsCode = goodsCode;
        this.isDelete = isDelete;
    }
}
