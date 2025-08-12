package com.edc.erp.directly.distribution.model.out;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName OrdDirDistributionImportResultOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/8/31 14:05
 **/
@Data
public class OrdDirDistributionImportResultOut implements Serializable {
    private static final long serialVersionUID = -631239892932946494L;

    private String storeCode;

    private List<OrdDirDistributionImportGoodsOut> importGoodsOutList;
}
