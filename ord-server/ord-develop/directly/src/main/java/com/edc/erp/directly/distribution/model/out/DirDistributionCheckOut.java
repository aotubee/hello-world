package com.edc.erp.directly.distribution.model.out;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionDetail;
import com.edc.erp.directly.distribution.model.excel.OrdDirDistributionImportErrorResult;
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
public class DirDistributionCheckOut implements Serializable {

    private static final long serialVersionUID = -6302754083921338269L;
    private Long distributionOrderId;

    private List<String> storeCodeList;

    private List<OrdDirOrderDistributionDetail> saveDetailList;

    private List<OrdDirOrderDistributionDetail> updateDetailList;

    private List<OrdDirOrderDistributionDetail> deleteDetailList;

    private OrdDirOrderDistribution updateDirOrderDistribution;

    private List<OrdDirDistributionImportErrorResult> errorResultList;
}
