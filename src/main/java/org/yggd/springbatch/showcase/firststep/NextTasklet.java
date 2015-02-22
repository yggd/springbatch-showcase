package org.yggd.springbatch.showcase.firststep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

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
        LOGGER.info("I am {}!", "NEXT TASKLET");
        return RepeatStatus.FINISHED; // このステップはこれで終了
    }
}
