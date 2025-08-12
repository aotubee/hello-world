package com.edc.erp.directly.returnorder.service.impl;

import com.edc.erp.directly.returnorder.entity.OrdDirReturnImage;
import com.edc.erp.directly.returnorder.mapper.OrdDirReturnImageMapper;
import com.edc.erp.directly.returnorder.service.OrderDirReturnImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName OrderDirReturnImageServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/4/18 18:04
 **/
@Service
@RequiredArgsConstructor
public class OrderDirReturnImageServiceImpl implements OrderDirReturnImageService {

    private final OrdDirReturnImageMapper ordDirReturnImageMapper;

    @Override
    public List<OrdDirReturnImage> findAllReturnImage(Integer id) {
        OrdDirReturnImage ordDirReturnImage = new OrdDirReturnImage();
        ordDirReturnImage.setReturnOrderId(id);
        return ordDirReturnImageMapper.select(ordDirReturnImage);
    }
}
