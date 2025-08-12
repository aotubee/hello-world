package com.edc.erp.disfirstorder.model.out;

import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDetail;
import com.edc.erp.disfirstorder.model.excel.OrdDisFirstOrderImportErrorResult;
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
public class DisFirstOrderCheckOut implements Serializable {

    private static final long serialVersionUID = -7697263165828572650L;
    private Long firstOrderId;

    private List<OrdDisOrderFirstDetail> saveDetailList;

    private List<OrdDisOrderFirstDetail> updateDetailList;

    private List<OrdDisOrderFirstDetail> deleteDetailList;

    private OrdDisOrderFirst updateOrdDirOrderFirst;

    private List<OrdDisFirstOrderImportErrorResult> errorResultList;
}
