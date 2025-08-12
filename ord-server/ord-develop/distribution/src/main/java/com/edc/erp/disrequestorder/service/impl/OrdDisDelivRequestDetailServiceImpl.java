package com.edc.erp.disrequestorder.service.impl;

import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.out.goods.OrgSortOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequestDetail;
import com.edc.erp.disrequestorder.mapper.OrdDisDelivRequestDetailMapper;
import com.edc.erp.disrequestorder.model.in.BackQueryRequestOrderDtlPageIn;
import com.edc.erp.disrequestorder.model.out.BackRequestOrderDetailOut;
import com.edc.erp.disrequestorder.model.out.DisRequestSummarizingOut;
import com.edc.erp.disrequestorder.model.out.ExcelRequestOrderDtlOut;
import com.edc.erp.disrequestorder.service.OrdDisDelivRequestDetailService;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 集货单明细表(OrdDisDelivRequestDetail)表服务实现类
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
@Slf4j
@Service
public class OrdDisDelivRequestDetailServiceImpl extends BaseServiceImpl<OrdDisDelivRequestDetail> implements OrdDisDelivRequestDetailService {

    @Autowired
    private OrdDisDelivRequestDetailMapper ordDisDelivRequestDetailMapper;
    @Autowired
    private OrderGoodsServer orderGoodsServer;
    @Autowired
    private FileService fileService;
    @Autowired
    private DisOrderHandle orderHandle;
    @Autowired
    private StockServer stockServer;

    @Override
    public BigDecimal getSkuNumberById(Long requestOrderId) {
        return ordDisDelivRequestDetailMapper.getSkuNumberById(requestOrderId);
    }

    @Override
    public Integer getGoodsItemNumber(Long requestOrderId) {
        return ordDisDelivRequestDetailMapper.getGoodsItemNumber(requestOrderId);
    }

    @Override
    public Page<BackRequestOrderDetailOut> findRequestOrderDetailList(BackQueryRequestOrderDtlPageIn dtlPageIn) {
        List<BackRequestOrderDetailOut> detailList = ordDisDelivRequestDetailMapper.findRequestOrderDetailListByPage(dtlPageIn);
        detailList.forEach(item -> {
            //仓位
            StockInfoOut stockInfoOut = stockServer.getTransInfo(item.getStockCode());
            item.setPositionName(Objects.isNull(stockInfoOut) ? "" : stockInfoOut.getStockName());
            //集货单价
            if (item.getIsGift().equals(NumberUtil.INTEGER_ZERO)) {
                item.setPrice(item.getRequestOrderAmount().divide(item.getQuantity(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            } else {
                item.setPrice(BigDecimal.ZERO);
            }
            //品类名称
            OrgSortOut orgSortOut = orderGoodsServer.getByCode(item.getSmallSort(), dtlPageIn.getBizOrgCode());
            item.setSortName(Objects.nonNull(orgSortOut) ? orgSortOut.getSortName() : "");
            item.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(item.getGoodsType()));
            item.setDistributionTypeValue(DistributionWaysEnum.getNameByType(item.getDistributionType()));
        });
        Page<BackRequestOrderDetailOut> resPage = new Page<>(dtlPageIn);
        resPage.setList(detailList);
        return resPage;
    }

    @Override
    public String exportDetailList(BackQueryRequestOrderDtlPageIn dtlPageIn) {
        Page<BackRequestOrderDetailOut> detailOutPage = this.findRequestOrderDetailList(dtlPageIn);
        List<ExcelRequestOrderDtlOut> excelRequestOrderDtlOuts = parseDtlDataToExcel(detailOutPage.getList());
        String title = "集货单明细信息";
        byte[] bytes = FileExportUtil.getFileBytesByData(excelRequestOrderDtlOuts,
                title, title, ExcelRequestOrderDtlOut.class, true);
        return fileService.uploadFile(title + ".xlsx", bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    /**
     * 批量新增集货单详情表
     *
     * @param requestOrderDetailList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<OrdDisDelivRequestDetail> requestOrderDetailList) {
        ordDisDelivRequestDetailMapper.batchSave(requestOrderDetailList);
    }

    /**
     * 根据集货单id查询集货单明细信息
     *
     * @param requestOrderId
     * @return
     */
    @Override
    public List<OrdDisDelivRequestDetail> findByRequestOrderIdAndBizOrgCode(Long requestOrderId) {
        OrdDisDelivRequestDetail ordDisDelivRequestDetail = new OrdDisDelivRequestDetail();
        ordDisDelivRequestDetail.setRequestOrderId(requestOrderId);
        ordDisDelivRequestDetail.setIsDelete(ModelConst.DELETE.NO);
        List<OrdDisDelivRequestDetail> detailList = ordDisDelivRequestDetailMapper.select(ordDisDelivRequestDetail);
        return detailList;
    }

    /**
     * 将查询得到的列表集合转换为导出集合
     *
     * @param list
     * @return List<ExcelRequestOrderDtlOut>
     */
    private List<ExcelRequestOrderDtlOut> parseDtlDataToExcel(List<BackRequestOrderDetailOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertDtlExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 将BackRequestOrderDetailOut转换为导出ExcelRequestOrderDtlOut
     *
     * @param backRequestOrderDetailOut
     * @return ExcelRequestOrderDtlOut
     */
    private ExcelRequestOrderDtlOut convertDtlExcel(BackRequestOrderDetailOut backRequestOrderDetailOut, int index) {
        ExcelRequestOrderDtlOut excelRequestOrderDtlOut = new ExcelRequestOrderDtlOut();
        BeanUtils.copy(backRequestOrderDetailOut, excelRequestOrderDtlOut);
        excelRequestOrderDtlOut.setIndex(index + 1);
        excelRequestOrderDtlOut.setSortName(backRequestOrderDetailOut.getSortName() + "(" + backRequestOrderDetailOut.getSmallSort() + ")");
        excelRequestOrderDtlOut.setAllowDistributionReturn(NumberUtil.INTEGER_ZERO.equals(backRequestOrderDetailOut.getAllowDistributionReturn()) ? "否" : "是");
        excelRequestOrderDtlOut.setDistributionType(DistributionWaysEnum.getNameByType(backRequestOrderDetailOut.getDistributionType()));
        excelRequestOrderDtlOut.setGoodsType(GoodsTypeEnum.getNameByCode(backRequestOrderDetailOut.getGoodsType()));
        excelRequestOrderDtlOut.setStock(backRequestOrderDetailOut.getPositionName() + "【" + backRequestOrderDetailOut.getStockCode() + "】");
        return excelRequestOrderDtlOut;
    }
}
