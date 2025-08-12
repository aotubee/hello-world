package com.edc.erp.common.model.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织商品实体类
 *
 * @author wanglidong
 * @since 2022/11/4 18:46
 */
@Data
public class OrgGoodsInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long orgGoodsId;

    @ApiModelProperty(value = "标准商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "助记码")
    private String mnemonicCode;

    @ApiModelProperty(value = "组织主条码")
    private String orgGoodsBar;

    @ApiModelProperty(value = "组织商品名称")
    private String orgGoodsName;

    @ApiModelProperty(value = "运营品类代码")
    private String orgSort;

    @ApiModelProperty(value = "组织商品类型")
    private String orgGoodsType;

    @ApiModelProperty(value = "商品标签")
    private String goodsTag;

    @ApiModelProperty(value = "商品卖点")
    private String sellingPoint;

    @ApiModelProperty(value = "生命周期ID")
    private Integer lifeCycleId;

    @ApiModelProperty(value = "商品状态ID")
    private Integer goodsStatusId;

    @ApiModelProperty(value = "商品品类ID")
    private Integer sortId;

    @ApiModelProperty(value = "公司代码")
    private String orgCode;

    @ApiModelProperty(value = "业务组织代码")
    private String bizOrgCode;

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
}