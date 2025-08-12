package com.edc.erp.disfirstorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 铺货单拆单品类
 *
 * @author weichao
 */
@Data
public class FirstOrderSortOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private Integer id;

    @ApiModelProperty(value = "配置表id")
    private Integer configId;

    @ApiModelProperty(value = "品类代码")
    private String sortCode;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "品类名称")
    private String sortName;

}
