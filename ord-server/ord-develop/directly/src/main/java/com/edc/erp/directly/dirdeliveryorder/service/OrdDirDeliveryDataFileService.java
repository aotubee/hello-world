package com.edc.erp.directly.dirdeliveryorder.service;

import com.edc.erp.common.entity.OrdDeliveryDataFile;

public interface OrdDirDeliveryDataFileService {

    boolean execFile(OrdDeliveryDataFile ordDeliveryDataFile);

    void overDataAFile(OrdDeliveryDataFile ordDeliveryDataFile);
}
