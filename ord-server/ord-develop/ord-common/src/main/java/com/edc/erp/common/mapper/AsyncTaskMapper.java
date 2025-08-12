package com.edc.erp.common.mapper;

import com.edc.erp.common.entity.AsyncTask;
import com.edc.erp.common.model.in.AsyncTaskQueryIn;
import com.edc.erp.common.model.out.AsyncTaskQueryOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 异步任务执行(AsyncTask)表数据库访问层
 *
 * @author zhangdong
 * @since 2022-08-30 17:20:59
 */
@Repository
public interface AsyncTaskMapper extends BaseMapper<AsyncTask> {

    /**
     * 查询任务列表
     *
     * @param retryMax
     * @return
     */
    List<AsyncTask> findList(@Param("retryNo") Integer retryMax);

    /**
     * 删除任务
     *
     * @param type
     * @param bizId
     * @return
     */
    int deleteTask(@Param("taskType") String type, @Param("bizId") Integer bizId);

    List<Long> findTaskIdList(@Param("retryNo") Integer retryMax);

    AsyncTask getAsyncTaskByParameter(@Param("id") Long id, @Param("retryNo") Integer retryMax);

    List<Long> findTaskIdListByBizOrgCode(@Param("retryNo") Integer retryMax, @Param("type") String type,
                                          @Param("bizOrgCode") String bizOrgCode, @Param("limitNum") Integer limitNum);

    List<AsyncTaskQueryOut> findAsyncTaskListByParameter(AsyncTaskQueryIn asyncTaskQueryIn);

    List<AsyncTask> findNeedUpdateZkOrderNoList(String bizOrgCode);

    void updateRemarkById(String remark, Integer id);

    void batchUpdate(@Param("idList") List<Integer> idList, @Param("type") String type);

    List<AsyncTask> findNeedRetryTaskList(@Param("businessNoList") List<String> businessNoList, @Param("type") String type);
}
