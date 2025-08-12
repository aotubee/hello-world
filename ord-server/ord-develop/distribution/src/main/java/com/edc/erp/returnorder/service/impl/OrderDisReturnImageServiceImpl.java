package com.edc.erp.returnorder.service.impl;

import com.edc.erp.returnorder.entity.OrdDisReturnImage;
import com.edc.erp.returnorder.mapper.OrdDisReturnImageMapper;
import com.edc.erp.returnorder.service.OrderDisReturnImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName OrderDisReturnImageServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/4/18 18:01
 **/
@Service
@RequiredArgsConstructor
public class OrderDisReturnImageServiceImpl implements OrderDisReturnImageService {

    private OrdDisReturnImageMapper ordDisReturnImageMapper;

    @Override
    public List<OrdDisReturnImage> findAllReturnImage(Integer id) {
        OrdDisReturnImage ordDisReturnImage = new OrdDisReturnImage();
        ordDisReturnImage.setReturnOrderId(id);
        return ordDisReturnImageMapper.select(ordDisReturnImage);
    }
}
