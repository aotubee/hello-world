package com.edc.erp.presale.model.in;

import com.edc.erp.presale.entity.OrdDisPresaleActivityGoods;
import com.edc.erp.presale.entity.OrdDisPresaleActivityStore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName SaveDisPresaleActivityIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/23 8:45
 **/
@Data
@ApiModel(value = "SaveDisPresaleActivityIn", description = "保存预售活动入参")
public class SaveDisPresaleActivityIn implements Serializable {
    private static final long serialVersionUID = -8405748808012562951L;

    @ApiModelProperty(name = "id", required = true, value = "预售活动id")
    private Long id;

    /**
     * 预售活动开始时间
     */
    @ApiModelProperty(name = "beginSaleDate", required = true, value = "预售活动开始时间")
    @NotBlank(message = "预售活动开始时间不能为空")
    private LocalDateTime beginSaleDate;

    /**
     * 预售活动结束时间
     */
    @ApiModelProperty(name = "endSaleDate", required = true, value = "预售活动结束时间")
    @NotBlank(message = "预售活动结束时间不能为空")
    private LocalDateTime endSaleDate;

    /**
     * 可订货开始时间
     */
    @ApiModelProperty(name = "beginOrderDate", required = true, value = "可订货开始时间")
    @NotBlank(message = "可订货开始时间不能为空")
    private LocalDateTime beginOrderDate;

    /**
     * 可订货结束时间
     */
    @ApiModelProperty(name = "endOrderDate", required = true, value = "可订货结束时间")
    @NotBlank(message = "可订货结束时间不能为空")
    private LocalDateTime endOrderDate;

    /**
     * 活动简介
     */
    @ApiModelProperty(name = "profile", value = "活动简介")
    private String profile;

    /**
     * app图片
     */
    @ApiModelProperty(name = "presaleImage", required = true, value = "app图片")
    @NotBlank(message = "app图片不能为空")
    private String presaleImage;

    /**
     * 是否活动限购
     */
    @ApiModelProperty(name = "isLimitBuy", required = true, value = "是否活动限购")
    @NotNull(message = "是否活动限购不能为空")
    private Integer isLimitBuy;

    /**
     * 限购次数
     */
    @ApiModelProperty(name = "limitBuyTime", value = "限购次数")
    private Integer limitBuyTime;

    /**
     * 促销价
     */
    @ApiModelProperty(name = "promotionalPrice", required = true, value = "促销价")
    @NotNull(message = "促销价不能为空")
    private BigDecimal promotionalPrice;

    @ApiModelProperty(name = "activityGoodsList", required = true, value = "活动商品")
    @NotNull(message = "活动商品不能为空")
    private List<OrdDisPresaleActivityGoods> activityGoodsList;

    @ApiModelProperty(name = "activityStoreList", required = true, value = "活动门店")
    @NotNull(message = "活动门店不能为空")
    private List<OrdDisPresaleActivityStore> activityStoreList;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "loginUsername", value = "登录人")
    private String loginUsername;

    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;
}
