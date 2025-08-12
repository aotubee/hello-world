package com.edc.erp.directly.returnnoticeorder.mapper;

import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNoticeGoods;
import com.edc.erp.directly.returnnoticeorder.model.in.ReturnNoticeGoodsIn;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 退货通知单与商品表(OrdDirReturnNoticeGoods)表数据库访问层
 *
 * @author
 * @since 2022-11-18 19:09:03
 */
@Repository
public interface OrdDirReturnNoticeGoodsMapper extends BaseMapper<OrdDirReturnNoticeGoods> {

    /**
     * 分页查商品信息
     * @param returnNoticeGoodsIn
     * @return
     */
    List<OrdReturnNoticeGoodsOut> findGoodsOutByPage(ReturnNoticeGoodsIn returnNoticeGoodsIn);
}
