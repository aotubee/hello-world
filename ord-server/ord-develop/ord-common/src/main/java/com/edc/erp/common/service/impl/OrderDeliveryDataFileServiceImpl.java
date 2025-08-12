package com.edc.erp.common.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.enumeration.OrderDeliverFileBusinessTypeEnum;
import com.edc.erp.common.enumeration.OrderDeliverFileStatusEnum;
import com.edc.erp.common.enumeration.OrderDeliverFileTypeEnum;
import com.edc.erp.common.mapper.OrderDeliveryDataFileMapper;
import com.edc.erp.common.model.in.datafile.OrdDeliveryDataFileIn;
import com.edc.erp.common.model.out.datafile.OrdDeliveryDataFileOut;
import com.edc.erp.common.service.OrderDeliveryDataFileService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.export.AsyncExportExecutor;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.sdk.dictionary.service.SystemDictService;
import com.edc.uc.authority.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 配货/配销数据文件
 */
@Service
@Slf4j
public class OrderDeliveryDataFileServiceImpl extends BaseServiceImpl<OrdDeliveryDataFile> implements OrderDeliveryDataFileService {

    @Autowired
    private OrderDeliveryDataFileMapper orderDeliveryDataFileMapper;
    @Autowired
    private SystemDictService systemDictService;
    @Autowired
    private AsyncPushTaskService asyncPushTaskService;

    @Autowired
    private AsyncExportExecutor asyncExportExecutor;

    @Autowired
    private FileService fileService;

    @Autowired
    @Qualifier("orderDirDeliveryDataFileSender")
    private MessageSender orderDirDeliveryDataFileSender;

    @Autowired
    @Qualifier("orderDisDeliveryDataFileSender")
    private MessageSender orderDisDeliveryDataFileSender;

    @Override
    public Page<OrdDeliveryDataFileOut> findByPage(OrdDeliveryDataFileIn ordDeliveryDataFileIn) {
        List<OrdDeliveryDataFileOut> fileList = orderDeliveryDataFileMapper.findByPage(ordDeliveryDataFileIn);
        fileList.forEach(dataFile -> {
            dataFile.setExecStatusStr(systemDictService.getSystemDictName(dataFile.getExecStatus()));
            dataFile.setFileTypeStr(systemDictService.getSystemDictNameByParentNoAndCode(dataFile.getBusinessType(), dataFile.getFileType()));
        });
        Page page = new Page(ordDeliveryDataFileIn);
        page.setList(fileList);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OrdDeliveryDataFile ordDeliveryDataFile) {
        if (null == ordDeliveryDataFile) throw new BusinessException("请上传文件信息");
        if (StringUtils.isBlank(ordDeliveryDataFile.getFileId())) throw new BusinessException("文件ID不能为空");
        if (StringUtils.isBlank(ordDeliveryDataFile.getBusinessType())) throw new BusinessException("业务类型为空");
        ordDeliveryDataFile.setExecStatus(OrderDeliverFileStatusEnum.IN_EXECUTION.getCode());
        ordDeliveryDataFile.setBeginTime(LocalDateTime.now());
        orderDeliveryDataFileMapper.insertSelective(ordDeliveryDataFile);

        if (ordDeliveryDataFile.getBusinessType().equals(OrderDeliverFileBusinessTypeEnum.DIR.getCode())) {
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.ORDER_DIR_DELIVERY_DATA_FILE, JSONObject.toJSONString(ordDeliveryDataFile), ordDeliveryDataFile.getBizOrgCode(), null);
            orderDirDeliveryDataFileSender.sendSync(JSONObject.toJSONString(ordDeliveryDataFile).getBytes(), System.currentTimeMillis() + SystemConstant.MQ_DELAY_FAST_TIME);
        }
        if (ordDeliveryDataFile.getBusinessType().equals(OrderDeliverFileBusinessTypeEnum.DIS.getCode())) {
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.ORDER_DIS_DELIVERY_DATA_FILE, JSONObject.toJSONString(ordDeliveryDataFile), ordDeliveryDataFile.getBizOrgCode(), null);
            orderDisDeliveryDataFileSender.sendSync(JSONObject.toJSONString(ordDeliveryDataFile).getBytes(), System.currentTimeMillis() + SystemConstant.MQ_DELAY_FAST_TIME);
        }

    }

    @Override
    public void over(OrdDeliveryDataFile ordDeliveryDataFile) {
        if (null == ordDeliveryDataFile || null == ordDeliveryDataFile.getId()) return;
        ordDeliveryDataFile.setExecStatus(OrderDeliverFileStatusEnum.COMPLETE.getCode());
        ordDeliveryDataFile.setOverTime(LocalDateTime.now());
        orderDeliveryDataFileMapper.updateByPrimaryKeySelective(ordDeliveryDataFile);
    }

    @Override
    public String download(Integer dateFileId) {
        OrdDeliveryDataFile entity = orderDeliveryDataFileMapper.selectByPrimaryKey(dateFileId);
        if (null == entity || StringUtils.isBlank(entity.getErrFileId())) return "";
        String namePreffix = OrderDeliverFileTypeEnum.getNameByCode(entity.getFileType());
        byte[] fileBytes = fileService.getFileBytesByFileId(entity.getErrFileId(), SystemConstant.SYSTEM_CODE);
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = namePreffix.concat(DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_MS_PATTERN)).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> fileBytes
        );
        return AsyncExportExecutor.DOWNLOADING;
    }
}
