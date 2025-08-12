package com.edc.erp.directly.dirdeliveryorder.model.in;


import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import lombok.Data;

/**
 * @return: 配货详情导入
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
public class ImportDeliveryIn extends OrdDirDelivery {

    String fileId;
}
