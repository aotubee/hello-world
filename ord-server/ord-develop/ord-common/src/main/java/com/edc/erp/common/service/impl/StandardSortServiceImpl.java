package com.edc.erp.common.service.impl;

import com.edc.erp.common.mapper.StandardSortMapper;
import com.edc.erp.common.service.StandardSortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 标准商品品类实现类
 *
 * @author wanglidong
 * @since 2022/11/23 17:54
 */
@Service
public class StandardSortServiceImpl implements StandardSortService {

    @Autowired
    private StandardSortMapper standardSortMapper;

    /**
     * 根据商品品类代码查询品类名称
     *
     * @param smallSort
     * @return
     */
    @Override
    public String selectSortNameByCode(String smallSort) {
        return standardSortMapper.selectSortNameByCode(smallSort);
    }
}
