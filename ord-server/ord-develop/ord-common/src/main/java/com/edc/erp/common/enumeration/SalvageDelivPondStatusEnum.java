package com.edc.erp.common.enumeration;

public enum SalvageDelivPondStatusEnum {


    WAIT_EXECUTION("waitExecution", "待执行"),
    EXECUTION_ING("waitExecutionIng", "执行中"),
    COMPLETED("completed", "已完结"),
    ;
    private String key;
    private String value;

    SalvageDelivPondStatusEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

}
