package com.edc.erp.common.async;

/**
 * @description: 异步推送接口
 * @author fxw
 * @since 2022-11-22
 */
@FunctionalInterface
public interface AsynPusher {
    
    /**
     * 异步调用其他微服务接口
     *
     * @param jsonArgs
     * @return
     */
    String push(String jsonArgs);
}
