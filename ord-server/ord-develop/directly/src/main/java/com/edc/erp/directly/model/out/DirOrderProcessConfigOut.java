package com.edc.erp.directly.model.out;

import com.edc.erp.directly.entity.DirOrderProcessConfig;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import lombok.Data;

import java.util.List;

/**
 * @author fxw
 * @description: 订单流程配置出参
 * @since 2022/10/18 16:49
 */
@Data
public class DirOrderProcessConfigOut extends DirOrderProcessConfig {

    private List<DirOrderProcessConfigItem> configItemList;
}
