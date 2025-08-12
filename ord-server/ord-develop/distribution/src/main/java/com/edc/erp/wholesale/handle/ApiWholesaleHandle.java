package com.edc.erp.wholesale.handle;

import com.edc.erp.common.model.in.customer.QueryClientDistInfoIn;
import com.edc.erp.common.model.in.goods.QuerySaleGoodsInfoIn;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.rpc.SaleGoodsInfoClient;
import com.edc.erp.common.service.ClientDistInfoService;
import com.edc.erp.enumeration.WholesaleOrderTypeEnum;
import com.edc.erp.wholesale.enumeration.WholesaleTypeEnum;
import com.edc.erp.wholesale.model.in.ApiWholesaleDetailIn;
import com.edc.erp.wholesale.model.in.ApiWholesaleOrderIn;
import com.edc.erp.wholesale.model.out.ReadyHandleWholesaleApiOut;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName ApiWholesaleHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/10 10:45
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiWholesaleHandle {

    private final SaleGoodsInfoClient saleGoodsInfoClient;
    private final ApiWholesaleReturnsHandle apiWholesaleReturnsHandle;
    private final ClientDistInfoService clientDistInfoService;

    private final ApiWholesaleShipmentHandle apiWholesaleShipmentHandle;

    @Transactional(rollbackFor = Exception.class)
    public void handleApiTask(ApiWholesaleOrderIn apiWholesaleOrderIn) {
        ReadyHandleWholesaleApiOut readyHandleWholesaleApiOut = this.beforeHandle(apiWholesaleOrderIn);
        if (WholesaleTypeEnum.ORDER.getOrderType().equals(apiWholesaleOrderIn.getType())) {
            Long id = apiWholesaleShipmentHandle.createWholesaleShipment(readyHandleWholesaleApiOut);
            apiWholesaleShipmentHandle.auditWholesaleShipment(id, readyHandleWholesaleApiOut.getSaleGoodsInfoOutMap(), readyHandleWholesaleApiOut.getClientDistInfoOut().getClientId());
        }
        if (WholesaleTypeEnum.RETURN.getOrderType().equals(apiWholesaleOrderIn.getType())) {
            Long id = apiWholesaleReturnsHandle.createWholesaleReturns(readyHandleWholesaleApiOut);
            apiWholesaleReturnsHandle.auditWholesaleReturns(id, readyHandleWholesaleApiOut.getSaleGoodsInfoOutMap(), readyHandleWholesaleApiOut.getClientDistInfoOut().getClientId());
        }
    }

    private ReadyHandleWholesaleApiOut beforeHandle(ApiWholesaleOrderIn apiWholesaleOrderIn) {
        List<String> goodsCodeList = apiWholesaleOrderIn.getDetailList().stream().map(ApiWholesaleDetailIn::getGoodsCode).collect(Collectors.toList());
        //调用rpc获取商品信息以及包装规格、批发价、库存价
        QuerySaleGoodsInfoIn querySaleGoodsInfoIn = new QuerySaleGoodsInfoIn();
        querySaleGoodsInfoIn.setBizOrgCode(apiWholesaleOrderIn.getBizOrgCode());
        querySaleGoodsInfoIn.setGoodsCodeList(goodsCodeList);
        String wholesaleOrderType;
        if (WholesaleTypeEnum.ORDER.getOrderType().equals(apiWholesaleOrderIn.getType())) {
            wholesaleOrderType = WholesaleOrderTypeEnum.OUT.getCode();
        } else {
            wholesaleOrderType = WholesaleOrderTypeEnum.RETURNS.getCode();
        }
        querySaleGoodsInfoIn.setWholesaleOrderType(wholesaleOrderType);
        querySaleGoodsInfoIn.setClientCode(apiWholesaleOrderIn.getClientCode());
//        querySaleGoodsInfoIn.setStockId(stockInfoOut.getId());
        querySaleGoodsInfoIn.setStockCode(apiWholesaleOrderIn.getStockCode());
        querySaleGoodsInfoIn.setWarehouseCode(apiWholesaleOrderIn.getWarehouseCode());
        querySaleGoodsInfoIn.setPriceGroupCode(apiWholesaleOrderIn.getPriceGroupCode());
        //查询商品信息
        Response<List<SaleGoodsInfoOut>> response = saleGoodsInfoClient.findGoodsInfo(querySaleGoodsInfoIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error("三方单据{}创建批发出查询商品异常", apiWholesaleOrderIn.getSourceNo(), response.getMessage());
            throw new BusinessException(response.getMessage());
        }
        Map<String, SaleGoodsInfoOut> saleGoodsInfoOutMap = response.getData().stream().collect(Collectors.toMap(SaleGoodsInfoOut::getGoodsCode, Function.identity()));
        ClientDistInfoOut clientDistInfoOut = this.initClientInfo(apiWholesaleOrderIn);
        ReadyHandleWholesaleApiOut readyHandleWholesaleApiOut = new ReadyHandleWholesaleApiOut();
        readyHandleWholesaleApiOut.setApiWholesaleOrderIn(apiWholesaleOrderIn);
        readyHandleWholesaleApiOut.setClientDistInfoOut(clientDistInfoOut);
        readyHandleWholesaleApiOut.setSaleGoodsInfoOutMap(saleGoodsInfoOutMap);
//        readyHandleWholesaleApiOut.setOldWholesaleShipmentNo(apiWholesaleOrderIn);
        return readyHandleWholesaleApiOut;
    }

    private ClientDistInfoOut initClientInfo(ApiWholesaleOrderIn apiWholesaleOrderIn) {
        String sourceNo = apiWholesaleOrderIn.getSourceNo();
        String clientCode = apiWholesaleOrderIn.getClientCode();
        // 查询客户信息
        ClientDistInfoOut clientDistInfo = null;
        // 来源省烟草
        if (StringUtils.isNotBlank(sourceNo) && sourceNo.startsWith("HS")) {

            clientDistInfo = new ClientDistInfoOut();
            clientDistInfo.setClientCode(clientCode);
            clientDistInfo.setPriceGroupCode(apiWholesaleOrderIn.getPriceGroupCode());
            clientDistInfo.setClientId(apiWholesaleOrderIn.getClientId());
            clientDistInfo.setAddressDetail(apiWholesaleOrderIn.getAddressDetail());
        }
        // 来源中科
        if (StringUtils.isNotBlank(sourceNo) && sourceNo.startsWith("YH")) {
            List<ClientDistInfoOut> clientDistInfos = clientDistInfoService.findClientDistInfo(new QueryClientDistInfoIn(clientCode, apiWholesaleOrderIn.getBizOrgCode()));
            if (CollectionUtils.isEmpty(clientDistInfos)) {
                log.error("三方单据{}创建批发出查询客户代码{}-----收货信息校验异常", sourceNo, clientCode);
                throw new BusinessException(sourceNo + "创建批发出查询客户" + clientCode + "与收货人校验失败");
            }
            clientDistInfo = clientDistInfos.get(0);
        }
        return clientDistInfo;
    }


}
