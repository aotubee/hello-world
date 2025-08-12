package com.edc.erp.common.service;

/**
 * 标准商品品类接口
 *
 * @author wanglidong
 * @since 2022/11/23 17:55
 */
public interface StandardSortService {

    /**
     * 根据商品品类代码查询品类名称
     * @param smallSort
     * @return
     */
    String selectSortNameByCode(String smallSort);
}
