package com.edc.erp.common.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.excel.ExportOrderDelivryDataFile;
import com.edc.erp.common.excel.ImportOrderDeliveryDataFile;
import com.edc.erp.common.listener.OrderDeliveryDataFileImportListener;
import com.edc.plugins.utils.bean.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 配销/配货 配销退货,配货退货数据上传 处理工具
 */
public class OrderDelivryDataFileUtil {

    public static byte[] initExportByte(List<ImportOrderDeliveryDataFile> fileDatas, String title, Class clazz) {
        List<?> exportOrdDirReturnDetails = parseDataToExcel(fileDatas,clazz);
        return FileExportUtil.getFileBytesByData(exportOrdDirReturnDetails,
                title, title, clazz, true);
    }

    public static List<ImportOrderDeliveryDataFile> parseImportFile2List(byte[] fileBytes) {
        try{
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
            OrderDeliveryDataFileImportListener listener = new OrderDeliveryDataFileImportListener();
            ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrderDeliveryDataFile.class, listener).headRowNumber(1).build();
            ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
            excelReader.read(readSheet).finish();
            return listener.getOrderDeliveryDataFiles();
        }catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<ExportOrderDelivryDataFile> parseDataToExcel(List<ImportOrderDeliveryDataFile> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> converExcel(list.get(i)))
                .collect(Collectors.toList());
    }
    public static List<?> parseDataToExcel(List<ImportOrderDeliveryDataFile> list,Class clazz) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> converExcelByClass(list.get(i),clazz))
                .collect(Collectors.toList());
    }
    private static ExportOrderDelivryDataFile converExcel(ImportOrderDeliveryDataFile detailOut) {
        ExportOrderDelivryDataFile exportFile = new ExportOrderDelivryDataFile();
        BeanUtils.copy(detailOut, exportFile);
        if (detailOut.getIsSuccessed() == 1) {
            exportFile.setStatus("成功");
        } else {
            exportFile.setStatus("失败");
        }
        return exportFile;
    }

    private static Object converExcelByClass(ImportOrderDeliveryDataFile detailOut,Class clazz) {
        Object exportFile = null;
        try {
            exportFile = clazz.getDeclaredConstructor(new Class[0]).newInstance();
            BeanUtils.copy(detailOut, exportFile);
            String text = detailOut.getIsSuccessed() == 1 ?"成功":"失败";
            exportFile.getClass().getDeclaredMethod("setStatus",String.class).invoke(exportFile,text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exportFile;
    }

    public static boolean isGoodsCodeRepeated(Collection<String> goodsCodeList, String goodsCode) {
        return new HashBag(goodsCodeList).getCount(goodsCode) > 1;
    }

    /**
     * importDatas 中是否在 goodsCodeList 中存在1个以上
     * @param goodsCodeList
     * @param importDatas
     * @param msg
     * @return
     */
    public static boolean isHasRepeatGoodsCode(List<ImportOrderDeliveryDataFile> importDatas, Collection<String> goodsCodeList, String msg) {
        if(CollectionUtils.isEmpty(goodsCodeList)) return false;
        boolean hasRepeated = false;
        for (ImportOrderDeliveryDataFile importData : importDatas) {
            if(StringUtils.isBlank(importData.getGoodsCode())) continue;
            boolean isRepeated = isGoodsCodeRepeated(goodsCodeList,importData.getGoodsCode());
            if(isRepeated) {
                initErrorData(importData, msg);
                hasRepeated = true;
            }
        }
        return hasRepeated;
    }

    public static boolean isHasRepeatGoodsCode(Collection<String> toBeCompareCodes, Collection<String> goodsCodeList) {
        if(CollectionUtils.isEmpty(goodsCodeList)) return false;
        boolean hasRepeated = false;
        for (String code : toBeCompareCodes) {
            if(StringUtils.isBlank(code)) continue;
            boolean isRepeated = isGoodsCodeRepeated(goodsCodeList,code);
            if(isRepeated) {
                hasRepeated = true;
            }
        }
        return hasRepeated;
    }

    public static void initDetailErrRremark(List<ImportOrderDeliveryDataFile> importOrderDeliveryGoods, String errMsg) {
        for (ImportOrderDeliveryDataFile importOrderDeliveryGood : importOrderDeliveryGoods) {
            initErrorData(importOrderDeliveryGood, errMsg);
        }
    }

    public static boolean initErrorData(ImportOrderDeliveryDataFile importData, String msg) {
        importData.setIsSuccessed(0);
        dealNullRemark(importData);
        if(StringUtils.isBlank(msg)) {
            return false;
        }
        importData.setRemark(importData.getRemark() + msg + ";");
        return false;
    }

    public static void dealNullRemark(ImportOrderDeliveryDataFile importData) {
        if (StringUtils.isBlank(importData.getRemark())) {
            importData.setRemark(StringUtils.EMPTY);
        }
    }

    public static Map<String, List<ImportOrderDeliveryDataFile>> trans2MapNoNUllByOrderNo(List<ImportOrderDeliveryDataFile> fileDatas) {
        return fileDatas.stream().filter(p -> StringUtils.isNotBlank(p.getOrderNo())).collect(Collectors.groupingBy(ImportOrderDeliveryDataFile::getOrderNo));
    }

}
