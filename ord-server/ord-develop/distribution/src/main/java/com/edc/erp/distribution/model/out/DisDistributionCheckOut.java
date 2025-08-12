package com.edc.erp.distribution.model.out;

import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionDetail;
import com.edc.erp.distribution.model.excel.OrdDisDistributionImportErrorResult;
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
public class DisDistributionCheckOut implements Serializable {

    private Long distributionOrderId;

    private List<String> storeCodeList;

    private List<OrdDisOrderDistributionDetail> saveDetailList;

    private OrdDisOrderDistribution updateDirOrderDistribution;

    private List<OrdDisDistributionImportErrorResult> errorResultList;

    private List<OrdDisOrderDistributionDetail> updateDetailList;

    private List<OrdDisOrderDistributionDetail> deleteDetailList;
}
