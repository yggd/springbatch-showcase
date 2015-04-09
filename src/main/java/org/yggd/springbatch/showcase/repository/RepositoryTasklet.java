package org.yggd.springbatch.showcase.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

public class RepositoryTasklet implements Tasklet {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LOGGER.info("RepositoryTasklet called.");
        ExecutionContext stepExecutionContext = chunkContext
                .getStepContext().getStepExecution().getExecutionContext();
        ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext();

        // それぞれ、STEP_EXECUTION_CONTEXTテーブル、JOB_EXECUTION_CONTEXTテーブルへ永続化される。
        stepExecutionContext.put("key", "THIS IS STEP EXECUTION CONTEXT.");
        jobExecutionContext.put("key", "THIS IS JOB EXECUTION CONTEXT.");
        return RepeatStatus.FINISHED;
    }
}
