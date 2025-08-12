package com.edc.erp.distribution.model.in;

import com.edc.erp.common.model.out.OrderProcessOut;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName CreateOrderDataIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/8/26 9:41
 **/
@Data
public class DataForCreateDisOrderIn implements Serializable {


    private OrdDisOrderCycle ordDisOrderCycle;

    private OrdDisOrder ordDisOrder;

    private List<OrdDisOrderDetail> ordDisOrderDetailList;

    private List<OrderProcessOut> orderProcessOutList;

}
