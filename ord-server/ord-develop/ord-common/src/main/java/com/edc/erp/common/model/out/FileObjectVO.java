/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */

package com.edc.erp.common.model.out;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 返回前段的file对象描述
 *
 * @author: renshaobo
 * @date: 2019-06-13
 */
@Data
@ToString
public class FileObjectVO implements Serializable {

    private static final long serialVersionUID = 4858890531060217910L;
    /**
     * 主键
     */
    private String id;

    /**
     * 系统编号
     */
    private String systemNo;

    /**
     * 业务模块名
     */
    private String businessName;

    /**
     * 文件的key
     */
    private String fileKey;

    /**
     * 文件生成的完整url
     */
    private String url;

    /**
     * 文件原始名称
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long contentSize;

    /**
     * 文件的contenttype
     */
    private String contentType;

    /**
     * 文件是否为公有
     */
    private Boolean isPublic;
}
