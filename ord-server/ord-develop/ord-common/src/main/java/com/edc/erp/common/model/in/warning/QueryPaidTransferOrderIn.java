package com.edc.erp.common.model.in.warning;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-05-06 18:37
 */
@Data
public class QueryPaidTransferOrderIn extends BaseEntity {

    private String truncationDateTimeBegin;
    private String truncationDateTimeEnd;
    private String bizOrgCode;
    private List<String> orderStatusCodeList;
    private List<Long> orderTypeConfigIdList;
}
