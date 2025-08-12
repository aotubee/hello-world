package com.edc.erp.presale.mapper;

import com.edc.erp.presale.entity.OrdDisPresaleAdjustOrderDetail;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdDisPresaleAdjustOrderDetailMapper extends BaseMapper<OrdDisPresaleAdjustOrderDetail> {
    /**
     * 批量插入
     *
     * @param list
     */
    void batchInsert(List<OrdDisPresaleAdjustOrderDetail> list);
}
