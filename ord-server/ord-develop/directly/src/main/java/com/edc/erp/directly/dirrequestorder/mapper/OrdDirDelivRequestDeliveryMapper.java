package com.edc.erp.directly.dirrequestorder.mapper;

import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequestDelivery;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 要货单与配货单关联表(OrdDirDelivRequestDelivery)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
@Repository
public interface OrdDirDelivRequestDeliveryMapper extends BaseMapper<OrdDirDelivRequestDelivery> {

    void batchSaveDirDeliveryRequestOrderList(@Param("ordDirDelivRequestDeliveryList") List<OrdDirDelivRequestDelivery> ordDirDelivRequestDeliveryList);

}
