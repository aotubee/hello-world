package com.edc.erp.common.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 标准商品品类数据层接口
 *
 * @author wanglidong
 * @since 2022/11/23 17:57
 */
@Mapper
public interface StandardSortMapper {

    /**
     * 根据商品品类代码查询品类名称
     * @param smallSort
     * @return
     */
    @Select("select sort_name from gc_standard_sort where sort = #{smallSort,jdbcType=VARCHAR};")
    String selectSortNameByCode(@Param("smallSort") String smallSort);
}
