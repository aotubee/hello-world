package com.edc.erp.directly.returnorder.enumeration;

/**
 * @Description: 配销退货日志
 * @Author: ZhangYao
 * @Date: 2023/3/28 9:27
 **/
public enum DirOrdReturnOrderLogEnum {

    ORD_DIR_APP_SAVE("ordDirAppSave", "门店保存退货单"),

    ORD_DIR_APP_SUBMIT("ordDisAppSubmit", "门店提交退货单"),

    ;
    private String name;
    private String values;


    DirOrdReturnOrderLogEnum(String name, String values) {
        this.name = name;
        this.values = values;

    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.values;
    }



    public static String getValueByName(String name) {
        for (DirOrdReturnOrderLogEnum ele : values()) {
            if (ele.getName().equals(name)) {
                return ele.getValue();
            }
        }
        return null;
    }


}
