/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.cloud.statistics.rdb;

import com.google.common.base.Optional;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRegisterStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRunningStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskRunningStatistics;
import org.apache.commons.dbcp.BasicDataSource;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Date;

public class StatisticRdbRepositoryTest {
    
    private StatisticRdbRepository repository;
    
    @Before
    public void setup() throws SQLException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        repository = new StatisticRdbRepository(dataSource);
    }
    
    @Test
    public void assertAddTaskResultStatistics() {
        for (StatisticInterval each : StatisticInterval.values()) {
            Assert.assertTrue(repository.add(new TaskResultStatistics(100, 0, each, new Date())));
        }
    }
    
    @Test
    public void assertAddTaskRunningStatistics() {
        Assert.assertTrue(repository.add(new TaskRunningStatistics(100, new Date())));
    }
    
    @Test
    public void assertAddJobRunningStatistics() {
        Assert.assertTrue(repository.add(new TaskRunningStatistics(100, new Date())));
    }
    
    @Test
    public void assertAddJobRegisterStatistics() {
        Assert.assertTrue(repository.add(new JobRegisterStatistics(100, new Date())));
    }
    
    @Test
    public void assertFindTaskResultStatisticsWhenTableIsEmpty() {
        Assert.assertThat(repository.findTaskResultStatistics(new Date(), StatisticInterval.MINUTE).size(), Is.is(0));
        Assert.assertThat(repository.findTaskResultStatistics(new Date(), StatisticInterval.HOUR).size(), Is.is(0));
        Assert.assertThat(repository.findTaskResultStatistics(new Date(), StatisticInterval.DAY).size(), Is.is(0));
    }
    
    @Test
    public void assertFindTaskResultStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        for (StatisticInterval each : StatisticInterval.values()) {
            Assert.assertTrue(repository.add(new TaskResultStatistics(100, 0, each, yesterday)));
            Assert.assertTrue(repository.add(new TaskResultStatistics(100, 0, each, now)));
            Assert.assertThat(repository.findTaskResultStatistics(yesterday, each).size(), Is.is(2));
            Assert.assertThat(repository.findTaskResultStatistics(now, each).size(), Is.is(1));
        }
    }
    
    @Test
    public void assertGetSummedTaskResultStatisticsWhenTableIsEmpty() {
        for (StatisticInterval each : StatisticInterval.values()) {
            TaskResultStatistics po = repository.getSummedTaskResultStatistics(new Date(), each);
            Assert.assertThat(po.getSuccessCount(), Is.is(0));
            Assert.assertThat(po.getFailedCount(), Is.is(0));
        }
    }
    
    @Test
    public void assertGetSummedTaskResultStatistics() {
        for (StatisticInterval each : StatisticInterval.values()) {
            Date date = new Date();
            repository.add(new TaskResultStatistics(100, 2, each, date));
            repository.add(new TaskResultStatistics(200, 5, each, date));
            TaskResultStatistics po = repository.getSummedTaskResultStatistics(date, each);
            Assert.assertThat(po.getSuccessCount(), Is.is(300));
            Assert.assertThat(po.getFailedCount(), Is.is(7));
        }
    }
    
    @Test
    public void assertFindLatestTaskResultStatisticsWhenTableIsEmpty() {
        for (StatisticInterval each : StatisticInterval.values()) {
            Assert.assertFalse(repository.findLatestTaskResultStatistics(each).isPresent());
        }
    }
    
    @Test
    public void assertFindLatestTaskResultStatistics() {
        for (StatisticInterval each : StatisticInterval.values()) {
            repository.add(new TaskResultStatistics(100, 2, each, new Date()));
            repository.add(new TaskResultStatistics(200, 5, each, new Date()));
            Optional<TaskResultStatistics> po = repository.findLatestTaskResultStatistics(each);
            Assert.assertThat(po.get().getSuccessCount(), Is.is(200));
            Assert.assertThat(po.get().getFailedCount(), Is.is(5));
        }
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWhenTableIsEmpty() {
        Assert.assertThat(repository.findTaskRunningStatistics(new Date()).size(), Is.is(0));
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        Assert.assertTrue(repository.add(new TaskRunningStatistics(100, yesterday)));
        Assert.assertTrue(repository.add(new TaskRunningStatistics(100, now)));
        Assert.assertThat(repository.findTaskRunningStatistics(yesterday).size(), Is.is(2));
        Assert.assertThat(repository.findTaskRunningStatistics(now).size(), Is.is(1));
    }
    
    @Test
    public void assertFindLatestTaskRunningStatisticsWhenTableIsEmpty() {
        Assert.assertFalse(repository.findLatestTaskRunningStatistics().isPresent());
    }
    
    @Test
    public void assertFindLatestTaskRunningStatistics() {
        repository.add(new TaskRunningStatistics(100, new Date()));
        repository.add(new TaskRunningStatistics(200, new Date()));
        Optional<TaskRunningStatistics> po = repository.findLatestTaskRunningStatistics();
        Assert.assertThat(po.get().getRunningCount(), Is.is(200));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWhenTableIsEmpty() {
        Assert.assertThat(repository.findJobRunningStatistics(new Date()).size(), Is.is(0));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        Assert.assertTrue(repository.add(new JobRunningStatistics(100, yesterday)));
        Assert.assertTrue(repository.add(new JobRunningStatistics(100, now)));
        Assert.assertThat(repository.findJobRunningStatistics(yesterday).size(), Is.is(2));
        Assert.assertThat(repository.findJobRunningStatistics(now).size(), Is.is(1));
    }
    
    @Test
    public void assertFindLatestJobRunningStatisticsWhenTableIsEmpty() {
        Assert.assertFalse(repository.findLatestJobRunningStatistics().isPresent());
    }
    
    @Test
    public void assertFindLatestJobRunningStatistics() {
        repository.add(new JobRunningStatistics(100, new Date()));
        repository.add(new JobRunningStatistics(200, new Date()));
        Optional<JobRunningStatistics> po = repository.findLatestJobRunningStatistics();
        Assert.assertThat(po.get().getRunningCount(), Is.is(200));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWhenTableIsEmpty() {
        Assert.assertThat(repository.findJobRegisterStatistics(new Date()).size(), Is.is(0));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        Assert.assertTrue(repository.add(new JobRegisterStatistics(100, yesterday)));
        Assert.assertTrue(repository.add(new JobRegisterStatistics(100, now)));
        Assert.assertThat(repository.findJobRegisterStatistics(yesterday).size(), Is.is(2));
        Assert.assertThat(repository.findJobRegisterStatistics(now).size(), Is.is(1));
    }
    
    @Test
    public void assertFindLatestJobRegisterStatisticsWhenTableIsEmpty() {
        Assert.assertFalse(repository.findLatestJobRegisterStatistics().isPresent());
    }
    
    @Test
    public void assertFindLatestJobRegisterStatistics() {
        repository.add(new JobRegisterStatistics(100, new Date()));
        repository.add(new JobRegisterStatistics(200, new Date()));
        Optional<JobRegisterStatistics> po = repository.findLatestJobRegisterStatistics();
        Assert.assertThat(po.get().getRegisteredCount(), Is.is(200));
    }
    
    private Date getYesterday() {
        return new Date(new Date().getTime() - 24 * 60 * 60 * 1000);
    }
}
