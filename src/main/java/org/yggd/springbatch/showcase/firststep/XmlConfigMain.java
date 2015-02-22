package org.yggd.springbatch.showcase.firststep;

import org.springframework.batch.core.launch.support.CommandLineJobRunner;

/**
 * Created by yggd on 2015/02/22.
 */
public class XmlConfigMain {

    /**
     * XMLのBean定義ファイルから起動
     *
     * @param args 引数
     */
    public static void main(String[] args) throws Exception {
        CommandLineJobRunner.main(new String[]{"org/yggd/springbatch/showcase/firststep/beanDefinition.xml", "firstJob"});
    }
}
