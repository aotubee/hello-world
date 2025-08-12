package com.edc.erp.directly.model.out;

import com.edc.erp.directly.entity.DirOrderProcess;
import lombok.Data;

import java.util.List;

/**
 * @author fxw
 * @description: 订单流程出参
 * @since 2022/10/18 16:46
 */
@Data
public class DirOrderProcessOut extends DirOrderProcess {

    private List<DirOrderProcessConfigOut> configList;
}
