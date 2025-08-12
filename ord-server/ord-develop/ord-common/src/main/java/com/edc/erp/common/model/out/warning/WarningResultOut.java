package com.edc.erp.common.model.out.warning;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-01-27 11:05
 */
@Data
public class WarningResultOut extends BaseEntity {

    private Boolean checkFlag;

    private String errorMessage;
}

