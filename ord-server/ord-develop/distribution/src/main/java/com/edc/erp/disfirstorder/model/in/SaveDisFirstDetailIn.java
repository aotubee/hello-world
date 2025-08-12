package com.edc.erp.disfirstorder.model.in;

import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName SaveDirDistributionDetailIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/12 16:35
 **/
@Data
public class SaveDisFirstDetailIn implements Serializable {
    private static final long serialVersionUID = -3074921741199288346L;

    @ApiModelProperty(name = "firstOrderId", value = "铺货单主键ID")
    private Long firstOrderId;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 是否即时生效
     */
    @ApiModelProperty(name = "isEffectiveImmediately", value = "是否即时生效(0:否 1是)")
    private Integer isEffectiveImmediately;

    /**
     * 生效时间
     */
    @ApiModelProperty(name = "effectiveTime", value = "生效时间")
    private LocalDateTime effectiveTime;

    @ApiModelProperty(name = "detailList", value = "铺货明细集合")
    private List<OrdDisOrderFirstDetail> detailList;


}
