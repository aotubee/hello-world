package com.edc.erp.disdifferenceorder.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.enumeration.InvoiceTypeEnum;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifferenceDetail;
import com.edc.erp.disdifferenceorder.mapper.OrdDisDelivDifferenceDetailMapper;
import com.edc.erp.disdifferenceorder.model.in.OrdDisDelivDifferenceDetailIn;
import com.edc.erp.disdifferenceorder.model.out.ExcelDisDifferenceDetail;
import com.edc.erp.disdifferenceorder.model.out.OrdDisDelivDifferenceDetailOut;
import com.edc.erp.disdifferenceorder.service.OrdDisDelivDifferenceDetailService;
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 配销差异单详细表(OrdDisDelivDifferenceDetail)表服务实现类
 *
 * @author weichao
 * @since 2022-10-24 11:16:38
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdDisDelivDifferenceDetailServiceImpl extends BaseServiceImpl<OrdDisDelivDifferenceDetail> implements OrdDisDelivDifferenceDetailService {

    private final OrdDisDelivDifferenceDetailMapper ordDisDelivDifferenceDetailMapper;

    private final FileService fileService;

    @Override
    public Page<OrdDisDelivDifferenceDetailOut> findDifferenceOrderDtlListByParameter(OrdDisDelivDifferenceDetailIn disDifferenceDetailIn) {
        List<OrdDisDelivDifferenceDetailOut> ordDisDelivDifferenceDetailOuts = ordDisDelivDifferenceDetailMapper.findDifferenceOrderDtlListByPage(disDifferenceDetailIn);
        ordDisDelivDifferenceDetailOuts.forEach(item -> {
            item.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(item.getGoodsType()));
            item.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(item.getInvoiceType()));
        });
        Page<OrdDisDelivDifferenceDetailOut> resultPage = new Page<>(disDifferenceDetailIn);
        resultPage.setList(ordDisDelivDifferenceDetailOuts);
        return resultPage;
    }

    @Override
    public String exportDisDifferenceDetails(OrdDisDelivDifferenceDetailIn disDifferenceDetailIn) {
        disDifferenceDetailIn.setPageNum(0);
        disDifferenceDetailIn.setPageSize(0);
        Page<OrdDisDelivDifferenceDetailOut> resultPage = findDifferenceOrderDtlListByParameter(disDifferenceDetailIn);
        List<ExcelDisDifferenceDetail> excelDisDifferenceDetails = parseDataToExcel(resultPage.getList());
        log.info("导出配销差异单明细列表集合大小是--{}", excelDisDifferenceDetails.size());
        String title = "导出配销差异单明细";
        byte[] bytes = FileExportUtil.getFileBytesByData(excelDisDifferenceDetails,
                title, title, ExcelDisDifferenceDetail.class, true);
        return fileService.uploadFile(title + ".xlsx", bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    @Override
    public void updateDisDifferenceDetail(OrdDisDelivDifferenceDetail differenceDetail) {
        ordDisDelivDifferenceDetailMapper.updateByPrimaryKeySelective(differenceDetail);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<OrdDisDelivDifferenceDetail> disDelivDifferenceDetails) {
        ordDisDelivDifferenceDetailMapper.batchSave(disDelivDifferenceDetails);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<OrdDisDelivDifferenceDetail> ordDisDelivDifferenceDetails) {
        ordDisDelivDifferenceDetailMapper.batchUpdate(ordDisDelivDifferenceDetails);
    }

    @Override
    public Integer getGoodsSize(Integer id) {
        return ordDisDelivDifferenceDetailMapper.getGoodsSize(id);
    }

    @Override
    public List<OrdDisDelivDifferenceDetail> selectAllByDifferenceOrderId(Integer differenceId) {
        OrdDisDelivDifferenceDetail query = new OrdDisDelivDifferenceDetail();
        query.setDifferenceOrderId(differenceId);
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        return ordDisDelivDifferenceDetailMapper.select(query);
    }

    /**
     * 根据差异单主键查询差异单详情
     *
     * @param id
     * @return
     */
    @Override
    public List<OrdDisDelivDifferenceDetailOut> findDifferenceOrderDtlById(Integer id) {
        List<OrdDisDelivDifferenceDetailOut> ordDisDelivDifferenceDetailOuts = ordDisDelivDifferenceDetailMapper.findDifferenceOrderDtlById(id);
        if (CollectionUtils.isNotEmpty(ordDisDelivDifferenceDetailOuts)) {
            ordDisDelivDifferenceDetailOuts.forEach(item -> {
                item.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(item.getGoodsType()));
                item.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(item.getInvoiceType()));
            });
        }
        return ordDisDelivDifferenceDetailOuts;
    }

    private List<ExcelDisDifferenceDetail> parseDataToExcel(List<OrdDisDelivDifferenceDetailOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    private ExcelDisDifferenceDetail convertExcel(OrdDisDelivDifferenceDetailOut ordDisDelivDifferenceDetailOut, int index) {
        ExcelDisDifferenceDetail excelDisDifferenceDetail = new ExcelDisDifferenceDetail();
        BeanUtils.copy(ordDisDelivDifferenceDetailOut, excelDisDifferenceDetail);
        if (Objects.nonNull(ordDisDelivDifferenceDetailOut)) {
            excelDisDifferenceDetail.setIsGift(NumberUtil.INTEGER_ZERO.equals(ordDisDelivDifferenceDetailOut.getIsGift()) ? "否" : "是");
        }
        excelDisDifferenceDetail.setIndex(index + 1);
        return excelDisDifferenceDetail;
    }
}
