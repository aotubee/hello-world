package com.edc.erp.presale.model.out;


import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 退货单(DisReturn)实体类
 *
 * @author yaojinpeng
 * @since 2022-10-24 15:35:29
 */
@Data
public class OrdDisPresaleAssetsDetailExtOut extends OrdDisPresaleAssetsDetail implements Serializable {

    private static final long serialVersionUID = 1L;

//    @ApiModelProperty(name = "surplusPackageQuantity", value = "剩余包装数")
//    private BigDecimal surplusPackageQuantity;
}
