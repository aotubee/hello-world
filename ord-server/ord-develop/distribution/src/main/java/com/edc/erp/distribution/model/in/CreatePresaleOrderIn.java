package com.edc.erp.distribution.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName SubmitPresaleOrderIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/4 17:42
 **/
@Data
public class CreatePresaleOrderIn implements Serializable {
    private static final long serialVersionUID = 2499489671178627347L;

    @ApiModelProperty(name = "presaleActivityId", value = "预售活动id")
    private Long presaleActivityId;

    @ApiModelProperty(name = "createPresaleOrderGoodsInList", value = "商品明细")
    private List<CreatePresaleOrderGoodsIn> createPresaleOrderGoodsInList;


}
