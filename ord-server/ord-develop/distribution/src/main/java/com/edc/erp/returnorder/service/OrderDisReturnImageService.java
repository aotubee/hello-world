package com.edc.erp.returnorder.service;

import com.edc.erp.returnorder.entity.OrdDisReturnImage;

import java.util.List;

public interface OrderDisReturnImageService {

    List<OrdDisReturnImage> findAllReturnImage(Integer id);
}
