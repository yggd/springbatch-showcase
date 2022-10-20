# Spring batch Showcase

柔軟性は高いんだけどいかんせんなかなかとっつきづらいSpring Batchの使い方とか。

## <span style="color: red; ">OUTDATED 2022/10/21</span>

ここで紹介されている `MapJobRepositoryFactoryBean` は Spring Batch 4.3で非推奨となり、
5.0で削除される予定です。

https://docs.spring.io/spring-batch/docs/current/api/org/springframework/batch/core/repository/support/MapJobRepositoryFactoryBean.html

代替としてはH2などによるインメモリデータベースと、 `JobRepositoryFactoryBean` を使用してください。

詳しい使い方・動かし方については、以下『TERASOLUNA Batch Framework for Java (5.x) Development Guideline』のチュートリアルが参考になると考えます。

https://terasoluna-batch.github.io/guideline/current/ja/single_index.html#Ch09


## first step
### package: org.yggd.springbatch.showcase.firststep

"タスクレット"(FirstTasklet, NextTasklet)というSpring Batchによる最小単位のタスクを2つ実行する。

Spring Batchはジョブの実行履歴・状態をRDBMSに永続化する仕組みを持つため、
実行には`JobRepository`と`TransactionManager`という2つのBean定義が必要となるが、
ここでは実行検証用にインメモリの`MapJobRepositoryFactoryBean`とダミーのトランザクション管理機能`ResourcelessTransactionManager`
を定義している。（`resources/org/yggd/springbatch/showcase/firststep/beanDefinition.xml`を参照）

実行は以下の2つのクラスを起動する。（やってることは同じ）

 * XMLベースのBean定義の実行はXmlConfigMainを実行する。(JobRepository, TransactionManagerの定義が必須)
 * JavaベースのBean定義の実行はJavaConfigMainを実行する。(JobRepository, TransactionManagerは定義不要,WARNログ出るけど)

Spring Batchの1ジョブに共通する構成として以下の通りJob,Stepという2つの要素がある。

 * Job(firstJob)
   * Step(firstStep: 実装は `FirstTasklet`)
   * Step(nextStep: 実装は `NextTasklet`)

場合によっては1ジョブでやりたいことをいくつかのStepに分割する必要があるが、**何基準で分割する** かが大事なポイントなのでまた後述する。

## repository
### package: org.yggd.springbatch.showcase.repository

ジョブとジョブの実行を永続管理する`jobRepository`を使用する。
永続化はRDBMSを使用し、ここではPostgreSQLを使用している。

他のDBサーバの使用や接続先情報を変更する場合は`resources/repository.properties`を編集し、
`resources/commonContext.xml`にある、`PropertyPlaceholderConfigurer`のプロパティファイル名(batch-xxxx.properties)
のxxxxの部分を以下のRDBMSからあてはまるものから選択する。
(もちろんクラスパス上にjdbcドライバも必要。こちらは`build.gradle`を参照のこと。)

 * db2
 * derby
 * h2
 * hsqldb
 * mysql
 * oracle10g
 * postgresql
 * sqlf
 * sqlite
 * sqlserver
 * sybase

`resources/org/yggd/springbatch/showcase/commonContext.xml`の`<jdbc:initialize-database>`要素でDDLが実行され、以下のテーブルが生成される。
分類として**Job/Step** というバッチ処理単位による粒度の違いと、**Jobインスタンス/Job実行** という静的/動的の違いが2つずつあることに注目する。

|テーブル名|説明|
|----------|------|
|BATCH_JOB_INSTANCE|ジョブのBean名と実行時の起動パラメータのハッシュ値であるjob_keyを管理する。|
|BATCH_JOB_EXECUTION|ジョブの実行ステータス（完了、停止、失敗、・・・）を管理する。|
|BATCH_JOB_EXECUTION_PARAMS|ジョブの起動パラメータを管理する。|
|BATCH_JOB_EXECUTION_CONTEXT|ジョブのコンテキストを管理する。|
|BATCH_STEP_EXECUTION|ステップの実行ステータスと、特にリスタート時に必要となるコミット回数、R/W件数を管理する。|
|BATCH_STEP_EXECUTION_CONTEXT|ステップのコンテキストを管理する。|

> テーブル生成のDDLそのものはSpring Batchのjarファイル(spring-batch-core-x.x.x.jar)内に用意されている。
> 初回実行時は`resources/repository.properties`の`batch.data.source.init`を`true`としてバッチ処理を起動することで、
> DDLの実行を行い、テーブルを自動的に生成させることができる。
> （というより`true`のままだと起動時に毎回dropとcreate tableが行われる。）

