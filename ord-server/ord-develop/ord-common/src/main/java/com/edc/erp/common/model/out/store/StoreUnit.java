package com.edc.erp.common.model.out.store;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


/**
 * @description:  门店单元信息
 * @author fxw
 * @since 2022/09/17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sc_store_unit")
public class StoreUnit {

    public StoreUnit(String unitCode, String orgCode, Integer isDelete){
        this.unitCode = unitCode;
        this.orgCode = orgCode;
        this.isDelete = isDelete;
    }
    @ApiModelProperty(value = "单元id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer unitId;

    @ApiModelProperty(value = "单元代码")
    private String unitCode;

    @ApiModelProperty(value = "单元名称")
    private String unitName;

    @ApiModelProperty(value = "门店类型")
    private String storeProperty;

    @ApiModelProperty(value = "合作方式")
    private String businessWay;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改者")
    private String updater;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "组织")
    private String orgCode;

    @ApiModelProperty(value = "")
    private Integer isEnable;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;
    @ApiModelProperty(value = "业务组织代码")
    private String bizOrgCode;

}
