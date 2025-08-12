package com.edc.erp.common.entity;


import io.swagger.annotations.ApiModel;
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


/**
 * (AdministrativeAreaInfo)实体类
 *
 * @author lx
 * @since 2022-10-19 16:06:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "administrative_area_info")
@ApiModel(value = "AdministrativeAreaInfo", description = "")
public class AdministrativeAreaInfo implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "areaId", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer areaId;

    /** 行政代码 */
    @ApiModelProperty(name = "areaCode", value = "行政代码")
    private String areaCode;

    /** 父级编码 */
    @ApiModelProperty(name = "pAreaCode", value = "父级编码")
    private String pAreaCode;

    /** 名称 */
    @ApiModelProperty(name = "areaName", value = "名称")
    private String areaName;

    /** 层级 */
    @ApiModelProperty(name = "areaType", value = "层级")
    private Integer areaType;

    /** 状态,0:启用，1：禁用 */
    @ApiModelProperty(name = "areaStatus", value = "状态,0:启用，1：禁用")
    private Integer areaStatus;

    /** 本级编码 */
    @ApiModelProperty(name = "areaLevelCode", value = "本级编码")
    private String areaLevelCode;

    /** 邮政编码 */
    @ApiModelProperty(name = "zipCode", value = "邮政编码")
    private Integer zipCode;

    /** 区号 */
    @ApiModelProperty(name = "cityCode", value = "区号")
    private String cityCode;

    /** 简称 */
    @ApiModelProperty(name = "shortName", value = "简称")
    private String shortName;

    /** 拼音 */
    @ApiModelProperty(name = "pinyin", value = "拼音")
    private String pinyin;

    /** 经度 */
    @ApiModelProperty(name = "lng", value = "经度")
    private Object lng;

    /** 纬度 */
    @ApiModelProperty(name = "lat", value = "纬度")
    private Object lat;

}
