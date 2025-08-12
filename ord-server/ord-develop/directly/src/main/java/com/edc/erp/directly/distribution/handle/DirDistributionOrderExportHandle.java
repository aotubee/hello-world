//package com.edc.erp.directly.distribution.handle;
//
//import com.alibaba.excel.EasyExcelFactory;
//import com.alibaba.excel.ExcelWriter;
//import com.alibaba.excel.write.metadata.WriteSheet;
//import com.edc.erp.common.util.NumberUtil;
//import com.edc.erp.directly.distribution.model.excel.OrdDirDistributionImportErrorResult;
//import com.edc.plugins.export.AsyncExportExecutor;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayOutputStream;
//import java.util.List;
//
///**
// * @ClassName DirDistibutionOrderExportHandle
// * @Description TODO
// * @Author ZhangYao
// * @CreateTime 2023/9/2 14:37
// **/
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class DirDistributionOrderExportHandle {
//
//    private final AsyncExportExecutor asyncExportExecutor;
//
//    public String dirDistributionAsyncExport(String fileName, String sheetName, String loginUsername, List<?> exportList, Class<?> clazz) {
//        asyncExportExecutor.export(
//                loginUsername,
//                fileName,
//                () -> dirDistributionDetailDataToByte(sheetName, exportList, clazz)
//        );
//        return AsyncExportExecutor.DOWNLOADING;
//    }
//
//    @Async
//    public void asyncDirDistributionAsyncExport(String fileName, String sheetName, String loginUsername, List<?> exportList, Class<?> clazz) {
//        asyncExportExecutor.export(
//                loginUsername,
//                fileName,
//                () -> dirDistributionDetailDataToByte(sheetName, exportList, clazz)
//        );
//    }
//
//    private byte[] dirDistributionDetailDataToByte(String sheetName, List<?> dataList, Class<?> clazz) {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ExcelWriter excelWriter = EasyExcelFactory.write(bos, clazz).build();
//        WriteSheet writeSheet = EasyExcelFactory.writerSheet(NumberUtil.INTEGER_ONE).build();
//        writeSheet.setSheetName(sheetName);
//        excelWriter.write(dataList, writeSheet);
//        excelWriter.finish();
//        return bos.toByteArray();
//    }
//}
