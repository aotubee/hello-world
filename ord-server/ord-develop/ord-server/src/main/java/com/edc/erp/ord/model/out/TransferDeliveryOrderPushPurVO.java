package com.edc.erp.ord.model.out;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName TransferDeliveryOrderPushPurVO
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/2/8 15:09
 **/
@Data
public class TransferDeliveryOrderPushPurVO implements Serializable {
    private static final long serialVersionUID = 5357348797889299296L;

    private String bizOrgCode;

    private String executeTime;
}
