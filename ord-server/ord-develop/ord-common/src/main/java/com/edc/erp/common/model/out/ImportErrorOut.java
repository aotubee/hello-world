package com.edc.erp.common.model.out;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName ImportErrorOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/6/21 10:50
 **/
@Data
public class ImportErrorOut implements Serializable {
    private static final long serialVersionUID = 2458021037085861676L;

    private Integer sortNumber;

    private String rowIndex;

    private String errorMessage;
}
