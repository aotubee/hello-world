package com.edc.erp.common.model.in.store;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName QueryBizOrgCodeStoreIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/4 10:36
 **/
@Data
public class QueryBizOrgCodeStoreIn implements Serializable {
    private static final long serialVersionUID = 4915193765978420977L;

    private String bizOrgCode;

    private List<String> storeCodeList;
}
