package com.edc.erp.directly.returnorder.enumeration;



public enum OrdReturnOrderReasonEnum {

    PREVIOUS_RETURN("previousReturn", "往次退货"),

    DIFFERENCE("difference", "差异"),

    THIS_RETURN("thisReturn", "当次残损"),
    NOTICE_RETURN("noticeReturn", "通知退货");

    private String dictVlueCode;

    private String dictValueName;


    OrdReturnOrderReasonEnum(String dictVlueCode, String dictValueName) {
        this.dictVlueCode = dictVlueCode;
        this.dictValueName = dictValueName;
    }
    public String getDictVlueCode() {
        return dictVlueCode;
    }

    public void setDictVlueCode(String dictVlueCode) {
        this.dictVlueCode = dictVlueCode;
    }

    public String getDictValueName() {
        return dictValueName;
    }

    public void setDictValueName(String dictValueName) {
        this.dictValueName = dictValueName;
    }

    public static String getNameByCode(String dictVlueCode) {
        for (OrdReturnOrderReasonEnum ele : values()) {
            if (ele.getDictVlueCode().equals(dictVlueCode)) {
                return ele.getDictValueName();
            }
        }
        return null;
    }

}
