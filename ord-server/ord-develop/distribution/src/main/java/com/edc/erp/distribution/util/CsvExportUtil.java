package com.edc.erp.distribution.util;

import com.edc.plugins.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-09-02 09:28
 */
@Slf4j
public class CsvExportUtil {

    /**
     * CSV文件列分隔符
     */
    private static final String CSV_COLUMN_SEPARATOR = ",";

    /**
     * CSV文件行分隔符
     */
    private static final String CSV_ROW_SEPARATOR = System.lineSeparator();

    /**
     * @param dataList 集合数据
     * @param titles   表头部数据
     * @param keys     表内容的键值
     */
    public static byte[] doExport(List<Map<String, Object>> dataList, String titles, String keys) {

        // 保证线程安全
        StringBuffer buf = new StringBuffer();

        String[] titleArr;
        String[] keyArr;

        titleArr = titles.split(",");
        keyArr = keys.split(",");

        // 组装表头
        for (String title : titleArr) {
            buf.append(title).append(CSV_COLUMN_SEPARATOR);
        }
        buf.append(CSV_ROW_SEPARATOR);

        // 组装数据
        if (CollectionUtils.isNotEmpty(dataList)) {
            for (Map<String, Object> data : dataList) {
                for (String key : keyArr) {
                    buf.append(data.get(key)).append(CSV_COLUMN_SEPARATOR);
                }
                buf.append(CSV_ROW_SEPARATOR);
            }
        }
        try {
            return buf.toString().getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            log.error("{}导出异常", titles, e);
            e.printStackTrace();

        }
//
//        FileExportUtil.getFileBytesByDataForCsv(excelGoods,
//                "商品信息",
//                "商品信息",
//                ExcelGoodsInfo.class,
//                true);
        return null;
    }

    /**
     * 设置Header
     *
     * @param fileName
     * @param response
     * @throws UnsupportedEncodingException
     */
    public static void responseSetProperties(String fileName, HttpServletResponse response) throws UnsupportedEncodingException {
        // 设置文件后缀
        String time = DateUtils.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        String fn = fileName + time + ".csv";
        // 读取字符编码
        String utf = "UTF-8";

        // 设置响应
        response.setContentType("application/ms-txt.numberformat:@");
        response.setCharacterEncoding(utf);
        response.setHeader("Pragma", "public");
        response.setHeader("Cache-Control", "max-age=30");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fn, utf));
    }


    /**
     * @param value 待处理的字段值
     * @description: 处理csv文件字段中需要转义的引号
     * 添加双引号，防止被字段中的逗号和换行符干扰
     * 使其显示为一个单元格
     * @return: {@link String}
     * @author: zhangyao
     * @date: 2022-05-10 14:49:46
     */
    public static String processValueForCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains("\"")) {
            value = value.replaceAll("\"", "\"\"");
        }
        value = "\"" + value + "\"";

        return value;
    }

}
