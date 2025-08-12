package com.edc.erp.distribution.model.out;

import com.edc.erp.distribution.entity.OrdDisOrderTrack;
import lombok.Data;

/**
 * 订单追踪出参
 * @author wei
 */
@Data
public class OrdDisOrderTrackOut extends OrdDisOrderTrack {

    private String createTimeStr;
}
