package com.edc.erp.common.model.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName AsyncDeliveryImportInfoIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/3/4 17:17
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsyncDeliveryImportInfoIn implements Serializable {

    private static final long serialVersionUID = -8739175638551068421L;
    private List<String> storeCodeList;

    private String bizOrgCode;

}
