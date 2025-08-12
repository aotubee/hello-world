package com.edc.erp.distribution.util;

import com.edc.erp.common.util.NumberUtil;
import com.edc.plugins.common.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yaojinpeng
 * @since 2022/10/19 16:04
 */
public class ImportUtil {

    /**
     * 模板导入出参封装
     *
     * @param strings  错误集合
     * @param size 正确导入的集合数
     * @return
     */
    public static Response<Map<String, List<String>>> getStringResponse(List<String> strings, int size) {
        String successMessage = "成功导入" + size + "条记录";
        Map<String, List<String>> map = new HashMap(NumberUtil.INTEGER_TWO);
        strings.add(0, successMessage + ";");
        // 有错误信息
        if (strings.size() > 1) {
            map.put("warn", strings);
            return Response.data(map);
        }
        map.put("success", strings);
        return Response.data(map);
    }
}
