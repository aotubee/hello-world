package com.edc.erp.directly.dirdeliveryorder.service;

import com.edc.erp.directly.dirdeliveryorder.model.in.DirOverallDeliveryAsyncImportIn;
import com.edc.plugins.common.response.Response;

/**
 * @ClassName OrdDisOverallDeliveryService
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/11/22 15:58
 **/
public interface OrdDirOverallDeliveryService {

    Response<String> asyncImportOverallDelivery(DirOverallDeliveryAsyncImportIn dirOverallDeliveryAsyncImportIn, String loginUsername, String bizOrgCode);
}
