package com.edc.erp.returnorder.model.in;

import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName AppOrdDirReturnSaveIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/4/17 18:09
 **/
@Data
public class AppSaveOrdDisReturnDetailIn extends OrdDisReturnDetail {

    @ApiModelProperty(name = "imageUrlList", value = "明细上传图片地址集合")
    private List<String> imageUrlList;
}
