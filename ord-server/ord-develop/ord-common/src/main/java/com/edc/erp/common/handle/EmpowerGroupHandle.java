package com.edc.erp.common.handle;

import com.alibaba.fastjson.JSON;
import com.edc.erp.common.model.in.FindGroupIn;
import com.edc.erp.common.model.out.EmpowerGroupAuthCodeOut;
import com.edc.erp.common.model.out.EmpowerGroupBaseDetailOut;
import com.edc.erp.common.model.out.EmpowerGroupDetailOut;
import com.edc.erp.common.model.out.EmpowerGroupOut;
import com.edc.erp.common.rpc.EmpowerGroupClient;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description 授权组适配器
 *
 * @author gusiyuan
 * @since 2024/11/12 18:36
 */
@Component
public class EmpowerGroupHandle {

    private final EmpowerGroupClient empowerGroupClient;

    public EmpowerGroupHandle(EmpowerGroupClient empowerGroupClient) {
        this.empowerGroupClient = empowerGroupClient;
    }

    public List<String> findEmpowerGroupsByType(String bizOrgCode, String groupType) {
        // token中取出用户所属授权组
        String erpGroupCodeStr = UserUtil.getErpGroupCodeList();
        List<EmpowerGroupOut> empowerGroupOuts = JSON.parseArray(erpGroupCodeStr, EmpowerGroupOut.class);
        if (CollectionUtils.isEmpty(empowerGroupOuts)) {
            return Collections.EMPTY_LIST;
        }
        // 取出规定类型组代码集合
        Map<String, List<String>> map = empowerGroupOuts.stream()
                .collect(Collectors.groupingBy(EmpowerGroupOut::getGroupType, Collectors.mapping(EmpowerGroupOut::getGroupCode, Collectors.toList())));
        List<String> groupCodes = map.get(groupType);
        if (CollectionUtils.isEmpty(groupCodes)) {
            return Collections.EMPTY_LIST;
        }
        FindGroupIn findGroupIn = new FindGroupIn();
        findGroupIn.setGroupType(groupType);
        findGroupIn.setBizOrgCode(bizOrgCode);
        findGroupIn.setGroupCodes(groupCodes);
        Response<List<String>> dataResp = empowerGroupClient.findByTypeAndGroup(findGroupIn);
        if (!dataResp.isSuccess()){
            throw new BusinessException(dataResp.getMessage());
        }
        List<String> codes = dataResp.getData();
        return codes;
    }

    public List<EmpowerGroupBaseDetailOut> findEmpowerGroupsInfoByType(String bizOrgCode, String groupType) {
        // token中取出用户所属授权组
        String erpGroupCodeStr = UserUtil.getErpGroupCodeList();
        List<EmpowerGroupOut> empowerGroupOuts = JSON.parseArray(erpGroupCodeStr, EmpowerGroupOut.class);
        if (CollectionUtils.isEmpty(empowerGroupOuts)) {
            return Collections.EMPTY_LIST;
        }
        // 取出规定类型组代码集合
        Map<String, List<String>> map = empowerGroupOuts.stream()
                .collect(Collectors.groupingBy(EmpowerGroupOut::getGroupType, Collectors.mapping(EmpowerGroupOut::getGroupCode, Collectors.toList())));
        List<String> groupCodes = map.get(groupType);
        if (CollectionUtils.isEmpty(groupCodes)) {
            return Collections.EMPTY_LIST;
        }
        FindGroupIn findGroupIn = new FindGroupIn();
        findGroupIn.setGroupType(groupType);
        findGroupIn.setBizOrgCode(bizOrgCode);
        findGroupIn.setGroupCodes(groupCodes);
        Response<List<EmpowerGroupBaseDetailOut>> dataResp = empowerGroupClient.findAllByTypeAndGroup(findGroupIn);
        if (!dataResp.isSuccess()){
            throw new BusinessException(dataResp.getMessage());
        }
        List<EmpowerGroupBaseDetailOut> codes = dataResp.getData();
        return codes;
    }

//    public EmpowerGroupAuthCodeOut findEmpowerGroups(String bizOrgCode) {
//        EmpowerGroupAuthCodeOut empowerGroupAuthCodeOut = new EmpowerGroupAuthCodeOut();
//        // token中取出用户所属授权组
//        String erpGroupCodeStr = UserUtil.getErpGroupCodeList();
//        List<EmpowerGroupOut> empowerGroupOuts = JSON.parseArray(erpGroupCodeStr, EmpowerGroupOut.class);
//        if (CollectionUtils.isEmpty(empowerGroupOuts)) {
//            return empowerGroupAuthCodeOut;
//        }
//        // 分组整合
//        List<FindGroupIn> findGroupIns = empowerGroupOuts.stream()
//                .collect(Collectors.groupingBy(EmpowerGroupOut::getGroupType, Collectors.mapping(EmpowerGroupOut::getGroupCode, Collectors.toList())))
//                .entrySet().stream()
//                .map(entry -> {
//                    FindGroupIn findGroupIn = new FindGroupIn();
//                    findGroupIn.setGroupType(entry.getKey());
//                    findGroupIn.setGroupCodes(entry.getValue());
//                    findGroupIn.setBizOrgCode(bizOrgCode);
//                    return findGroupIn;
//                })
//                .collect(Collectors.toList());
//        if (CollectionUtils.isEmpty(findGroupIns)) {
//            return empowerGroupAuthCodeOut;
//        }
//        Response<List<EmpowerGroupDetailOut>> dataResp = empowerGroupClient.findByTypeAndGroups(findGroupIns);
//        if (!dataResp.isSuccess()){
//            throw new BusinessException(dataResp.getMessage());
//        }
//        List<EmpowerGroupDetailOut> empowerGroupDetailOuts = dataResp.getData();
//        List<String> authorizeVendorCodes = empowerGroupDetailOuts.stream().filter(item -> PurConstant.EmpowerGroupType.PURCHASE.equals(item.getGroupType())).map(EmpowerGroupDetailOut::getCode).collect(Collectors.toList());
//        empowerGroupAuthCodeOut.setAuthorizeVendorCodes(authorizeVendorCodes);
//        empowerGroupAuthCodeOut.setAuthorizeStockCodes(authorizeStockCodes);
//        return empowerGroupAuthCodeOut;
//    }

//    public void checkVendorAndStockAuthed(String bizOrgCode, String vendorCode, String stockCode) {
//        EmpowerGroupAuthCodeOut empowerGroups = this.findEmpowerGroups(bizOrgCode);
//        List<String> authorizeVendorCodes = empowerGroups.getAuthorizeVendorCodes();
//        List<String> authorizeStockCodes = empowerGroups.getAuthorizeStockCodes();
//        if (CollectionUtils.isNotEmpty(authorizeVendorCodes) && !authorizeVendorCodes.contains(vendorCode)) {
//            throw new BusinessException("订单方" + vendorCode + "未被授权使用！");
//        }
//        if (CollectionUtils.isNotEmpty(authorizeStockCodes) && !authorizeStockCodes.contains(stockCode)) {
//            throw new BusinessException("仓位" + stockCode + "未被授权使用！");
//        }
//    }
}
