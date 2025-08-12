package com.edc.erp.returnnoticeorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


/**
 * @author lh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnStoreInfoIn  implements Serializable {

    /**
     * 门店表id
     */
    @ApiModelProperty(name = "id", value = "门店表id")
    private Integer id;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 可退数量
     */
    @ApiModelProperty(name = "returnNum", value = "可退数量")
    private BigDecimal returnNum;

    /**
     * 加盟商
     */
    @ApiModelProperty(name = "client", value = "加盟商")
    private String client;
}
