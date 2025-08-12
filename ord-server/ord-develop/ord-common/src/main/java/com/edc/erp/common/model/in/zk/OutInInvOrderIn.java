/**
 * Copyright © 2010-2021 Everyday Chain. All rights reserved.
 */

package com.edc.erp.common.model.in.zk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 查询出入库单据入参类
 *
 * @author: lishaobo
 * @date: 2021-04-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutInInvOrderIn implements Serializable {

    /**
     * 开始时间
     */
    private String beginTime;

    /**
     * 结束时间
     */
    private String endTime;
}