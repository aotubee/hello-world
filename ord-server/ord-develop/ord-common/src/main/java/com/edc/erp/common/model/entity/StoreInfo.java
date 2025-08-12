package com.edc.erp.common.model.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 门店基础信息
 * </p>
 *
 * @author gusiyuan
 * @since 2021-05-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sc_store_info")
public class StoreInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "门店id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer storeId;

    @ApiModelProperty(value = "门店编码")
    private String storeCode;

    @ApiModelProperty(value = "erp门店代码")
    private String erpStoreCode;

    @ApiModelProperty(value = "门店名称")
    private String storeName;

    @ApiModelProperty(value = "公司代码")
    private String orgCode;

    @ApiModelProperty(value = "业务组织")
    private String bizOrgCode;

    @ApiModelProperty(value = "所属部门")
    private String deptCode;

    @ApiModelProperty(value = "所属区域")
    private String belongArea;

    @ApiModelProperty(value = "默认配送中心")
    private String defaultDistributionCenter;

    @ApiModelProperty(value = "门店状态")
    private String storeStatus;

    @ApiModelProperty(value = "门店业态")
    private String storeType;

    @ApiModelProperty(value = "门店类型")
    private String storeProperty;

    @ApiModelProperty(value = "经营方式")
    private String businessWay;

    @ApiModelProperty(value = "所属配送方案")
    private String alcSchemeCode;

    @ApiModelProperty(value = "所属价格组")
    private String priceSchemeCode;

    @ApiModelProperty(value = "所属经营方案")
    private String saleSchemeCode;

    @ApiModelProperty(value = "开始营业日期")
    private LocalDate openingDate;

    @ApiModelProperty(value = "闭店日期")
    private LocalDate closingDate;

    @ApiModelProperty(value = "暂停营业开始时间")
    private LocalDate closeUpBeginDate;

    @ApiModelProperty(value = "改造结束时间")
    private LocalDate remouldEndDate;

    @ApiModelProperty(value = "改造开始时间")
    private LocalDate remouldBeginDate;

    @ApiModelProperty(value = "暂停营业结束时间")
    private LocalDate closeUpEndDate;

    @ApiModelProperty(value = "市场类型")
    private String marketCategory;

    @ApiModelProperty(value = "基盘编码")
    private String basalDiscCode;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改者")
    private String updater;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;

    @ApiModelProperty(value = "是否删除")
    private Integer iSAllot;

    @ApiModelProperty(value = "是否删除")
    private Integer iSAllotDis;

    public StoreInfo(String erpStoreCode, Integer isDelete) {
        this.erpStoreCode = erpStoreCode;
        this.isDelete = isDelete;
    }
}
