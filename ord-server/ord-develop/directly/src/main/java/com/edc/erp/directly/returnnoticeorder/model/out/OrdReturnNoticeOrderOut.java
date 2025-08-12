package com.edc.erp.directly.returnnoticeorder.model.out;


import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNotice;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 退货通知单返回类
 *
 * @author yaojinpeng
 * @since 2022/10/21 12:24
 */

@Data
public class OrdReturnNoticeOrderOut extends OrdDirReturnNotice implements Serializable {

    /**
     * 退货类型中文值
     */
    @ApiModelProperty(name = "returnTypeValue", value = "退货类型中文值")
    private String returnTypeValue;

    /**
     * 退货原因中文值
     */
    @ApiModelProperty(name = "returnWhyValue", value = "退货原因中文值")
    private String returnWhyValue;

    /**
     * 退货状态中文值
     */
    @ApiModelProperty(name = "statusStr", value = "退货状态中文值")
    private String statusStr;

    /**
     * 退货通知状态中文
     */
    @ApiModelProperty(name = "returnNoticeStatusValue", value = "退货通知状态中文(App用)")
    private String returnNoticeStatusValue;

    /**
     * 是否已退
     */
    @ApiModelProperty(name = "isReturn", value = "是否已退")
    private Integer isReturn;

}
