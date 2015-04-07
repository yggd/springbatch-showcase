package org.yggd.springbatch.showcase.firststep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.Map;

/**
 * 後続タスクレット
 */
public class NextTasklet implements Tasklet {

    private static final Logger LOGGER = LoggerFactory.getLogger(NextTasklet.class);

    /**
     * 後続タスクレット
     *
     * @param contribution ステップの実行状態
     * @param chunkContext チャンクの実行状態
     * @return ステータス(終了)
     * @throws Exception 予期しない例外
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        // JobのExecutionContextによるStep間の引き継ぎ
        ExecutionContext ctx = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        LOGGER.info("I am NEXT TASKLET! execution context set by previous step context is {}", ctx.get("message")); // 前Stepからの引き継ぎ

        return RepeatStatus.FINISHED; // このステップはこれで終了
    }
}
