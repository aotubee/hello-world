package com.edc.erp.common.service;

import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.model.in.datafile.OrdDeliveryDataFileIn;
import com.edc.erp.common.model.out.datafile.OrdDeliveryDataFileOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.BaseService;

public interface OrderDeliveryDataFileService extends BaseService<OrdDeliveryDataFile> {

    Page<OrdDeliveryDataFileOut> findByPage(OrdDeliveryDataFileIn ordDeliveryDataFileIn);

    void save(OrdDeliveryDataFile ordDeliveryDataFile);

    void over(OrdDeliveryDataFile ordDeliveryDataFile);

    String download(Integer dateFileId);
}
