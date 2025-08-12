package com.edc.erp.returnnoticeorder.model.in;

import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNoticeGoods;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核退货通知单入参
 *
 * @author yaojinpeng
 * @since 2022/10/30 17:18
 */
@Data
public class AuditOrdDisReturnNotice {

    /**
     * 退货通知单主键
     */
    private Integer id;

    /**
     * 是否立即生效
     */
    @ApiModelProperty(name = "isEffectiveImmediately", value = "是否即时生效", required = true, example = "1:立即生效;0:定时生效")
    private Integer isEffectiveImmediately;

    /**
     * 生效时间
     */
    private LocalDateTime takeEffectTime;

    /**
     * 商品明细集合
     */
    private List<OrdDisReturnNoticeGoods> ordDisReturnNoticeGoodsList;
}
