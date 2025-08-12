package com.edc.erp.presale.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName CheckExistPresaleActivityIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/28 18:58
 **/
@Data
public class CheckExistPresaleActivityIn implements Serializable {
    private static final long serialVersionUID = 6811910045966338301L;

    @ApiModelProperty(name = "storeCodeList", required = true, value = "门店代码集合")
    private List<String> storeCodeList;

    @ApiModelProperty(name = "goodsCodeList", required = true, value = "商品代码集合")
    private List<String> goodsCodeList;

    @ApiModelProperty(name = "beginOrderDate", required = true, value = "预售可订货开始时间")
    private LocalDateTime beginOrderDate;

    @ApiModelProperty(name = "endOrderDate", required = true, value = "预售可订货结束时间")
    private LocalDateTime endOrderDate;

    @ApiModelProperty(name = "statusList", required = true, value = "预售活动状态")
    private List<String> statusList;

    @ApiModelProperty(name = "id", required = true, value = "预售活动id")
    private Long id;

    @ApiModelProperty(name = "isGift", required = true, value = "是否赠品")
    private Integer isGift;
}
