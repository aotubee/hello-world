/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */

package com.edc.erp.common.rpc;

import com.edc.erp.common.model.in.PutObjectStreamIn;
import com.edc.erp.common.model.out.FileObjectVO;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 文件服务接口
 *
 * @author: lishaobo
 * @date: 2019-09-08
 */
@FeignClient(name = "edc-server-file", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface FilesClient {

    /**
     * 上传文件
     *
     * @param putObjectStreamIn 文件上传参数类
     * @return Response<FileObjectVO>
     * @author: lishaobo
     * @date: 2019-09-24 15:20
     */
    @PostMapping(value = "/file/uploadByStream")
    @ResponseBody
    Response<FileObjectVO> uploadByStream(@RequestBody PutObjectStreamIn putObjectStreamIn);

    /**
     * 兑换文件临时url
     *
     * @param id 文件私有ID
     * @return Response<String>
     * @author: lishaobo
     * @date: 2019-09-24 15:20
     */
    @GetMapping(value = "/file/down")
    @ResponseBody
    Response<String> getFileUrlById(@RequestParam("id") String id);

    /***
     * 删除文件临时url
     * @param fileId
     * @param systemNo
     * @param businessName
     * @return
     */
    @DeleteMapping(value = "/file/delete")
    @ResponseBody
    Response delete(@RequestParam("fileId") String fileId, @RequestParam("systemNo") String systemNo,
                    @RequestParam("businessName") String businessName);
}
