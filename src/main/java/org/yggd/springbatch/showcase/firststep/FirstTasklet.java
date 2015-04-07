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
 * 最初のSpring Bathchアプリ、タスクレットのexecute()が呼び出されることを確認する。
 */
public class FirstTasklet implements Tasklet {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirstTasklet.class);

    /**
     * Taskletのエントリポイント
     *
     * @param contribution ステップの実行状態
     * @param chunkContext チャンクの実行状態
     * @return ステータス(終了)
     * @throws Exception 予期しない例外
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();

        // ジョブ起動パラメータの取得
        LOGGER.info("FirstTasklet has been executed. job param is {}", jobParameters);

        ExecutionContext executionContext = chunkContext.getStepContext()
                                                .getStepExecution()
                                                .getJobExecution()
                                                .getExecutionContext();
        // ステップ間の情報引き継ぎはJobExecutionのExecutionContextを取得する。（StepExecutionのExecutionContextではダメ）
        executionContext.put("message", "foobar");
        return RepeatStatus.FINISHED; // このステップはこれで終了
    }
}
