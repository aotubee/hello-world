package com.edc.erp.directly.returnnoticeorder.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;



/**
 * 退货通知单与门店表(OrdDirReturnNoticeStore)实体类
 *
 * @author
 * @since 2022-11-18 19:09:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_return_notice_store")
@ApiModel(value = "OrdDirReturnNoticeStore", description = "退货通知单与门店表")
public class OrdDirReturnNoticeStore implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 退货通知单主键 */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "退货通知单主键")
    private Integer returnNoticeOrderId;

    /** 退货通知单商品表id */
    @ApiModelProperty(name = "returnNoticeGoodsId", value = "退货通知单商品表id")
    private Integer returnNoticeGoodsId;

    /** 门店代码 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /** 门店名称 */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /** 限量退货时可退数量 */
    @ApiModelProperty(name = "qty", value = "限量退货时可退数量")
    private BigDecimal qty;

    /** 限量退货时可退数量 */
    @ApiModelProperty(name = "returnDeadline", value = "退货截止时间")
    private LocalDateTime returnDeadline;

}
