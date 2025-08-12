package com.edc.erp.directly.dirfirstorder.mapper;

import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirOrderFirstIn;
import com.edc.erp.directly.dirfirstorder.model.out.FirstOrderConfigOut;
import com.edc.erp.directly.dirfirstorder.model.out.FirstOrderSortOut;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDerDirOrderFirstOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 铺货单(OrdDirOrderFirst)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-10 14:09:10
 */
@Repository
public interface OrdDirOrderFirstMapper extends BaseMapper<OrdDirOrderFirst> {
    /**
     * 获取首单铺货订单列表
     * @param ordDirOrderFirstIn
     * @return
     */
    List<OrdDerDirOrderFirstOut> findFirstOrderByPage(OrdDirOrderFirstIn ordDirOrderFirstIn);

    /**
     * 获取铺货单拆单配置
     * @param bizOrgCode
     * @return
     */
    FirstOrderConfigOut getOrderFirstConfig(@Param("bizOrgCode") String bizOrgCode);

    /**
     * 询铺货配置品类分类
     * @param id
     * @return
     */
    List<FirstOrderSortOut> findOrderFirstConfigSort(@Param("id") Integer id);

    /**
     * 定时器根据条件查询已审核铺货单
     * @param ordDirOrderFirst
     * @return
     */
    List<OrdDirOrderFirst> selectByEffectiveTime(OrdDirOrderFirst ordDirOrderFirst);
}
