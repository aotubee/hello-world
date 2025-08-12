/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */

package com.edc.erp.common.service.impl;

import com.edc.erp.common.model.in.PutObjectStreamIn;
import com.edc.erp.common.model.out.FileObjectVO;
import com.edc.erp.common.rpc.FilesClient;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.net.URL;

/**
 * 公用的处理文件调用的util
 *
 * @author: lishaobo
 * @date: 2019-11-22
 */
@Component
@Slf4j
public class FileService {

    @Resource
    private FilesClient filesClient;

    @Value("${spring.application.name}")
    private String applicationName;

    public static byte[] file2byte(File tradeFile) {
        byte[] buffer = null;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = new FileInputStream(tradeFile);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            log.error("未找对文件对象");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != bos) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer;
    }

    public byte[] getFileBytesByFileId(String fileId, String systemNo) {
        String privateUrl = getFileUrlAndDelFileById(fileId, systemNo);
        if (StringUtils.isBlank(privateUrl)) {
            return null;
        }
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(new URL(privateUrl));
        } catch (IOException e) {
            log.error("用文件URL下载失败，文件ID是--{}", fileId);
            return null;
        }
        return bytes;
    }

    /**
     * 根据文件ID去兑换私有文件URL的方法
     *
     * @param fileId
     * @param systemNo
     * @return
     */
    private String getFileUrlAndDelFileById(String fileId, String systemNo) {
        Response<String> downResponse = filesClient.getFileUrlById(fileId);
        String privateUrl;
        if (downResponse.isSuccess()) {
            privateUrl = downResponse.getData();
        } else {
            log.error("兑换文件URL失败，文件ID是--{}", fileId);
            return null;
        }
        // 删除OSS文件
        try {
            filesClient.delete(fileId, systemNo, applicationName);
        } catch (Exception e) {
            log.error("删除文件失败，文件ID是--{},错误信息是--{}", fileId, e.getMessage());
        }
        return privateUrl;
    }

    /**
     * 非页面自定义文件上传
     * @param fileName 文件名
     * @param bytes    文件流
     * @param systemNo    系统编码
     * @param systemName  系统名称
     * @return
     */
    public String uploadFile(String fileName, byte[] bytes,String systemNo,String systemName){
        if (ArrayUtils.isEmpty(bytes)) {
            return StringUtils.EMPTY;
        }
        PutObjectStreamIn fileApiIn = new PutObjectStreamIn();
        fileApiIn.setSystemNo(systemNo);
        fileApiIn.setBusinessName(systemName);
        fileApiIn.setFileName(fileName);
        fileApiIn.setIsPublic(false);
        fileApiIn.setFile(bytes);
        Response<FileObjectVO> uploadResponse = filesClient.uploadByStream(fileApiIn);
        String id;
        String privateUrl;
        if (uploadResponse.isSuccess()) {
            id = uploadResponse.getData().getId();
            privateUrl = uploadResponse.getData().getUrl();
        } else {
            log.error("上传失败");
            return StringUtils.EMPTY;
        }
        // 删除OSS文件
        try {
            filesClient.delete(id, systemNo, applicationName);
        } catch (Exception e) {
            log.error("删除文件失败，文件ID是--{},错误信息是--{}", id, e.getMessage());
        }
        return privateUrl;
    }

    public String upload(String systemNo, String fileName, byte[] bytes){
        PutObjectStreamIn putObjectStreamVO = new PutObjectStreamIn(systemNo, applicationName, false, fileName, bytes);
        Response<FileObjectVO> uploadResponse = filesClient.uploadByStream(putObjectStreamVO);
        String id;
        String privateUrl;
        if (uploadResponse.isSuccess()) {
            id = uploadResponse.getData().getId();
            privateUrl = uploadResponse.getData().getUrl();
        } else {
            log.error("上传财务计划导出列表失败");
            return null;
        }
        // 删除OSS文件
        try {
            filesClient.delete(id, systemNo, applicationName);
        } catch (Exception e) {
            log.error("删除文件失败，文件ID是--{},错误信息是--{}", id, e.getMessage());
        }
        return privateUrl;
    }

    public String uploadAndGetId(String systemNo, String fileName, byte[] bytes) {
        PutObjectStreamIn putObjectStreamVO = new PutObjectStreamIn(systemNo, applicationName, false, fileName, bytes);
        Response<FileObjectVO> uploadResponse = filesClient.uploadByStream(putObjectStreamVO);
        String id;
        if (uploadResponse.isSuccess()) {
            id = uploadResponse.getData().getId();
        } else {
            log.error("上传导出列表失败");
            return null;
        }
        // 删除OSS文件
        try {
            filesClient.delete(id, systemNo, applicationName);
        } catch (Exception e) {
            log.error("删除文件失败，文件ID是--{},错误信息是--{}", id, e.getMessage());
        }
        return id;
    }
}
