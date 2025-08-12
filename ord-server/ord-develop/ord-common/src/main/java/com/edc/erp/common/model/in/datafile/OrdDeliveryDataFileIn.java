package com.edc.erp.common.model.in.datafile;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author tangxiaoliang
 * @description:配销/配货上传文件数据表
 * @since 2023/03/13
 */
@Data
public class OrdDeliveryDataFileIn extends Page implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 文件名 */
    @ApiModelProperty(name = "fileName", value = "文件名")
    private String fileName;

    /** 文件ID */
    @ApiModelProperty(name = "fileId", value = "文件ID")
    private String fileId;

    /** 业务类型(直营/加盟) */
    @ApiModelProperty(name = "businessType", value = "业务类型(直营/加盟)")
    private String businessType;

    /** 文件类型 */
    @ApiModelProperty(name = "fileType", value = "文件类型")
    private String fileType;

    /** 执行状态 */
    @ApiModelProperty(name = "execStatus", value = "执行状态")
    private String execStatus;

    /** 开始时间 */
    @ApiModelProperty(name = "beginTime", value = "开始时间")
    private LocalDateTime beginTime;

    /** 创建时间 */
    @ApiModelProperty(name = "overTime", value = "创建时间")
    private LocalDateTime overTime;

    /** 开始时间 */
    @ApiModelProperty(name = "startTime", value = "查询开始时间")
    private LocalDateTime startTime;

    /** 创建时间 */
    @ApiModelProperty(name = "endTime", value = "查询结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(name = "orgCode", value = "公司code")
    private String orgCode;

    @ApiModelProperty(name = "bizOrgCode", value = "组织code")
    private String bizOrgCode;

    /** 结束时间 */
    @ApiModelProperty(name = "creator", value = "结束时间")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 修改人 */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 是否删除(0：否，1：是；默认0) */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;
}
