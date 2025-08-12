package com.edc.erp.directly.dirfirstorder.model.out;

import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDetail;
import com.edc.erp.directly.dirfirstorder.model.excel.OrdDirFirstOrderImportErrorResult;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName DirDistributionCheckOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/12 18:06
 **/
@Data
public class DirFirstOrderCheckOut implements Serializable {

    private static final long serialVersionUID = 5487608467979352616L;
    private Long firstOrderId;

    private List<OrdDirOrderFirstDetail> saveDetailList;

    private List<OrdDirOrderFirstDetail> updateDetailList;

    private List<OrdDirOrderFirstDetail> deleteDetailList;

    private OrdDirOrderFirst updateOrdDirOrderFirst;

    private List<OrdDirFirstOrderImportErrorResult> errorResultList;
}
