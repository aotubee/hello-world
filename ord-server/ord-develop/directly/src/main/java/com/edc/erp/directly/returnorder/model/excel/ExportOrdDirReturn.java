package com.edc.erp.directly.returnorder.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.edc.erp.common.util.BigDecimalAmountConverter;
import com.edc.erp.common.util.BigDecimalConverter;
import com.edc.erp.common.util.LocalDateTimeConverter;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description: 配货单导出
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
public class ExportOrdDirReturn  implements Serializable {
    private static final long serialVersionUID = 1739820287228268911L;
    /**
     *
     */
    @ExcelProperty(value = "序号",index = 0)
    private Integer index;
    /**
     * 退货单号
     */
    @ExcelProperty(value = "退货单号",index = 1)
    private String returnOrderNo;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态",index = 2)
    private String returnStatus;

    /**
     * 冲销标识
     */
    @ExcelProperty(value = "冲销标识",index = 3)
    private String isReversal;

    /**
     * 是否红冲单
     */
    @ExcelProperty(value = "是否冲销单",index = 4)
    private String isReversalOrder;

    /**
     * 退货类型
     */
    @ExcelProperty(value = "退货类型",index = 5)
    private String returnType;

    /**
     * 退货原因
     *
     */
    @ExcelProperty(value = "退货原因",index = 6)
    private String returnOrderReasonValue;

    /**
     * 门店代码
     */
    @ExcelProperty(value = "门店代码",index = 7)
    private String storeCode;

    /**
     * 门店名称
     */
    @ExcelProperty(value = "门店名称",index = 8)
    private String storeName;

    /**
     * 门店区域
     */
    @ExcelProperty(value = "门店区域",index = 9)
    private String storeArea;

    /**
     * 仓储信息
     */
    @ExcelProperty(value = "仓储信息",index = 10)
    private String wrh;

    /**
     * 仓位信息
     */
    @ExcelProperty(value = "仓位信息",index = 11)
    private String stock;

    /**
     * 商品品项数
     */
    @ExcelProperty(value = "商品品项数",index = 12)
    private Integer skuCount;

    /**
     * 关联通知单
     */
    @ExcelProperty(value = "关联通知单",index = 13)
    private String returnNoticeNo;

    /**
     * 申请退货包装数
     */
    @ExcelProperty(value = "申请退货数量",index = 14,converter = BigDecimalConverter.class)
    private BigDecimal applyReturnQuantity;

    /**
     * 申请退货金额
     */
    @ExcelProperty(value = "申请退货金额",index = 15,converter = BigDecimalAmountConverter.class)
    private BigDecimal applyReturnAmount;

    /**
     * 审核退货数量
     */
    @ExcelProperty(value = "审核退货数量",index = 16,converter = BigDecimalConverter.class)
    private BigDecimal auditReturnQuantity;

    /**
     * 审核退货金额
     */
    @ExcelProperty(value = "审核退货金额",index = 17,converter = BigDecimalAmountConverter.class)
    private BigDecimal auditReturnAmount;

    /**
     * 实际退货数量
     */
    @ExcelProperty(value = "退货数量",index = 18,converter = BigDecimalConverter.class)
    private BigDecimal actualReturnQuantity;


    /**
     * 实际退货金额
     */
    @ExcelProperty(value = "退货金额",index = 19,converter = BigDecimalAmountConverter.class)
    private BigDecimal actualReturnAmount;

    /**
     * 物流单号
     *
     */
    @ExcelProperty(value = "物流单号",index = 20)
    private String logisticsNo;

    /**
     * 来源单号
     */
    @ExcelProperty(value = "来源单号",index = 21)
    private String sourceReturnNo;

    /**
     * 来源单号
     */
    @ExcelProperty(value = "海鼎单号",index = 22)
    private String hdReturnNo;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人",index = 23)
    private String creator;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间",index = 24,converter = LocalDateTimeConverter.class)
    private LocalDateTime createTime;

    /**
     * 收货时间
     */
    @ExcelProperty(value = "收货时间",index = 25,converter = LocalDateTimeConverter.class)
    private LocalDateTime receiveTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注",index = 26)
    private String remark;
}

