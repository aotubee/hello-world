package com.edc.erp.distribution.controller;

import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



/**
 * <p>
 * 配销订货周期 前端控制器
 * </p>
 *
 * @author fxw
 * @since 2022-10-17 16:49:33
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisOrderCycle")
@Api(value = "ordDisOrderCycle", tags = "配销订货周期模块")
public class OrdDisOrderCycleController {

    private final DisOrderCycleHandle disOrderCycleHandle;

}
