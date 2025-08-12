package com.edc.erp.directly.returnorder.service;

import com.edc.erp.directly.returnorder.entity.OrdDirReturnImage;

import java.util.List;

public interface OrderDirReturnImageService {

    List<OrdDirReturnImage> findAllReturnImage(Integer id);
}
