package com.edc.erp.common.mapper;


import com.edc.erp.common.model.out.goods.GoodsSortOut;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgSortMapper {


    List<GoodsSortOut> findSortListByBizOrgCode(@Param("bizOrgCode") String bizOrgCode);
}
