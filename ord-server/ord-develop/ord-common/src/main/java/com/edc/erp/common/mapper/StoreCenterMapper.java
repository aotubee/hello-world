package com.edc.erp.common.mapper;

import com.edc.erp.common.model.entity.ClientInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author
 * @since 2022-08-30 17:20:59
 */
@Repository
public interface StoreCenterMapper {


    /**
     * 根据门店区域code查名称
     * @param storeArea
     * @param bizOrgCode
     * @return
     */
    String getNameByCode(@Param("storeArea") String storeArea,@Param("bizOrgCode") String bizOrgCode);

    /**
     * 是否独立核算
     * @param clientCode
     * @param bizOrgCode
     * @return
     */
    Integer getIsIndependentAccountingByCode(@Param("clientCode") String clientCode, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 根据门店类型和组织查询已启用的APP用户的门店代码集合
     * @param storeProperty
     * @param bizOrgCode
     * @return
     */
    List<String> findEnableAppStoreCodeListByPropertyAndOrg(@Param("storeProperty") String storeProperty, @Param("bizOrgCode") String bizOrgCode);

    Integer getIsCanEditPrice(@Param("clientCode") String clientCode, @Param("bizOrgCode") String bizOrgCode);

    ClientInfo getClientInfoByCode(@Param("clientCode") String clientCode, @Param("bizOrgCode") String bizOrgCode);
}
