/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */

package com.edc.erp.common.model.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 本类提供服务上传文件参数
 *
 * @author: renshaobo
 * @date: 2019-05-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PutObjectStreamIn implements Serializable {

    private static final long serialVersionUID = 590367413578909856L;

    private String systemNo;

    /**
     * 业务名
     */
    private String businessName;

    /**
     * 是否公共读
     */
    private Boolean isPublic;

    /**
     * 文件
     */
    private String fileName;

    /**
     * 文件
     */
    private byte[] file;
}
