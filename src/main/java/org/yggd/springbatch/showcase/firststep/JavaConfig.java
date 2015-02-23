package org.yggd.springbatch.showcase.firststep;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JavaベースのBean定義
 */
@Configuration
@EnableBatchProcessing // Spring BatchのBean定義として必要
public class JavaConfig {

    @Autowired
    private JobBuilderFactory jobBuilders;

    @Autowired
    private StepBuilderFactory stepBuilders;

    /**
     * Taskletの定義.
     *
     * @return FirstTasklet
     */
    @Bean
    public Tasklet firstTasklet() {
        return new FirstTasklet();
    }


    /**
     * 後続Taskletの定義
     *
     * @return NextTasklet
     */
    @Bean
    public Tasklet nextTasklet() {
        return new NextTasklet();
    }

    /**
     * ジョブステップの定義.
     *
     * @return firstStep
     */
    @Bean
    protected Step firstStep() {
        return stepBuilders.get("firstStep")
                .tasklet(firstTasklet()) // 上のTaskletをステップに登録
                .build();
    }

    @Bean
    protected  Step nextStep() {
        return stepBuilders.get("nextStep")
                .tasklet(nextTasklet())
                .build();
    }

    /**
     * ジョブの定義
     *
     * @return firstJob
     */
    @Bean
    protected Job firstJob() {
        return jobBuilders.get("firstJob")
                .start(firstStep()) // 上のステップをジョブに登録
                .next(nextStep())
                .build();
    }
}
