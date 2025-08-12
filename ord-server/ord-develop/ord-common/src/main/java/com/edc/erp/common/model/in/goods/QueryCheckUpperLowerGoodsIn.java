package com.edc.erp.common.model.in.goods;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName QueryCheckUpperLowerGoods
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/6/18 15:45
 **/
@Data
public class QueryCheckUpperLowerGoodsIn implements Serializable {
    private static final long serialVersionUID = 3538060052868379259L;

    private String bizOrgCode;

    private List<String> goodsCodeList;
}
