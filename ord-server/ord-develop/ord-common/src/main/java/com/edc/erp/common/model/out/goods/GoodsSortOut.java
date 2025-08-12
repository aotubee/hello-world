package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @ClassName GoodsSortOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/3/5 9:40
 **/
@Data
public class GoodsSortOut implements Serializable {
    private static final long serialVersionUID = -2675003330422640771L;

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
}
