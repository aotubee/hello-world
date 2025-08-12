package com.edc.erp.presale.model.out;

import com.edc.erp.presale.entity.OrdDisPresaleActivity;
import com.edc.erp.presale.entity.OrdDisPresaleOrder;
import com.edc.erp.presale.entity.OrdDisPresaleOrderDetail;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName CreatePresaleOrderDataOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/29 18:55
 **/
@Data
public class CreatePresaleOrderDataOut implements Serializable {
    private static final long serialVersionUID = 6907772707836857250L;

    private OrdDisPresaleOrder ordDisPresaleOrder;

    private List<OrdDisPresaleOrderDetail> orderDetailList;

}
