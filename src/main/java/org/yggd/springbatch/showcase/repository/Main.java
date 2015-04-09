package org.yggd.springbatch.showcase.repository;

import org.springframework.batch.core.launch.support.CommandLineJobRunner;

/**
 * JobRepository(DB)で管理されるジョブの実行
 */
public class Main {

    public static void main(String[] args) throws Exception {
        CommandLineJobRunner.main(
                new String[]{"org/yggd/springbatch/showcase/repository/repositoryConfig.xml",
                        "repositoryJob", "aaa=hoge"});
    }
}
