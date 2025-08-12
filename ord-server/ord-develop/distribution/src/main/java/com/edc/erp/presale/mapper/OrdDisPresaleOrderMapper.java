package com.edc.erp.presale.mapper;

import com.edc.erp.model.in.QueryStorePresaleOrderForAppIn;
import com.edc.erp.model.in.QueryStorePresaleOrderIn;
import com.edc.erp.model.out.DisStorePresaleOrderInfoOut;
import com.edc.erp.model.out.DisStorePresaleOrderPageForAppOut;
import com.edc.erp.presale.entity.OrdDisPresaleOrder;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdDisPresaleOrderMapper extends BaseMapper<OrdDisPresaleOrder> {

    List<DisStorePresaleOrderPageForAppOut> findStorePresaleOrderForAppByPage(QueryStorePresaleOrderForAppIn queryStorePresaleOrderForAppIn);

    List<DisStorePresaleOrderInfoOut> findStorePresaleOrderByPage(QueryStorePresaleOrderIn queryStorePresaleOrderIn);
}
