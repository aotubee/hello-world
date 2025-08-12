package com.edc.erp.common.model.in.zk;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName ZkBussinessOrderPushIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/6/22 15:44
 **/
@Data
public class ZkBussinessOrderPushIn implements Serializable {

    @ApiModelProperty(name = "idList", required = true, value = "单据id集合")
    private List<Integer> idList;

    @ApiModelProperty(name = "type", required = true, value = "业务类型")
    private String type;
}
