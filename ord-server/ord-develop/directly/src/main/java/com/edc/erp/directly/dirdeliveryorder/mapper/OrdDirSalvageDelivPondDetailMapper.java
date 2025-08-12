package com.edc.erp.directly.dirdeliveryorder.mapper;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirSalvageDelivPondDetail;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirSalvageDelivPondDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdDirSalvageDelivPondDetailMapper extends BaseMapper<OrdDirSalvageDelivPondDetail> {

    List<OrdDirSalvageDelivPondDetailOut> findByDeliveryOrderIdList(@Param("deliveryOrderIdList") List<Long> deliveryOrderIdList);

    List<Long> findDeliveryOrderIdListBySalvagePondId(@Param("salvagePondId") Long salvagePondId);
}
