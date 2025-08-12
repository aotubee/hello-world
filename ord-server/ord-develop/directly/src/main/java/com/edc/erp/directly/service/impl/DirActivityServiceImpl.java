package com.edc.erp.directly.service.impl;

import com.edc.erp.directly.entity.DirActivity;
import com.edc.erp.directly.mapper.DirActivityMapper;
import com.edc.erp.directly.service.DirActivityService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;





/**
 * 活动表(DirActivity)表服务实现类
 *
 * @author fxw
 * @since 2022-11-18 18:27:42
 */
@Service
@RequiredArgsConstructor
public class DirActivityServiceImpl extends BaseServiceImpl<DirActivity> implements DirActivityService {
     
     private final DirActivityMapper dirActivityMapper;
}
