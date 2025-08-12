package com.edc.erp.disdeliveryorder.model.in;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import lombok.Data;

/**
 * 配销详情导入
 * @author
 */
@Data
public class ImportDeliveryIn extends OrdDisDelivery {

    String fileId;
}
