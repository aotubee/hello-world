package com.edc.erp.common.model.in.fund;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName UnFrozenIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/7/26 15:10
 **/
@Data
public class UnFrozenIn implements Serializable {
    private static final long serialVersionUID = 396500243601110961L;

    private List<String> unFrozenBusinessNos;
}
