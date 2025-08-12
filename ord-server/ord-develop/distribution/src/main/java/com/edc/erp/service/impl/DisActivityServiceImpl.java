package com.edc.erp.service.impl;

import com.edc.erp.entity.DisActivity;
import com.edc.erp.service.DisActivityService;
import com.edc.erp.mapper.DisActivityMapper;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;





/**
 * 配销活动表(DisActivity)表服务实现类
 *
 * @author fxw
 * @since 2022-10-19 18:43:02
 */
@Service
@RequiredArgsConstructor
public class DisActivityServiceImpl extends BaseServiceImpl<DisActivity> implements DisActivityService {
     
     private final DisActivityMapper disActivityMapper;
}
