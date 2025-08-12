package com.edc.erp.directly.returnorder.model.in;

import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
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
public class AppSaveOrdDirReturnDetailIn extends OrdDirReturnDetail {

    @ApiModelProperty(name = "imageUrlList", value = "明细上传图片地址集合")
    private List<String> imageUrlList;
}
