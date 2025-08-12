package com.edc.erp.common.mapper;

import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.model.in.datafile.OrdDeliveryDataFileIn;
import com.edc.erp.common.model.out.datafile.OrdDeliveryDataFileOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配销/配货上传文件数据
 *
 * @author tangxiaoliang
 * @since 2023-03-13
 */
@Repository
public interface OrderDeliveryDataFileMapper extends BaseMapper<OrdDeliveryDataFile> {

    List<OrdDeliveryDataFileOut> findByPage(OrdDeliveryDataFileIn ordDeliveryDataFileIn);
}
