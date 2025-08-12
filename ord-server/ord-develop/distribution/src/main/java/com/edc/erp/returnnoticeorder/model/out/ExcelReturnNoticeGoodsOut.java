package com.edc.erp.returnnoticeorder.model.out;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.edc.erp.common.model.out.goods.StandardSpecTransInfoOut;
import lombok.Data;

/**
 * 退货通知单商品明细导出
 *
 * @author yaojinpeng
 * @since 2022/10/29 14:51
 */

@Data
public class ExcelReturnNoticeGoodsOut {

    /**
     * 序号
     */
    @Excel(name = "序号")
    private Integer index;

    /**
     * 商品代码
     */
    @Excel(name = "商品代码", orderNum = "1", width = 15)
    private String goodsCode;

    /**
     * 商品名称
     */
    @Excel(name = "商品名称",orderNum = "2", width = 25)
    private String goodsName;


    /**
     * 配货规格
     */
    @Excel(name = "配货规格", orderNum = "5", width = 15)
    private String specification;

    /**
     * 商品小分类
     */
    @Excel(name = "商品小分类", orderNum = "6", width = 15)
    private String sortName;

}
