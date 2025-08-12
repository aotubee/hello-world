package com.edc.erp.common.util;

import com.alibaba.excel.event.AnalysisEventListener;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.out.ImportErrorOut;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 导入模板父监听器
 *
 * @param <T>
 * @author w
 */
public abstract class ImportListener<T> extends AnalysisEventListener<T> {

    public List<Map<String, String>> errorList = new ArrayList<>();

    public Map<String, StringJoiner> totalErrorMap = new HashMap<>();


    /**
     * 获取导入错误
     *
     * @return
     */
    public List<String> getErrorDate() {
        List<String> strings = new ArrayList<>();
        for (Map<String, String> map : this.errorList) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
                if (StringUtils.isNotEmpty(stringStringEntry.getValue())) {
                    stringBuilder.append(stringStringEntry.getValue());
                }
            }
            strings.add(stringBuilder.toString());
        }
        return strings;
    }

    public String getImportErrorMessage(Integer maxImportCount, Integer dataListSize, Map<String, StringJoiner> errorMap) {
        int num = Math.max(0, maxImportCount - dataListSize - 2);
        String errorMessage = "";
        List<ImportErrorOut> sortImportErrorMessageOuts = Lists.newArrayList();
        if (errorMap.size() > NumberUtils.INTEGER_ZERO) {
            errorMap.entrySet().forEach(entry -> {
                String regex = "(?<=【)(.*?)(?=】)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(entry.getKey());

                ImportErrorOut sortImportErrorMessageOut = new ImportErrorOut();
                Integer sortNumber = -1;
                if (matcher.find()) {
                    sortNumber = Integer.parseInt(matcher.group());
                }
                sortImportErrorMessageOut.setSortNumber(sortNumber);
                sortImportErrorMessageOut.setErrorMessage(entry.getKey() + entry.getValue().toString());
                sortImportErrorMessageOuts.add(sortImportErrorMessageOut);
            });
            errorMessage = sortImportErrorMessageOuts.stream().sorted(Comparator.comparing(ImportErrorOut::getSortNumber))
                    .map(ImportErrorOut::getErrorMessage).collect(Collectors.joining(SystemConstant.COMMA));
        }
        String message = "成功导入" + dataListSize + "条数据。\n" +
                "失败" + num + "条数据; \n"
                + errorMessage;
        return message;
    }

    public String getImportErrorMessage(Map<String, StringJoiner> errorMap) {
        String message = "";
        List<ImportErrorOut> sortImportErrorMessageOuts = Lists.newArrayList();
        if (errorMap.size() > NumberUtils.INTEGER_ZERO) {
            errorMap.entrySet().forEach(entry -> {
                String regex = "(?<=【)(.*?)(?=】)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(entry.getKey());

                ImportErrorOut sortImportErrorMessageOut = new ImportErrorOut();
                Integer sortNumber = -1;
                if (matcher.find()) {
                    sortNumber = Integer.parseInt(matcher.group());
                }
                sortImportErrorMessageOut.setSortNumber(sortNumber);
                sortImportErrorMessageOut.setErrorMessage(entry.getKey() + entry.getValue().toString());
                sortImportErrorMessageOuts.add(sortImportErrorMessageOut);
            });
            message = sortImportErrorMessageOuts.stream().sorted(Comparator.comparing(ImportErrorOut::getSortNumber))
                    .map(ImportErrorOut::getErrorMessage).collect(Collectors.joining(SystemConstant.COMMA + "\n"));
        }
        return StringUtils.isNotBlank(message) ? message : "导入中，请稍后查看。";
    }
}
