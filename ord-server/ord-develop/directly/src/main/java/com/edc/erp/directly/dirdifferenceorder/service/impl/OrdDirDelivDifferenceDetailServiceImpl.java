package com.edc.erp.directly.dirdifferenceorder.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.enumeration.InvoiceTypeEnum;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifferenceDetail;
import com.edc.erp.directly.dirdifferenceorder.mapper.OrdDirDelivDifferenceDetailMapper;
import com.edc.erp.directly.dirdifferenceorder.model.in.OrdDirDelivDifferenceDetailIn;
import com.edc.erp.directly.dirdifferenceorder.model.out.ExcelDirDifferenceDetail;
import com.edc.erp.directly.dirdifferenceorder.model.out.OrdDirDelivDifferenceDetailOut;
import com.edc.erp.directly.dirdifferenceorder.service.OrdDirDelivDifferenceDetailService;
import com.edc.erp.directly.util.FileExportUtil;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 差异单详细表(OrdDirDelivDifferenceDetail)表服务实现类
 *
 * @author weichao
 * @since 2022-11-14 11:32:46
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdDirDelivDifferenceDetailServiceImpl extends BaseServiceImpl<OrdDirDelivDifferenceDetail> implements OrdDirDelivDifferenceDetailService {

    private final OrdDirDelivDifferenceDetailMapper ordDirDelivDifferenceDetailMapper;

    private final FileService fileService;

    @Override
    public Integer getGoodsSize(Integer id) {
        return ordDirDelivDifferenceDetailMapper.getGoodsSize(id);
    }

    @Override
    public Page<OrdDirDelivDifferenceDetailOut> findDifferenceOrderDtlListByParameter(OrdDirDelivDifferenceDetailIn dirDifferenceDetailIn) {
        List<OrdDirDelivDifferenceDetailOut> ordDirDelivDifferenceDetailOuts = ordDirDelivDifferenceDetailMapper.findDifferenceOrderDtlListByPage(dirDifferenceDetailIn);
        ordDirDelivDifferenceDetailOuts.forEach(item -> {
            item.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(item.getGoodsType()));
            item.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(item.getInvoiceType()));
        });
        Page<OrdDirDelivDifferenceDetailOut> resultPage = new Page<>(dirDifferenceDetailIn);
        resultPage.setList(ordDirDelivDifferenceDetailOuts);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<OrdDirDelivDifferenceDetail> details) {
        ordDirDelivDifferenceDetailMapper.batchSave(details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDirDifferenceDetail(OrdDirDelivDifferenceDetail differenceDetail) {
        ordDirDelivDifferenceDetailMapper.updateByPrimaryKeySelective(differenceDetail);
    }

    @Override
    public String exportDirDifferenceDetails(OrdDirDelivDifferenceDetailIn dirDifferenceDetailIn) {
        dirDifferenceDetailIn.setPageNum(0);
        dirDifferenceDetailIn.setPageSize(0);
        Page<OrdDirDelivDifferenceDetailOut> resultPage = findDifferenceOrderDtlListByParameter(dirDifferenceDetailIn);
        List<ExcelDirDifferenceDetail> excelDirDifferenceDetails = parseDataToExcel(resultPage.getList());
        log.info("导出差异单明细列表集合大小是--{}", excelDirDifferenceDetails.size());
        String title = "导出直营配货差异单明细";
        byte[] bytes = FileExportUtil.getFileBytesByData(excelDirDifferenceDetails,
                title, title, ExcelDirDifferenceDetail.class, true);
        return fileService.uploadFile(title + ".xlsx", bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<OrdDirDelivDifferenceDetail> ordDirDelivDifferenceDetails) {
        ordDirDelivDifferenceDetailMapper.batchUpdate(ordDirDelivDifferenceDetails);
    }

    /**
     * 根据差异单主键查询差异单明细
     *
     * @param id
     * @return
     */
    @Override
    public List<OrdDirDelivDifferenceDetailOut> findDifferenceOrderDtlById(Integer id) {
        List<OrdDirDelivDifferenceDetailOut> dirDelivDifferenceDetails = ordDirDelivDifferenceDetailMapper.findDifferenceOrderDtlById(id);
        if (CollectionUtils.isNotEmpty(dirDelivDifferenceDetails)) {
            dirDelivDifferenceDetails.forEach(item -> {
                item.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(item.getGoodsType()));
                item.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(item.getInvoiceType()));
            });
        }
        return dirDelivDifferenceDetails;
    }

    private List<ExcelDirDifferenceDetail> parseDataToExcel(List<OrdDirDelivDifferenceDetailOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    private ExcelDirDifferenceDetail convertExcel(OrdDirDelivDifferenceDetailOut ordDirDelivDifferenceDetailOut, int index) {
        ExcelDirDifferenceDetail excelDisDifferenceDetail = new ExcelDirDifferenceDetail();
        BeanUtils.copy(ordDirDelivDifferenceDetailOut, excelDisDifferenceDetail);
        excelDisDifferenceDetail.setIsGift(NumberUtil.INTEGER_ZERO.equals(ordDirDelivDifferenceDetailOut.getIsGift()) ? "否" : "是");
        excelDisDifferenceDetail.setIndex(index + 1);
        return excelDisDifferenceDetail;
    }


}
