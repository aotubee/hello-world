package com.edc.erp.disdeliveryorder.mapper;

import com.edc.erp.disdeliveryorder.entity.OrdDisSalvageDelivPondDetail;
import com.edc.erp.disdeliveryorder.model.out.OrdDisSalvageDelivPondDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdDisSalvageDelivPondDetailMapper extends BaseMapper<OrdDisSalvageDelivPondDetail> {

    List<OrdDisSalvageDelivPondDetailOut> findByDeliveryOrderIdList(@Param("deliveryOrderIdList") List<Long> deliveryOrderIdList);

    List<Long> findDeliveryOrderIdListBySalvagePondId(@Param("salvagePondId") Long salvagePondId);
}
