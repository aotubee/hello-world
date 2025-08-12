package com.edc.erp.presale.model.in;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName UpdatePresaleOrderPaidIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/10 11:22
 **/
@Data
public class UpdatePresaleOrderPaidForMqIn implements Serializable {
    private static final long serialVersionUID = -6439846474113019919L;

    private String loginUsername;

    private Long presaleOrderId;

}