ジョブ/ステップのコンテキストというのが一見分かりづらいが、それぞれのスコープで持つことができる属性値の集合である。
Servlet仕様で言うところのServletRequest,HttpSessionと同様、スコープが異なる参照・設定可能な属性(attribute)と見えてしまえば分かりやすい。
ただしこのコンテキストは以下のJSON形式でテーブルに格納されるが、LOB列ではないために長さに制限(VARCHARで2500)がある点に注意する。

#### 永続化されるコンテキストの例（ステップコンテキストの例）
`{"map":[{"entry":[{"string":["batch.stepType","org.springframework.batch.core.step.tasklet.TaskletStep"]},{"string":["batch.taskletType","org.yggd.springbatch.showcase.repository.RepositoryTasklet"]},{"string":["key","THIS IS STEP EXECUTION CONTEXT."]}]}]}`

> 使いどころとしてはStep間で引き継ぎたいユーザ情報をジョブのコンテキストとして退避しておくことができる。
> 「情報量の制限が引っかかる」、「いちいち永続化してほしくない」、「APIが紛らわしいし長いので使いたくない」などの理由が
> ある場合は代わりにjobスコープのBeanをDIして引き継ぐという手もある。

ジョブ/ステップのコンテキスト取得をTaskletでやる場合は以下のように記述する。

```:java
// Stepスコープのコンテキストの取得
ExecutionContext stepExecutionContext = chunkContext
    .getStepContext().getStepExecution().getExecutionContext();

// Jobスコープのコンテキストの取得
ExecutionContext jobExecutionContext = chunkContext.getStepContext()
    .getStepExecution().getJobExecution().getExecutionContext();
```

なお、JOB_EXECUTIONテーブルで同じジョブを再実行すると、以下のように`JobRestartException`がスローされてしまう。

```
[main] ERROR o.s.b.c.l.s.CommandLineJobRunner - Job Terminated in error: JobInstance already exists and is not restartable
org.springframework.batch.core.repository.JobRestartException: JobInstance already exists and is not restartable
	at org.springframework.batch.core.launch.support.SimpleJobLauncher.run(SimpleJobLauncher.java:101) ~[spring-batch-core-3.0.2.RELEASE.jar:3.0.2.RELEASE]
	at org.springframework.batch.core.launch.support.CommandLineJobRunner.start(CommandLineJobRunner.java:362) [spring-batch-core-3.0.2.RELEASE.jar:3.0.2.RELEASE]
	at org.springframework.batch.core.launch.support.CommandLineJobRunner.main(CommandLineJobRunner.java:590) [spring-batch-core-3.0.2.RELEASE.jar:3.0.2.RELEASE]
	at org.yggd.springbatch.showcase.repository.Main.main(Main.java:11) [main/:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.7.0_51]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57) ~[na:1.7.0_51]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.7.0_51]
	at java.lang.reflect.Method.invoke(Method.java:606) ~[na:1.7.0_51]
	at com.intellij.rt.execution.application.AppMain.main(AppMain.java:140) [idea_rt.jar:na]
```

`JobLauncher`が永続化済みの`JobInstance`を復元しようとするが、ここでBATCH_JOB_INSTANCEテーブルの
job_key（対象ジョブと起動パラメータのハッシュ値）が一致し、既に正常実行済み（COMPLETED）ステータスとなる場合は「運用上意図しない重複実行」と判断される。

これは正常終了したジョブの重複実行を防ぐ目的があるが、永続化された`jobRepository`を使用した上でのテストや定期実行ジョブなどでは煩雑となり、
かえって弊害となるケースが出てくる。

対応策としてはタイムスタンプやユーザ定義のシーケンス値など、ジョブ実行時にユニーク値を与えられる起動パラメータを用意する手があるが、
より手軽かつ確実な方法として、ここでは`JsrJobParametersConverter`をBean定義内に記述している。
動作上の理屈としてはジョブ起動の都度、ユニークな起動パラメータ(jsr_batch_run_id)を追加してくれることで
BATCH_JOB_INSTANCEのインスタンスキーが変化し、「別のジョブ実行」として扱われる仕組みを利用している。
（多重実行チェックそのものを抑止しているわけではない。）
記述例は`resources/org/yggd/springbatch/showcase/commonContext.xml`を参照のこと。

もちろんfirststepで述べたように、インメモリの`jobRepository`を使用している場合や、
都度DDLでテーブルをドロップさせている場合は重複実行例外は発生しない。

