package com.edc.erp.directly.distribution.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.handle.EmpowerGroupHandle;
import com.edc.erp.common.mapper.OrgSortMapper;
import com.edc.erp.common.model.in.StoreAreaIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.out.goods.GoodsSortNodeOut;
import com.edc.erp.common.model.out.goods.GoodsSortOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreDelivery;
import com.edc.erp.common.model.out.store.StoreInfoOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirordercart.service.ShoppingCartService;
import com.edc.erp.directly.distribution.entity.OrdDirOrderAllocationPool;
import com.edc.erp.directly.distribution.entity.OrdDirOrderAllocationPoolHistory;
import com.edc.erp.directly.distribution.excel.ExportOrdDirOrderAllocationPool;
import com.edc.erp.directly.distribution.handle.OrderDetailHandle;
import com.edc.erp.directly.distribution.listener.OrdDirOrderAllocationPoolListener;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderAllocationPoolHistoryMapper;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderAllocationPoolMapper;
import com.edc.erp.directly.distribution.model.in.ImportOrdAllocationPoolGoodsVO;
import com.edc.erp.directly.distribution.model.in.QueryAllocationPoolPageIn;
import com.edc.erp.directly.distribution.model.in.UpdateAllocationPoolIn;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderAllocationPoolPageOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderAllocationPoolService;
import com.edc.erp.directly.distribution.util.FileExportUtil;
import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.erp.directly.enumeration.OrderAllocationPoolStatusEnum;
import com.edc.erp.directly.handle.DirOrderConfigHandle;
import com.edc.erp.directly.model.out.OrderCycleDeliveryOut;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.uc.authority.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName OrdDirOrderAllocationPoolServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/1/7 8:55
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class OrdDirOrderAllocationPoolServiceImpl extends BaseServiceImpl<OrdDirOrderAllocationPool> implements OrdDirOrderAllocationPoolService {

    private final OrdDirOrderAllocationPoolMapper ordDirOrderAllocationPoolMapper;

    private final OrderGoodsServer orderGoodsServer;

    private final DirOrderConfigHandle orderConfigHandle;

    private final ShoppingCartService shoppingCartService;

    private final OrderDetailHandle orderDetailHandle;

    private final OrdDirOrderAllocationPoolHistoryMapper ordDirOrderAllocationPoolHistoryMapper;

    private final FileService fileService;

    private final StockServer stockServer;

    private final StoreCenterService storeCenterService;

    private final EmpowerGroupHandle empowerGroupHandle;

    private final OrgSortMapper orgSortMapper;

    private final RedisService redisService;

    @Override
    public List<String> findStoreCodeList() {
        return ordDirOrderAllocationPoolMapper.findStoreList();
    }

    @Override
    public List<OrdDirOrderAllocationPool> findEmptyTruncationDateTimeListByStoreCode(String storeCode) {
        return ordDirOrderAllocationPoolMapper.findEmptyTruncationDateTimeListByStoreCode(storeCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleSupplementQuantity(OrdDirOrderAllocationPool ordDirOrderAllocationPool, Map<String, StoreDelivery> deliveryTypeMap, Map<String, Map<String, BigDecimal>> truncationTimeGoodsQuantityMap) {
        String bizOrgCode = ordDirOrderAllocationPool.getBizOrgCode();
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setStoreCode(ordDirOrderAllocationPool.getStoreCode());
        orderGoodsIn.setGoodsCode(ordDirOrderAllocationPool.getGoodsCode());
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.getTypeBySourceCode(SourceTypeEnum.INITIATIVE.getKey()));
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.isNull(orderGoodsOut)) {
            log.error("门店{}订单调配商品{}查询为空", ordDirOrderAllocationPool.getStoreCode(), ordDirOrderAllocationPool.getGoodsCode());
            errorJoiner.add(ordDirOrderAllocationPool.getGoodsCode());
            ordDirOrderAllocationPoolMapper.deleteByPrimaryKey(ordDirOrderAllocationPool.getId());
            return;
        }
        if (Objects.isNull(orderGoodsOut.getDistributionSpecification())) {
            log.error("门店{}" + ordDirOrderAllocationPool.getStoreCode() + "订单调配商品" + orderGoodsOut.getGoodsName() + "(" + orderGoodsOut.getGoodsCode() + ")配送规格为空");
            errorJoiner.add(ordDirOrderAllocationPool.getGoodsCode());
            ordDirOrderAllocationPoolMapper.deleteByPrimaryKey(ordDirOrderAllocationPool.getId());
            return;
        }
        if (Objects.isNull(orderGoodsOut.getDistributionUnitPrice())) {
            log.error("门店{}" + ordDirOrderAllocationPool.getStoreCode() + "订单调配商品" + orderGoodsOut.getGoodsName() + "(" + orderGoodsOut.getGoodsCode() + ")配送价异常为空");
            errorJoiner.add(ordDirOrderAllocationPool.getGoodsCode());
            ordDirOrderAllocationPoolMapper.deleteByPrimaryKey(ordDirOrderAllocationPool.getId());
            return;
        }
        List<String> combinationTypeCodeList = Lists.newArrayList(orderGoodsOut.getStockCode(), DistributionWaysEnum.getTypeByName(orderGoodsOut.getDistributionWay()), StringUtils.isNotBlank(GoodsTypeEnum.getCodeByName(orderGoodsOut.getGoodsType())) ? GoodsTypeEnum.getCodeByName(orderGoodsOut.getGoodsType()) : orderGoodsOut.getGoodsType());
        // 查找订单类型配置
        DirOrderTypeConfig dirOrderTypeConfig = orderConfigHandle.getOneByCombinationTypeValueList(ordDirOrderAllocationPool.getStoreCode(), bizOrgCode, combinationTypeCodeList);
        if (Objects.isNull(dirOrderTypeConfig)) {
            log.error("门店{}" + ordDirOrderAllocationPool.getStoreCode() + "订单调配商品{}查询订单类型配置入参：仓位{}，" +
                    "配送方式{}，品类属性{}，组织{}不存在的订单类型配置", ordDirOrderAllocationPool.getStoreCode(), orderGoodsOut.getStockCode(), orderGoodsOut.getDistributionWay(), orderGoodsOut.getGoodsType(), bizOrgCode);
            errorJoiner.add(ordDirOrderAllocationPool.getGoodsCode());
            ordDirOrderAllocationPoolMapper.deleteByPrimaryKey(ordDirOrderAllocationPool.getId());
            return;
        }
        StoreDelivery storeDelivery = deliveryTypeMap.get(dirOrderTypeConfig.getOrderPeriod());
        if (Objects.isNull(storeDelivery)) {
            log.error("门店{}订单调配商品{}配送信息{}为空", ordDirOrderAllocationPool.getStoreCode(), ordDirOrderAllocationPool.getGoodsCode(), dirOrderTypeConfig.getOrderPeriod());
            errorJoiner.add(ordDirOrderAllocationPool.getGoodsCode());
            ordDirOrderAllocationPoolMapper.deleteByPrimaryKey(ordDirOrderAllocationPool.getId());
            return;
        }
        OrderCycleDeliveryOut storeDeliveryType = shoppingCartService.getStoreDeliveryInfoType(storeDelivery);
        if (Objects.isNull(storeDeliveryType)) {
            log.error("门店{}订单调配商品{}要货周期为空", ordDirOrderAllocationPool.getStoreCode(), ordDirOrderAllocationPool.getGoodsCode());
            errorJoiner.add(ordDirOrderAllocationPool.getGoodsCode());
            ordDirOrderAllocationPoolMapper.deleteByPrimaryKey(ordDirOrderAllocationPool.getId());
            return;
        }
        if (StringUtils.isBlank(dirOrderTypeConfig.getTruncationTimePoint())) {
            log.error("门店{}订单调配商品{}截单时间节点为空", ordDirOrderAllocationPool.getStoreCode(), ordDirOrderAllocationPool.getGoodsCode());
            errorJoiner.add(ordDirOrderAllocationPool.getGoodsCode());
            ordDirOrderAllocationPoolMapper.deleteByPrimaryKey(ordDirOrderAllocationPool.getId());
            return;
        }
        String storeDeliveryTruncationTime = shoppingCartService.getStoreDeliveryTruncationTime(storeDeliveryType.getDistributionCycle(), storeDeliveryType.getDeliveryDailyCycle(), dirOrderTypeConfig.getTruncationTimePoint());
        if (StringUtils.isBlank(storeDeliveryTruncationTime)) {
            log.error("门店{}订单调配商品{}截单时间计算为空，请检查相关配置", ordDirOrderAllocationPool.getStoreCode(), ordDirOrderAllocationPool.getGoodsCode());
            errorJoiner.add(ordDirOrderAllocationPool.getGoodsCode());
            ordDirOrderAllocationPoolMapper.deleteByPrimaryKey(ordDirOrderAllocationPool.getId());
            return;
        } else {
            storeDeliveryTruncationTime = storeDeliveryTruncationTime + ":00";
        }
        LocalDateTime truncationTime = DateUtils.parseTime(storeDeliveryTruncationTime);
        if (truncationTime.toLocalDate().compareTo(LocalDate.now()) != NumberUtil.INTEGER_ZERO) {
            log.error("门店{}订单调配商品{}截单时间{}不在当天", ordDirOrderAllocationPool.getStoreCode(), ordDirOrderAllocationPool.getGoodsCode(), storeDeliveryTruncationTime);
            errorJoiner.add(ordDirOrderAllocationPool.getGoodsCode());
            ordDirOrderAllocationPoolMapper.deleteByPrimaryKey(ordDirOrderAllocationPool.getId());
            return;
        }
        // 截单时间
        ordDirOrderAllocationPool.setTruncationDateTime(DateUtils.parseTime(storeDeliveryTruncationTime));
        // 单日平均销售数量
        if (Objects.isNull(ordDirOrderAllocationPool.getAverageDailySalesQuantity())) {
            ordDirOrderAllocationPool.setAverageDailySalesQuantity(BigDecimal.ZERO);
        }
        // 计算订货量和叫货周期
        BigDecimal computeOrderQuantity = null;
        // 订货量增量值
        BigDecimal computeOrderQuantityIncrement = BigDecimal.ONE;
        // 获取中分类
        String middleSort = orderGoodsOut.getSort().substring(0, 4);
        // 是否命中1401,1403
        boolean isExistFlag = false;
        // 1401，1403 redis写死中分类
        String orderAllocationPoolSortValue = redisService.get(SystemConstant.DIR_ORD_ALLOCATION_POOL_SORT_KEY);
        if (StringUtils.isNotBlank(middleSort) && StringUtils.isNotBlank(orderAllocationPoolSortValue)) {
            String[] orderAllocationPoolSortArray = orderAllocationPoolSortValue.split(SystemConstant.COMMA);
            // true：1401,1403；false：非1401,1403
            isExistFlag = Arrays.stream(orderAllocationPoolSortArray).anyMatch(s -> s.equals(middleSort));
            // 匹配到1401,1403，增量为0；否则原值1
            computeOrderQuantityIncrement = isExistFlag ? BigDecimal.ZERO : computeOrderQuantityIncrement;
        }
        // 直营日配，计算订货数量=门店单品单日平均销售数量+1
        if (StoreDeliveryCycleEnum.DAY_WITH.getCode().equals(storeDeliveryType.getDistributionCycle())) {
            computeOrderQuantity = ordDirOrderAllocationPool.getAverageDailySalesQuantity().add(computeOrderQuantityIncrement);
        }
        // 单日配，双日配，按日的中转品：
        if (StoreDeliveryCycleEnum.ODD_DAYS.getCode().equals(storeDeliveryType.getDistributionCycle())
                || StoreDeliveryCycleEnum.ALTERNATE_DAYS.getCode().equals(storeDeliveryType.getDistributionCycle())
                || SystemConstant.DELIVERY_BY_DAY.equals(storeDeliveryType.getDistributionCycle())) {
            // 1401,1403
            if (isExistFlag) {
                // 计算订货数量=门店单品单日平均销售数量*2
                computeOrderQuantity = ordDirOrderAllocationPool.getAverageDailySalesQuantity().multiply(new BigDecimal(NumberUtil.INTEGER_TWO));
            } else {
                // 计算订货数量=（门店单品单日平均销售数量*2）+1
                computeOrderQuantity = ordDirOrderAllocationPool.getAverageDailySalesQuantity().multiply(new BigDecimal(NumberUtil.INTEGER_TWO)).add(computeOrderQuantityIncrement);
            }
        }
        if (Objects.isNull(computeOrderQuantity)) {
            log.error("门店{}订单调配商品{}计算订货量计算为空", ordDirOrderAllocationPool.getStoreCode(), ordDirOrderAllocationPool.getGoodsCode());
            errorJoiner.add(ordDirOrderAllocationPool.getGoodsCode());
            ordDirOrderAllocationPoolMapper.deleteByPrimaryKey(ordDirOrderAllocationPool.getId());
            return;
        }
        ordDirOrderAllocationPool.setComputeOrderQuantity(computeOrderQuantity.setScale(0, RoundingMode.UP));
        // 计算叫货周期
        String deliveryCycle = StoreDeliveryCycleEnum.getNameByCode(storeDeliveryType.getDistributionCycle());
        if (StringUtils.isBlank(deliveryCycle) && SystemConstant.DELIVERY_BY_DAY.equals(storeDeliveryType.getDistributionCycle())) {
            deliveryCycle = storeDeliveryType.getDeliveryDailyCycle();
        }
        // 校验基础陈列量
        if (Objects.isNull(ordDirOrderAllocationPool.getBaseDisplayQuantity())) {
            log.error("门店{}订单调配商品{}基础陈列量为空", ordDirOrderAllocationPool.getStoreCode(), ordDirOrderAllocationPool.getGoodsCode());
            errorJoiner.add(ordDirOrderAllocationPool.getGoodsCode());
            ordDirOrderAllocationPoolMapper.deleteByPrimaryKey(ordDirOrderAllocationPool.getId());
            return;
        }
        // 建议订货数量，基础陈列量与计算订货数量取大值
        if (ordDirOrderAllocationPool.getBaseDisplayQuantity().compareTo(ordDirOrderAllocationPool.getComputeOrderQuantity()) == NumberUtil.INTEGER_ONE) {
            ordDirOrderAllocationPool.setRecommendOrderQuantity(ordDirOrderAllocationPool.getBaseDisplayQuantity());
        } else {
            ordDirOrderAllocationPool.setRecommendOrderQuantity(ordDirOrderAllocationPool.getComputeOrderQuantity());
        }
        // 门店下单数量
        Map<String, BigDecimal> goodsQuantityMap = truncationTimeGoodsQuantityMap.get(storeDeliveryTruncationTime);
        if (null == goodsQuantityMap) {
            goodsQuantityMap = orderDetailHandle.findStoreOrderQuantityByTruncationTime(ordDirOrderAllocationPool.getTruncationDateTime(), ordDirOrderAllocationPool.getStoreCode());
            truncationTimeGoodsQuantityMap.put(storeDeliveryTruncationTime, goodsQuantityMap);
        }
        // 下单量为空计算上当0处理
        BigDecimal orderQuantity = Objects.isNull(goodsQuantityMap.get(ordDirOrderAllocationPool.getGoodsCode())) ? BigDecimal.ZERO : goodsQuantityMap.get(ordDirOrderAllocationPool.getGoodsCode());
        // 存放真实值
        ordDirOrderAllocationPool.setStoreOrderQuantity(goodsQuantityMap.get(ordDirOrderAllocationPool.getGoodsCode()));
        // 计算补单量 = 建议订货数量 - 门店下单数量
        BigDecimal supplementQuantity = ordDirOrderAllocationPool.getRecommendOrderQuantity().subtract(orderQuantity);
        // 补单包装量
        BigDecimal supplementPackageQuantity;
        // 非正整数时统一处理为0
        if (supplementQuantity.compareTo(BigDecimal.ZERO) < NumberUtil.INTEGER_ZERO) {
            supplementQuantity = BigDecimal.ZERO;
            supplementPackageQuantity = BigDecimal.ZERO;
        } else {
            // 不为整数倍，向上取整重新计算补单量
            if (supplementQuantity.intValue() % orderGoodsOut.getDistributionSpecification().getQpc() != 0) {
                supplementPackageQuantity = supplementQuantity.divide(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_ZERO, RoundingMode.UP);
                supplementQuantity = supplementPackageQuantity.multiply(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()));
            } else {
                supplementPackageQuantity = supplementQuantity.divide(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_ZERO, RoundingMode.DOWN);
            }
        }
        ordDirOrderAllocationPool.setSupplementQuantity(supplementQuantity);
        ordDirOrderAllocationPool.setSupplementPackageQuantity(supplementPackageQuantity);
        ordDirOrderAllocationPool.setDeliveryCycle(deliveryCycle);
        ordDirOrderAllocationPool.setUpdater(SystemConstant.SYSTEM_USER);
        ordDirOrderAllocationPool.setUpdateTime(LocalDateTime.now());
        ordDirOrderAllocationPoolMapper.updateByPrimaryKey(ordDirOrderAllocationPool);
    }

    @Override
    public List<OrdDirOrderAllocationPool> findStoreTruncationDateTimeList() {
        return ordDirOrderAllocationPoolMapper.findStoreTruncationDateTimeList();
    }

    @Override
    public List<OrdDirOrderAllocationPool> findListByStoreCodeAndTruncationDateTime(String storeCode, LocalDateTime truncationDateTime) {
        return ordDirOrderAllocationPoolMapper.findListByStoreCodeAndTruncationDateTime(storeCode, truncationDateTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toHistory(List<OrdDirOrderAllocationPool> ordDirOrderAllocationPoolList, String storeCode, LocalDateTime truncationDateTime) {
        OrdDirOrderAllocationPoolHistory checkOne = new OrdDirOrderAllocationPoolHistory();
        checkOne.setStoreCode(storeCode);
        checkOne.setTruncationDateTime(truncationDateTime);
        int count = ordDirOrderAllocationPoolHistoryMapper.selectCount(checkOne);
        if (count == 0) {
            ordDirOrderAllocationPoolList.forEach(ordDirOrderAllocationPool -> {
                OrdDirOrderAllocationPoolHistory ordDirOrderAllocationPoolHistory = new OrdDirOrderAllocationPoolHistory();
                BeanUtils.copy(ordDirOrderAllocationPool, ordDirOrderAllocationPoolHistory);
                ordDirOrderAllocationPoolHistoryMapper.insert(ordDirOrderAllocationPoolHistory);
            });
        }
        OrdDirOrderAllocationPool ordDirOrderAllocationPool = new OrdDirOrderAllocationPool();
        ordDirOrderAllocationPool.setStoreCode(storeCode);
        ordDirOrderAllocationPool.setTruncationDateTime(truncationDateTime);
        ordDirOrderAllocationPoolMapper.delete(ordDirOrderAllocationPool);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> importAllocationPoolDetail(String fileId, String loginUsername) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (Objects.isNull(bytes)) {
            throw new BusinessException("无效的Excel模板");
        }
        List<String> areaCodeList = empowerGroupHandle.findEmpowerGroupsByType(UserUtil.getBizOrgCode(), "operation");
        Map<String, StoreInfoOut> checkStoreMap = null;
        if (CollectionUtils.isNotEmpty(areaCodeList)) {
            StoreInfoIn storeInfoIn = new StoreInfoIn();
            storeInfoIn.setAreaCodes(areaCodeList);
            List<StoreInfoOut> storeInfoOutList = storeCenterService.findByAreaCodes(storeInfoIn);
            checkStoreMap = new HashMap<>();
            for (StoreInfoOut storeInfoOut : storeInfoOutList) {
                checkStoreMap.put(storeInfoOut.getErpStoreCode(), storeInfoOut);
            }
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OrdDirOrderAllocationPoolListener listener = new OrdDirOrderAllocationPoolListener(this, checkStoreMap);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdAllocationPoolGoodsVO.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(0).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > 0) {
            return Response.error(listener.getImportErrorMessage(totalErrorMap));
        }
        listener.getImportList().forEach(ordDirOrderAllocationPool -> {
            ordDirOrderAllocationPool.setUpdater(loginUsername);
            ordDirOrderAllocationPool.setUpdateTime(LocalDateTime.now());
            ordDirOrderAllocationPoolMapper.updateByPrimaryKey(ordDirOrderAllocationPool);
        });
        return Response.success("导入成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<UpdateAllocationPoolIn> updateAllocationPoolInList, String loginUsername) {
        updateAllocationPoolInList.forEach(updateAllocationPoolIn -> {
            OrdDirOrderAllocationPool existOne = ordDirOrderAllocationPoolMapper.selectByPrimaryKey(updateAllocationPoolIn.getId());
            if (Objects.isNull(existOne)) {
                throw new BusinessException("不存在的调配记录");
            }
            LocalDateTime truncationDateTime = existOne.getTruncationDateTime();
            Duration duration = Duration.between(LocalDateTime.now(), truncationDateTime);
            if (duration.toMinutes() <= 60) {
                throw new BusinessException(existOne.getStoreCode() + "门店" + existOne.getGoodsCode() + "商品当前时间距截单时间不允许更新补单量");
            }
            if (updateAllocationPoolIn.getSupplementQuantity().compareTo(BigDecimal.ZERO) < NumberUtil.INTEGER_ZERO) {
                throw new BusinessException(existOne.getStoreCode() + "门店" + existOne.getGoodsCode() + "商品补单量不是0/规格整数倍，请检查！");
            }
            if (updateAllocationPoolIn.getSupplementQuantity().compareTo(BigDecimal.ZERO) > NumberUtil.INTEGER_ZERO) {
                int remainder = updateAllocationPoolIn.getSupplementQuantity().intValue() % existOne.getDistributionSpecNum();
                if (remainder != NumberUtil.INTEGER_ZERO) {
                    throw new BusinessException(existOne.getStoreCode() + "门店" + existOne.getGoodsCode() + "商品补单量不是0/规格整数倍，请检查！");
                }
            }
            existOne.setSupplementQuantity(updateAllocationPoolIn.getSupplementQuantity());
            existOne.setSupplementPackageQuantity(existOne.getSupplementQuantity().divide(new BigDecimal(existOne.getDistributionSpecNum()), NumberUtil.INTEGER_ZERO, RoundingMode.DOWN));
            existOne.setStatus(OrderAllocationPoolStatusEnum.DONE.getKey());
            existOne.setRemark(updateAllocationPoolIn.getRemark());
            existOne.setUpdater(loginUsername);
            existOne.setUpdateTime(LocalDateTime.now());
            ordDirOrderAllocationPoolMapper.updateByPrimaryKey(existOne);
        });
    }

    @Override
    public Page<OrdDirOrderAllocationPoolPageOut> findListForPage(QueryAllocationPoolPageIn queryAllocationPoolPageIn) {
        List<String> areaCodeList = empowerGroupHandle.findEmpowerGroupsByType(queryAllocationPoolPageIn.getBizOrgCode(), "operation");
        List<String> queryAreaCodeList = Lists.newArrayList();
//        if (CollectionUtils.isEmpty(areaCodeList) && StringUtils.isBlank(queryAllocationPoolPageIn.getStoreArea())) {
//            queryAllocationPoolPageIn.setStoreCodeList(null);
//        } else if (CollectionUtils.isEmpty(areaCodeList) && StringUtils.isNotBlank(queryAllocationPoolPageIn.getStoreArea())) {
//            queryAreaCodeList.add(queryAllocationPoolPageIn.getStoreArea());
//            queryAllocationPoolPageIn.setStoreCodeList(queryAreaCodeList);
//        } else {
//            if (StringUtils.isNotEmpty(queryAllocationPoolPageIn.getStoreArea())) {
//                boolean areaIsExist = areaCodeList.stream().anyMatch(areaCode -> areaCode.equals(queryAllocationPoolPageIn.getStoreArea()));
//                if (!areaIsExist) {
//                    return new Page<>();
//                } else {
//                    queryAreaCodeList.add(queryAllocationPoolPageIn.getStoreArea());
//                }
//            } else {
//                queryAreaCodeList.addAll(areaCodeList);
//            }
//        }
        if (CollectionUtils.isEmpty(areaCodeList) && StringUtils.isBlank(queryAllocationPoolPageIn.getStoreArea())) {
            queryAllocationPoolPageIn.setStoreCodeList(null);
        } else if (CollectionUtils.isEmpty(areaCodeList) && StringUtils.isNotBlank(queryAllocationPoolPageIn.getStoreArea())) {
            queryAreaCodeList.add(queryAllocationPoolPageIn.getStoreArea());
            queryAllocationPoolPageIn.setStoreCodeList(queryAreaCodeList);
        } else {
            StoreAreaIn storeAreaIn = new StoreAreaIn();
            storeAreaIn.setAreaCodes(areaCodeList);
            storeAreaIn.setBizOrgCode(queryAllocationPoolPageIn.getBizOrgCode());
            List<String> childAreasCodeList = storeCenterService.findChildAreasByCodes(storeAreaIn);
            if (StringUtils.isNotBlank(queryAllocationPoolPageIn.getStoreArea())) {
                Optional<String> areaOptional = childAreasCodeList.stream().filter(checkAreaCode -> checkAreaCode.equals(queryAllocationPoolPageIn.getStoreArea())).findFirst();
                if (areaOptional.isPresent()) {
                    queryAreaCodeList.add(queryAllocationPoolPageIn.getStoreArea());
                    queryAllocationPoolPageIn.setStoreCodeList(queryAreaCodeList);
                } else {
                    return new Page<>();
                }
            } else {
                queryAreaCodeList.addAll(childAreasCodeList);
                queryAllocationPoolPageIn.setStoreCodeList(queryAreaCodeList);
            }
        }
        if (CollectionUtils.isNotEmpty(queryAreaCodeList)) {
            StoreInfoIn storeInfoIn = new StoreInfoIn();
            storeInfoIn.setAreaCodes(queryAreaCodeList);
            storeInfoIn.setBizOrgCode(queryAllocationPoolPageIn.getBizOrgCode());
            List<StoreInfoOut> storeInfoOutList = storeCenterService.findByAreaCodeList(storeInfoIn);
            List<String> storeCodeList = new ArrayList<>();
            storeInfoOutList.forEach(storeInfoOut -> storeCodeList.add(storeInfoOut.getErpStoreCode()));
            queryAllocationPoolPageIn.setStoreCodeList(storeCodeList);
        }
//        String loginBizOrgCode = queryAllocationPoolPageIn.getBizOrgCode();
//        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(loginBizOrgCode);
//        Optional<StockInfoOut> anyOneOptional = authOrgStockMap.values().stream().filter(stockInfoOut -> !loginBizOrgCode.equals(stockInfoOut.getBizOrgCode())).findFirst();
//        if (!anyOneOptional.isPresent()) {
//            queryAllocationPoolPageIn.setBizOrgCode("");
//        }
        if (StringUtils.isNotBlank(queryAllocationPoolPageIn.getSortCodes())) {
            List<String> sortCodeList = Arrays.asList(queryAllocationPoolPageIn.getSortCodes().split(SystemConstant.COMMA));
            queryAllocationPoolPageIn.setSortCodeList(sortCodeList);
        }
        List<OrdDirOrderAllocationPoolPageOut> list = ordDirOrderAllocationPoolMapper.findListByPage(queryAllocationPoolPageIn);
        list.forEach(ordDirOrderAllocationPoolPageOut -> ordDirOrderAllocationPoolPageOut.setStatusStr(OrderAllocationPoolStatusEnum.getValueByKey(ordDirOrderAllocationPoolPageOut.getStatus())));
        Page<OrdDirOrderAllocationPoolPageOut> page = new Page<>(queryAllocationPoolPageIn);
        page.setList(list);
        return page;
    }

    @Override
    public String exportAllocationPool(QueryAllocationPoolPageIn queryAllocationPoolPageIn) {
        //分页查询出货单明细
        Page<OrdDirOrderAllocationPoolPageOut> page = this.findListForPage(queryAllocationPoolPageIn);
        //获取分页数据
        List<OrdDirOrderAllocationPoolPageOut> detaiList = page.getList();
        //导出Excel实体
        List<ExportOrdDirOrderAllocationPool> exportDetails = new ArrayList<>();
        for (OrdDirOrderAllocationPoolPageOut detailOut : detaiList) {
            //导出实体
            ExportOrdDirOrderAllocationPool exportDetail = new ExportOrdDirOrderAllocationPool();
            BeanUtils.copy(detailOut, exportDetail);
            if (Objects.nonNull(detailOut.getTruncationDateTime())) {
                exportDetail.setTruncationDateTimeStr(DateUtils.format(detailOut.getTruncationDateTime()));
            }
            exportDetail.setUpdateTimeStr(DateUtils.format(detailOut.getUpdateTime()));
            //添加excel导出结果集
            exportDetails.add(exportDetail);
        }
        //导入excel标题
        String title = "订单调配";
        byte[] fileBytesByData = FileExportUtil.getFileBytesByData(
                exportDetails,
                title,
                title,
                ExportOrdDirOrderAllocationPool.class,
                true);

        return fileService.uploadFile(
                title + ".xlsx",
                fileBytesByData,
                SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    @Override
    public GoodsSortNodeOut findAllocationPoolSortTree(String bizOrgCode) {
        GoodsSortNodeOut rootNode = new GoodsSortNodeOut();
        rootNode.setSortName("根节点");
        rootNode.setChildNodes(Lists.newArrayList());
        List<String> poolSortList = ordDirOrderAllocationPoolMapper.findDirOrderAllocationPoolSortList(bizOrgCode);
        if (CollectionUtils.isEmpty(poolSortList)) {
            return rootNode;
        }
        List<String> bigSortList = poolSortList.stream().map(s -> s.substring(0, 2)).collect(Collectors.toList());
        Map<String, String> poolBigSortMap = bigSortList.stream().collect(Collectors.toMap(s -> s, Function.identity(), (v1, v2) -> v1));
        List<GoodsSortOut> goodsSortList = orgSortMapper.findSortListByBizOrgCode(bizOrgCode);
        List<GoodsSortNodeOut> sortTreeList = Lists.newArrayList();
        goodsSortList.forEach(goodsSortOut -> {
            if (!poolBigSortMap.containsKey(goodsSortOut.getSort())) {
                return;
            }
            // 封装父几点
            GoodsSortNodeOut parentSortNodeOut = new GoodsSortNodeOut();
            BeanUtils.copy(goodsSortOut, parentSortNodeOut);
            parentSortNodeOut.setChildNodes(Lists.newArrayList());
            this.handleChildNode(goodsSortList, parentSortNodeOut);
            sortTreeList.add(parentSortNodeOut);
        });
        rootNode.getChildNodes().addAll(sortTreeList);
        return rootNode;

    }

    public void handleChildNode(List<GoodsSortOut> goodsSortList, GoodsSortNodeOut parentSortNodeOut) {
        goodsSortList.forEach(goodsSortOut -> {
            // 过滤当前父节点
            if (goodsSortOut.getSort().equals(parentSortNodeOut.getSort())) {
                return;
            }
            if (!parentSortNodeOut.getSort().equals(goodsSortOut.getParentSortCode())) {
                return;
            }
            // 封装子节点
            GoodsSortNodeOut childSortNodeOut = new GoodsSortNodeOut();
            BeanUtils.copy(goodsSortOut, childSortNodeOut);
            childSortNodeOut.setChildNodes(Lists.newArrayList());
            // 子节点放进父节点
            parentSortNodeOut.getChildNodes().add(childSortNodeOut);
            // 递归找子节点下的子节点
            this.handleChildNode(goodsSortList, childSortNodeOut);
        });
    }
}
