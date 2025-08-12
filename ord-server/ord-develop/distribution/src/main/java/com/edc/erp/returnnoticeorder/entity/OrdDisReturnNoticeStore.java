package com.edc.erp.returnnoticeorder.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


/**
 * 配销退货通知单与门店表(DisReturnNoticeStore)实体类
 *
 * @author yaojinpeng
 * @since 2022-10-28 11:16:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_return_notice_store")
@ApiModel(value = "DisReturnNoticeStore", description = "配销退货通知单与门店表")
public class OrdDisReturnNoticeStore implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 配销退货通知单主键
     */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "配销退货通知单主键")
    private Integer returnNoticeOrderId;

    /**
     * 配销退货通知单商品表id
     */
    @ApiModelProperty(name = "returnNoticeGoodsId", value = "配销退货通知单商品表id")
    private Integer returnNoticeGoodsId;

    /**
     * 退货截止时间
     */
    @ApiModelProperty(name = "returnDeadline", value = "退货截止时间")
    private LocalDateTime returnDeadline;

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
     * 限量退货时可退数量
     */
    @ApiModelProperty(name = "qty", value = "限量退货时可退数量")
    private BigDecimal qty;

    /**
     * 加盟商
     */
    @ApiModelProperty(name = "client", value = "加盟商")
    private String client;

}
