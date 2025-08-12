package com.edc.erp.directly.returnorder.entity;


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
import java.time.LocalDateTime;


/**
 * 退货单(OrdDirReturn)实体类
 *
 * @author
 * @since 2022-11-18 18:50:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_return_image")
@ApiModel(value = "OrdDirReturnImage", description = "退货单图片上传")
public class OrdDirReturnImage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 退货单号
     */
    @ApiModelProperty(name = "returnOrderId", value = "退货单主键")
    private Integer returnOrderId;

    @ApiModelProperty(name = "returnDetailId", value = "退货单明细主键")
    private Integer returnDetailId;

    /**
     * 退货状态
     */
    @ApiModelProperty(name = "image_url", value = "退货状态")
    private String image_url;


    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;


    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;


}
