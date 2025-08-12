package com.edc.erp.common.service;

import com.edc.erp.common.model.out.InvBizRsnTransOut;
import com.edc.erp.common.model.out.equipment.EquipmentBusinessReasonOut;

import java.util.List;

/**
 * @Description: 库存业务原因
 * @Author: fxw
 * @Date: 2022/11/23
 */
public interface EquipmentBusinessReasonServer {

    InvBizRsnTransOut getWarehouseBizRsnTransByCode(String bizRsnCode, String bizOrgCode, Integer isDelete);

    /**
     * 根据code查询库存业务原因
     *
     * @param bizRsnCode
     * @param bizOrgCode
     * @param isDelete
     * @return
     */
    InvBizRsnTransOut getStoreInvBizRsnTransByCode(String bizRsnCode,
                                                   String bizOrgCode, Integer isDelete);


    /**
     *
     * @param bizOrgCode
     * @param businessReasonType
     * @param businessReasonName
     * @return
     */
    List<EquipmentBusinessReasonOut> page(String bizOrgCode, String businessReasonType, String businessReasonName, String businessReasonDimension);

    String getDefaultReasonCodeByName(String bizOrgCode, String businessReasonType, String businessReasonName, String businessReasonDimension);
}
