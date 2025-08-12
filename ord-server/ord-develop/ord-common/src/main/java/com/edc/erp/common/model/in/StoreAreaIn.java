package com.edc.erp.common.model.in;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * description 门店区域查询入参
 *
 * @author gusiyuan
 * @since 2025/2/7 15:24
 */
@Data
public class StoreAreaIn implements Serializable {

    private static final long serialVersionUID = 5549043225747535514L;

    private List<String> areaCodes;

    private String bizOrgCode;
}
