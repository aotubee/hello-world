package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author weichao
 * @date 2021-12-16
 */
@Data
public class OrgSortOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "品类代码")
    private String sort;

    @ApiModelProperty(value = "品类名称")
    private String sortName;

    @ApiModelProperty(value = "品类员ID")
    private Integer sortManager;

    @ApiModelProperty(value = "品类标准")
    private String sortStandard;

    @ApiModelProperty(value = "父代码")
    private String parentSortCode;

    @ApiModelProperty(value = "品类属性")
    private String goodsType;

    @ApiModelProperty(value = "标准商品类型名称")
    private String goodsTypeName;

    @ApiModelProperty(value = "创建人")
    private String creator;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改人")
    private String updater;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "最大品项数")
    private Integer maxItemNum;

    @ApiModelProperty(value = "商品数")
    private Integer itemNum;

    @ApiModelProperty(value = "子集合")
    private List<OrgSortOut> childList;

    @ApiModelProperty(value = "品类员")
    private String sortManagerName;

    @ApiModelProperty(value = "是否展示")
    private Integer isShow;
}
