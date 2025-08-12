package com.edc.erp.distribution.util;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 处理导出工具类
 *
 * @author lee
 */
@Slf4j
public class FileExportUtil {

    public static byte[] getFileBytesByData(List<?> list, String title, String sheetName, Class<?> pojoClass, boolean isCreateHeader) {
        // 将结果集转换为excel
        byte[] bytes;
        try {
            ExportParams exportParams = new ExportParams(title, sheetName, ExcelType.XSSF);
            exportParams.setCreateHeadRows(isCreateHeader);
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, pojoClass, list);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            bytes = baos.toByteArray();
            baos.flush();
            baos.close();
        } catch (IOException e) {
            log.error("导出列表转换失败");
            return null;
        }
        return bytes;
    }


}
