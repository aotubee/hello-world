package com.edc.erp.common.model.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author wld
 * @description:批发价格组实体类
 * @since 2022/10/27 8:43
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class PriceGroupVo implements Serializable {

    /**
     * 批发价格组
     */
    @ApiModelProperty(name = "priceGroup", value = "批发价格组")
    private String priceGroup;

    /**
     * 批发价格组代码
     */
    @ApiModelProperty(name = "priceGroupCode", value = "批发价格组代码")
    private String priceGroupCode;

    /**
     * 批发价格组名称
     */
    @ApiModelProperty(name = "priceGroupName", value = "批发价格组名称")
    private String priceGroupName;

    /**
     * 将价格组名称和价格组代码合并为批发价格组
     *
     * @param priceGroupCode
     * @param priceGroupName
     * @return
     */
    public static String mergeCodeAndName(String priceGroupCode, String priceGroupName) {
        String priceGroup = "【" + priceGroupCode + "】" + priceGroupName;
        return priceGroup;
    }

    /**
     * 根据批发价格组转换成价格组对象
     *
     * @param priceGroup
     * @return
     */
    public static PriceGroupVo changPriceGroup(String priceGroup) {
        String priceGroupCode = priceGroup.substring(priceGroup.indexOf("【") + 1, priceGroup.indexOf("】"));
        String priceGroupName = priceGroup.substring(priceGroup.indexOf("】") + 1);
        PriceGroupVo priceGroupVo = PriceGroupVo.builder().priceGroup(priceGroup).priceGroupCode(priceGroupCode).priceGroupName(priceGroupName).build();
        return priceGroupVo;
    }
}
