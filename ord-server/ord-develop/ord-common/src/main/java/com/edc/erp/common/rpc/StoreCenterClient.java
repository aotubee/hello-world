package com.edc.erp.common.rpc;

import com.edc.erp.common.model.entity.EquipmentStockAllot;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.AsyncDeliveryImportInfoIn;
import com.edc.erp.common.model.in.StoreAreaIn;
import com.edc.erp.common.model.in.store.QueryBizOrgCodeStoreIn;
import com.edc.erp.common.model.in.store.QueryEquipmentStockAllotIn;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.in.store.StoreStatusInfo;
import com.edc.erp.common.model.out.store.*;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


/**
 * 门店信息
 *
 * @author weichao
 */
@FeignClient(name = "mdm", contextId = "storeCenterClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface StoreCenterClient {

    /**
     * 根据门店查询门店信息
     *
     * @param storeCode
     * @return
     */

    @GetMapping("/storeCenter/storeInfo/getStoreInfoByStoreCode")
    Response<StoreOut> getStoreInfoByErpStoreCode(@RequestParam("storeCode") String storeCode);

    /**
     * 获取门店配送信息
     *
     * @param storeId
     * @return
     */
    @ApiOperation(value = "获取门店配送信息", notes = "获取门店配送信息")
    @GetMapping("/storeCenter/store/logistics/get")
    Response<StoreLogisticsOut> findStoreOnline(@RequestParam("storeId") Integer storeId);

    /**
     * 获取门店信息
     *
     * @param storeInfoIn
     * @return
     */
    @PostMapping("/storeCenter/storeInfo/findStoreInfoNoToken")
    Response<Page<StoreInfoOut>> findStoreInfo(@RequestBody StoreInfoIn storeInfoIn);

    /**
     * 根据条件获取门店code集合
     *
     * @param storeArea
     * @param storeName
     * @return
     */
    @GetMapping("/storeCenter/storeInfo/findStoreCodeList")
    Response<List<String>> findStoreCodeList(@RequestParam(value = "storeArea", required = false) String storeArea, @RequestParam(value = "storeName", required = false) String storeName);

    /**
     * 根据门店代码获取订单类型id
     *
     * @param storeCode
     * @return
     */
    @GetMapping("/gc/storeUnit/findOrderTypeIdByStoreCode")
    Response<List<Integer>> findOrderTypeIdByStoreCode(@RequestParam String storeCode);

    /**
     * 查询门店业务控制开关
     *
     * @param storeLifeCycle
     * @param bizOrgCode
     * @return
     */
    @GetMapping(value = "/storeCenter/store/status/getSwitch")
    Response<StoreStatusBusinessSwitch> getSwitch(@RequestParam String storeLifeCycle, @RequestParam String bizOrgCode);

    /**
     * 根据门店代码和门店类型查询门店信息和客户信息
     *
     * @param storeCode
     * @param storeProperty
     * @return
     */
    @GetMapping("/storeCenter/storeInfo/getStoreByStoreCodeAndType")
    Response<StoreAndClientInfoOut> getStoreByStoreCodeAndType(@RequestParam("storeCode") String storeCode, @RequestParam("storeProperty") String storeProperty, @RequestParam("bizOrgCode") String bizOrgCode);

    /**
     * 获取本组织自动跑货的门店代码集合
     *
     * @param bizOrgCode
     * @return
     */
    @GetMapping("/gc/manage/replenishmentConfig/findIsAutoReplenishmentStoreCodeList")
    Response<List<String>> findIsAutoReplenishmentStoreCodeList(@RequestParam("bizOrgCode") String bizOrgCode);

    @GetMapping("/storeCenter/storeInfo/getStoreInfoByCode")
    Response<StoreInfoBackOut> getStoreInfoByCode(@RequestParam String erpStoreCode);

    @GetMapping("/storeCenter/storeInfo/getStoreInfo")
    Response<StoreInfo> getStoreInfo(@RequestParam("storeCode") String storeCode,
                                     @RequestParam("bizOrgCode") String bizOrgCode);

    /**
     * 获取门店信息(门店类型,业务类型)
     *
     * @param storeStatusInfo
     * @return
     */
    @ApiOperation(value = "获取门店信息(门店类型,业务类型)", notes = "获取门店信息")
    @GetMapping("/storeCenter/storeInfo/getStatusStoreInfo")
    Response<StoreInfo> getStatusStoreInfo(@SpringQueryMap StoreStatusInfo storeStatusInfo);

    /**
     * 根据门店代码获取门店单元
     * @return
     * @param storeCode
     * @param bizOrgCode
     */
    @ApiOperation(value = "根据门店代码获取门店单元", notes = "", httpMethod = "GET")
    @GetMapping("/gc/storeUnit/getStoreUnitByStoreCode")
    Response<StoreUnit> getStoreUnitByStoreCode(@RequestParam("storeCode") String storeCode, @RequestParam("bizOrgCode") String bizOrgCode);

    /**
     * 根据门店code和业务组织代码查询门店单元关联订单类型配置转单优先级
     *
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "根据门店code和业务组织代码查询门店单元关联订单类型配置转单优先级", notes = "根据门店code和业务组织代码查询门店单元关联订单类型配置转单优先级")
    @GetMapping("/gc/storeUnit/getOrderPriorityByStoreCode")
    Response<String> getOrderPriorityByStoreCode(@RequestParam("storeCode") String storeCode,
                                                 @RequestParam(value = "bizOrgCode", required = false) String bizOrgCode,
                                                 @RequestParam("stockCode") String stockCode,
                                                 @RequestParam("distributionType") String distributionType);


    @ApiOperation(value = "获取库存分配规则", notes = "获取库存分配规则")
    @PostMapping("/logc/equipmentStockAllot/findByWmsCodeAndStockCode")
    Response<Map<String, EquipmentStockAllot>> findByWmsCodeAndStockCode(@RequestBody QueryEquipmentStockAllotIn queryEquipmentStockAllotIn);

    @ApiOperation(value = "根据门店代码获取门店配送信息", notes = "根据门店代码获取门店配送信息")
    @GetMapping("/storeCenter/store/logistics/getStoreLogisticsByStoreCode")
    Response<StoreLogisticsOut> getStoreLogisticsByStoreCode(@RequestParam @NotNull String storeCode, @RequestParam @NotNull String bizOrgCode);

    @ApiOperation(value = "根据门店属性查询门店", notes = "根据门店属性查询门店")
    @GetMapping("/storeCenter/storeInfo/findStoreInfoByProperty")
    Response<List<StoreInfo>> findStoreInfoByProperty(@RequestParam(value = "storeCodes") @NotNull List<String> storeCodes,
                                                      @RequestParam(value = "storeProperty") @NotNull String storeProperty,
                                                      @RequestParam(value = "bizOrgCode") @NotNull String bizOrgCode);

    /**
     * 通过门店单元代码集合和组织代码查询下面关联的门店代码集合
     * @param unitCodeList
     * @param bizOrgCode
     * @return
     */
    @GetMapping("/gc/storeUnit/findStoreCodeListByUnitCodeList")
    Response<List<String>> findStoreCodeListByUnitCodeListAndOrgCode(@RequestParam("unitCodeList") List<String> unitCodeList,
                                                                     @RequestParam("bizOrgCode") String bizOrgCode);

    /**
     * 获取本组织自动跑货的门店代码集合
     *
     * @param bizOrgCode
     * @param storeProperty
     * @return
     */
    @GetMapping("/gc/manage/replenishmentConfig/findIsAutoReplenishmentStoreCodeListByProperty")
    Response<List<String>> findIsAutoReplenishmentStoreCodeListByProperty(@RequestParam("bizOrgCode") String bizOrgCode,
                                                                          @RequestParam("storeProperty") String storeProperty);

    /**
     * 判断门店业务类型及获取门店名称
     */
    @GetMapping(value = "/storeCenter/storeInfo/getStoreBusinessType")
    Response<StoreBusinessSwitchOut> getStoreBusinessType(@RequestParam String storeCode, @RequestParam String bizOrgCode, @RequestParam String businessType);

    @GetMapping(value = "/storeCenter/storeInfo/getStoreAndStatusSwitch")
    Response<StoreAndStatusSwitchOut> getStoreAndStatusSwitch(@RequestParam String storeCode, @RequestParam(required = false) String bizOrgCode);


    @ApiOperation(value = "根据门店代码查询门店基础信息(传bizOrgCode为所属+可配)", notes = "根据门店代码查询门店基础信息(传bizOrgCode为所属+可配)")
    @PostMapping("/storeCenter/storeInfo/findStoreInfoByStoreCodeList")
    Response<List<StoreInfo>> findStoreInfoByStoreCodeList(@RequestBody AsyncDeliveryImportInfoIn asyncDeliveryImportInfoIn);


    @ApiOperation(value = "登录人bizOrgCode获取门店信息(门店类型,业务类型)", notes = "登录人bizOrgCode获取门店信息获取门店信息")
    @GetMapping("/storeCenter/storeInfo/getStatusStoreInfoByAuthOrg")
    Response<StoreInfo> getStatusStoreInfoByAuthOrg(@SpringQueryMap StoreStatusInfo storeStatusInfo);

    @ApiOperation(value = "根据门店代码获取门店配送信息", notes = "根据门店代码获取门店配送信息")
    @GetMapping("/storeCenter/store/delivery/getByStoreCode")
    Response<List<StoreDelivery>> getByStoreCode(@RequestParam String storeCode);

    @ApiOperation(value = "门店代码集合与组织查询门店信息", notes = "门店代码集合与组织查询门店信息")
    @PostMapping("/storeCenter/storeInfo/findSimpleStoresByCodes")
    Response<List<StoreInfo>> findSimpleStoresByCodes(@RequestBody QueryBizOrgCodeStoreIn queryBizOrgCodeStoreIn);

    @ApiOperation(value = "根据区域代码集合获取门店集合", notes = "根据区域代码集合获取门店集合")
    @PostMapping("/storeCenter/storeInfo/findByAreaCodes")
    Response<List<StoreInfoOut>> findByAreaCodes(@RequestBody StoreInfoIn storeInfoIn);

    @ApiOperation(value = "根据区域代码集合获取门店集合(包含父子混合)", notes = "根据区域代码集合获取门店集合")
    @PostMapping("/storeCenter/storeInfo/findByAreaCodeList")
    Response<List<StoreInfoOut>> findByAreaCodeList(@RequestBody StoreInfoIn storeInfoIn);


    @ApiOperation(value = "根据区域代码集合获取门店集合(包含父子混合)", notes = "根据区域代码集合获取门店集合")
    @PostMapping("/storeCenter/store/area/findChildAreasByCodes")
    Response<List<String>> findChildAreasByCodes(@RequestBody StoreAreaIn storeAreaIn);

}