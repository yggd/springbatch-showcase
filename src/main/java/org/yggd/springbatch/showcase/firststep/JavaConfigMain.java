package org.yggd.springbatch.showcase.firststep;

import org.springframework.batch.core.launch.support.CommandLineJobRunner;

/**
 * コマンドラインから実行（するように見せかける）
 */
public class JavaConfigMain {

    /**
     * エントリポイント
     *
     * @param args 引数
     */
    public static void main(String[] args) throws Exception {
        CommandLineJobRunner.main(
                new String[]{JavaConfig.class.getName(), "firstJob", "aaa=hoge"}
        );
    }
}
