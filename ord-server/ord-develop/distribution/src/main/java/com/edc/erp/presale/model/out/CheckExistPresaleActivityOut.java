package com.edc.erp.presale.model.out;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @ClassName CheckExistPreSaleActivityOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/28 18:56
 **/
@Data
public class CheckExistPresaleActivityOut implements Serializable {
    private static final long serialVersionUID = -6491271176165305013L;

    private String storeCode;

    private String goodsCode;

    private String presaleActivityNo;

    private Long id;

    private LocalDateTime beginOrderDate;

    private LocalDateTime endOrderDate;
}
