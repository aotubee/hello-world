package com.edc.erp.common.model.in.warning;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-08-11 15:02
 */
@Data
public class LogisticsMessageQueryIn extends BaseEntity {

    private List<String> storeCodeList;

    private String bizOrgCode;

    private String truncationDateTime;

    private List<String> orderStatusCodeList;

    private String beginTruncationDateTime;

    private String endTruncationDateTime;
}
