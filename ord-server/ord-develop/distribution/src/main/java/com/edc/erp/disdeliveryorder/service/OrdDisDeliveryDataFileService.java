package com.edc.erp.disdeliveryorder.service;

import com.edc.erp.common.entity.OrdDeliveryDataFile;

public interface OrdDisDeliveryDataFileService {

    boolean execFile(OrdDeliveryDataFile ordDeliveryDataFile);

    void overDataAFile(OrdDeliveryDataFile ordDeliveryDataFile);
}
