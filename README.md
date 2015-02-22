# Spring batch Showcase

柔軟性は高いんだけどいかんせんなかなかとっつきづらいSpring Batchの使い方とか。

## first step
### package: org.yggd.springbatch.showcase.firststep

"タスクレット"(FirstTasklet, NextTasklet)というSpring Batchによる最小単位のタスクを2つ実行する。

Spring Batchはジョブの実行履歴・状態をRDBMSに永続化する仕組みを持つため、
実行には`JobRepository`と`TransactionManager`という2つのBean定義が必要となるが、
ここでは実行検証用にインメモリの`MapJobRepositoryFactoryBean`とダミーのトランザクション管理機能`ResourcelessTransactionManager`
を定義している。（`beanDefinition.xml`を参照）

`JobRepository`についてはSpring Batchの最重要概念であるため、いずれ深掘りする予定。

実行は以下の2つのクラスを起動する。（やってることは同じ）

 * XMLベースのBean定義の実行はXmlConfigMainを実行する。(JobRepository, TrunsactionManagerの定義が必須)
 * JavaベースのBean定義の実行はJavaConfigMainを実行する。(JobRepository, TrunsactionManagerは定義不要,WARNログ出るけど)

Spring Batchの1ジョブに共通する構成として以下の通りJob,Stepという2つの要素がある。

 * Job(firstJob)
   * Step(firstStep: 実装は `FirstTasklet`)
   * Step(nextStep: 実装は `NextTasklet`)

1ジョブでやりたいことをStepに分割する必要があるが、**何基準で分割する** かが大事なポイントなのでまた後述する。

