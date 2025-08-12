package com.edc.erp.directly.distribution.model.in;

import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import com.edc.erp.directly.model.out.DirOrderProcessOut;
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
public class DataForCreateDirOrderIn implements Serializable {


    private OrdDirOrderCycle ordDirOrderCycle;

    private OrdDirOrder ordDirOrder;

    private List<OrdDirOrderDetail> ordDirOrderDetailList;

    private List<DirOrderProcessOut> orderProcessOutList;

}
