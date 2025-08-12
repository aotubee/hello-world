package com.edc.erp.directly.dirrequestorder.service.impl;

import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.out.goods.OrgSortOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequestDelivery;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequestDetail;
import com.edc.erp.directly.dirrequestorder.mapper.OrdDirDelivRequestDetailMapper;
import com.edc.erp.directly.dirrequestorder.model.in.DirRequestOrderDtlPageIn;
import com.edc.erp.directly.dirrequestorder.model.out.DirRequestOrderDetailOut;
import com.edc.erp.directly.dirrequestorder.model.out.ExcelRequestOrderDtlOut;
import com.edc.erp.directly.dirrequestorder.model.out.RequestSummarizingOut;
import com.edc.erp.directly.dirrequestorder.service.OrdDirDelivRequestDetailService;
import com.edc.erp.directly.util.FileExportUtil;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 要货单明细表(OrdDirDelivRequestDetail)表服务实现类
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
@Service
@RequiredArgsConstructor
public class OrdDirDelivRequestDetailServiceImpl extends BaseServiceImpl<OrdDirDelivRequestDetail> implements OrdDirDelivRequestDetailService {

    private final OrdDirDelivRequestDetailMapper ordDirDelivRequestDetailMapper;

    private final OrderGoodsServer orderGoodsServer;

    private final FileService fileService;

    private final StockServer stockServer;

    /**
     * 根据要货单ID查询要货单商品数量总和
     *
     * @param requestOrderId
     * @return
     */
    @Override
    public BigDecimal getSkuNumberById(Long requestOrderId) {
        return ordDirDelivRequestDetailMapper.getSkuNumberById(requestOrderId);
    }

    /**
     * 根据要货单ID查询要货单商品品项数
     *
     * @param requestOrderId
     * @return
     */
    @Override
    public Integer getGoodsItemNumber(Long requestOrderId) {
        return ordDirDelivRequestDetailMapper.getGoodsItemNumber(requestOrderId);
    }

    /**
     * 查询要货单明细
     *
     * @param dtlPageIn
     * @return
     */
    @Override
    public Page<DirRequestOrderDetailOut> findRequestOrderDetailList(DirRequestOrderDtlPageIn dtlPageIn) {
        List<DirRequestOrderDetailOut> detailList = ordDirDelivRequestDetailMapper.findRequestOrderDetailListByPage(dtlPageIn);
        detailList.forEach(item -> {
            StockInfoOut stockInfoOut = stockServer.getTransInfo(item.getStockCode());
            //仓位
            item.setStockName(Objects.isNull(stockInfoOut) ? "" : stockInfoOut.getStockName());
            //要货单价
//               BigDecimal realUnitPrice = ordDirDelivRequestDetailMapper.getRealUnitPrice(item.getRequestOrderId());
            if (item.getIsGift().equals(NumberUtil.INTEGER_ZERO)) {
                item.setPrice(item.getRequestOrderAmount().divide(item.getQuantity(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            } else {
                item.setPrice(BigDecimal.ZERO);
            }
//               item.setPrice(realUnitPrice);
            //品类名称
            OrgSortOut orgSortOut = orderGoodsServer.getByCode(item.getSmallSort(), dtlPageIn.getBizOrgCode());
            // 商品属性
            item.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(item.getGoodsType()));
            item.setSortName(Objects.nonNull(orgSortOut) ? orgSortOut.getSortName() : "");
            item.setDistributionTypeValue(DistributionWaysEnum.getNameByType(item.getDistributionType()));
        });
        Page<DirRequestOrderDetailOut> resPage = new Page<>(dtlPageIn);
        resPage.setList(detailList);
        return resPage;
    }

    /**
     * 导出要货单明细
     *
     * @param dtlPageIn
     * @return
     */
    @Override
    public String exportDetailList(DirRequestOrderDtlPageIn dtlPageIn) {
        Page<DirRequestOrderDetailOut> detailOutPage = this.findRequestOrderDetailList(dtlPageIn);
        List<ExcelRequestOrderDtlOut> excelRequestOrderDtlOuts = parseDtlDataToExcel(detailOutPage.getList());
        String title = "要货单明细信息";
        byte[] bytes = FileExportUtil.getFileBytesByData(excelRequestOrderDtlOuts,
                title, title, ExcelRequestOrderDtlOut.class, true);
        return fileService.uploadFile(title + ".xlsx", bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    /**
     * 根据要货单id查询要货单明细
     *
     * @param requestOrderId
     */
    @Override
    public List<OrdDirDelivRequestDetail> findByRequestOrderIdAndBizOrgCode(Long requestOrderId) {
        OrdDirDelivRequestDetail ordDirDelivRequestDetail = new OrdDirDelivRequestDetail();
        ordDirDelivRequestDetail.setRequestOrderId(requestOrderId);
        ordDirDelivRequestDetail.setIsDelete(ModelConst.DELETE.NO);
        List<OrdDirDelivRequestDetail> detailList = ordDirDelivRequestDetailMapper.select(ordDirDelivRequestDetail);
        return detailList;
    }

    /**
     * 批量新增要货单详情表
     *
     * @param requestOrderDetailList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<OrdDirDelivRequestDetail> requestOrderDetailList) {
        ordDirDelivRequestDetailMapper.batchSave(requestOrderDetailList);
    }

    /**
     * 解析导出需要明细数据
     *
     * @param list
     * @return
     */
    private List<ExcelRequestOrderDtlOut> parseDtlDataToExcel(List<DirRequestOrderDetailOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertDtlExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 转化导出明细数据
     *
     * @param dirRequestOrderDetailOut
     * @param index
     * @return
     */
    private ExcelRequestOrderDtlOut convertDtlExcel(DirRequestOrderDetailOut dirRequestOrderDetailOut, int index) {
        ExcelRequestOrderDtlOut excelRequestOrderDtlOut = new ExcelRequestOrderDtlOut();
        BeanUtils.copy(dirRequestOrderDetailOut, excelRequestOrderDtlOut);
        excelRequestOrderDtlOut.setIndex(index + 1);
        excelRequestOrderDtlOut.setSortName(dirRequestOrderDetailOut.getSortName() + "(" + dirRequestOrderDetailOut.getSmallSort() + ")");
        excelRequestOrderDtlOut.setAllowDistributionReturn(NumberUtil.INTEGER_ZERO.equals(dirRequestOrderDetailOut.getAllowDistributionReturn()) ? "否" : "是");
        excelRequestOrderDtlOut.setDistributionType(DistributionWaysEnum.getNameByType(dirRequestOrderDetailOut.getDistributionType()));
        excelRequestOrderDtlOut.setGoodsType(GoodsTypeEnum.getNameByCode(dirRequestOrderDetailOut.getGoodsType()));
        excelRequestOrderDtlOut.setStock(dirRequestOrderDetailOut.getStockName() + "【" + dirRequestOrderDetailOut.getStockCode() + "】");
        return excelRequestOrderDtlOut;
    }
}
