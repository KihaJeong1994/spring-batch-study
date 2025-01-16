# Spring batch study

## 1. spring batch 탄생 배경
- java 기반 표준 배치 기술(JSR)

## 2. 배치 핵심 패턴
- Read
- Process
- Write

## 3. batch 시나리오(batch에서 자주 쓰이고 필요한 기능. spring batch에서 제공)
- 배치 프로세스를 주기적으로 커밋(한꺼번에 커밋하면 db 과부하)
- 동시 다발적인 job 배치 처리. 대용량 병렬 처리
- 실패 후 수동 또는 스케줄링에 의한 재시작
- 의존관계가 있는 step 여러개 순차적 처리
- 조건적 flow 구성을 통한 체계적이고 유연한 배치 모델 구성
- 반복, 재시도, skip 처리


## 4. spring batch architecture

1. Application
- 비즈니스 로직
2. batch core
- Job, step 등 job을 구성할 수 있는 클래스들
3. batch infrastructure
- item, repeat 등 실제 데이터들을 처리하고 핸들링하는 클래스들

## 5. Job, Step, Tasklet

### 5-1. Job : 일, 일감. Step의 집합

- Job : 배치 계층 구조 최상위. 하나의 배치 작업 자체
  - SimpleJob : 순차적으로 Step 실행
  - FlowJob : 특정한 조건과 흐름에 따라 Step 실행
    - Step이 실패하더라도 Job은 실패로 끝나지 않도록 해야하는 경우
    - 조건에 따라 다음 실행할 Step을 구분해서 실행해야하는 경우
    - 특정 Step은 전혀 실행되지 않게 구성해야하는 경우
- JobInstance
  - 고유하게 식별 가능한 작업 실행
  - ex) 오늘 실행한 A Job과 내일 실행한 A Job은 같은 Job이지만, 다른 인스턴스이고, 실행 시기, 내용 등이 다름
  - JobName + jobKey(JobParameters의 해시값) 을 식별값으로 가짐 -> 같은 JobKey를 가지고 있는 JobInstance는 중복해서 실행 불가
  - BATCH_JOB_INSTANCE 테이블과 매핑
- JobExecution
  - JobInstance에 대한 한번의 시도를 의미하는 객체
  - 시작/종료시간, 상태(시작,완료,실패... ), 실행 결과
  - 상태 COMPLETED -> JobInstance 재실행 불가
  - 상태 FAILED -> JobInstance 재실행 가능(JobInstance가 재생성된다는 뜻은 아님)
  - COMPLETED 상태에서 재실행 시 JobExecution 생기지 않음
- JobParameter
  - Job 실행 시 파라미터로 사용가능
  - value와 타입(String, Date, Long, Double)을 담고있는 객체
  - JobParameters 에 key, value 형태로 저장
  - 하나의 Job에 존재할 수 있는 여러 JobInstance 구분
  - JobParameter : JobInstance = 1:1
  - BATCH_JOB_EXECUTION_PARAM 테이블과 매핑
  - JOB_EXECUTION 과 1:M 관계
  - JobParametersBuilder 혹은 java jar 파일 실행 시 파라미터로도 입력 가능
```shell
# 그냥 입력하면 long, double, date 인식을 못해서 괄호로 명시해줘야함
# zsh 에서는 () 포함한 인자 전달 시 문제 발생해서 ''로 감싸줘야함
java -jar build/libs/spring-batch-0.0.1-SNAPSHOT.jar 'name=user1' 'seq(long)=2L' 'date(date)=2024/12/19' 'age(double)=16.5'
```
- JobLauncher
  - Job 실행. Job과 JobParameters를 인자로 받아서 실행
  - client에게 JobExecution 반환
  - JobLauncherApplicationRunner 가 자동적으로 JobLauncher 실행
    - BatchAutoConfiguration 에서 생성 
    - Bean으로 등록된 모든 Job 실행
  - 실행 설정
    - 동기적 실행
    - 비동기적 실행
      - taskExecutor 를 SimpleAsyncTaskExecutor 로 설정할 경우
      - JobExecution 을 획득한 후 Client 에게 바로 JobExecution 을 반환하고 배치처리 완료
      - HTTP 요청에 의한 배치처리에 적합
```shell
# 1. application.yaml에서 spring.batch.job.names 에 하드코딩하거나
# 2. application.yaml에서 spring.batch.job.names=${job.name:NONE}--job.name 을 파라미터로 보내면 원하는 Job 만 수행 가능
java -jar build/libs/spring-batch-0.0.1-SNAPSHOT.jar --job.name=helloJob,simpleJob 'name=user1' 
```
- JobRepository
  - Job 실행 중 발생하는 메타데이터를 DB에 저장
  - 시작/종료 시간, 실행 횟수, 결과 등 저장
  - JobLauncher, Job, Step 구현체 내부에서 배치 관련 도메인을 DB에 CRUD 하는 데 사용
  - BatchConfigurer를 구현하거나 BasicBatchConfigurer을 상속해서 커스터마이징 가능
    - JobRepositoryFactoryBean : JDBC 방식. isolation level : serializable
    - MapJobRepositoryFactoryBean : 인메모리 방식
- JobBuilderFactory
  - JobBuilder 생성
  - JobBuilder의 API 활용 시 아래 빌더 생성
    - SimpleJobBuilder : SimpleJob 생성
    - JobFlowBuilder : Flow 생성
      - FlowJobBuilder 의존 : FlowJob 생성
        - on(String pattern) : TransitionBuilder 를 생성 후 Step 간 조건부 전환 구성. to(), stop(), fail(), end(), stopAndRestart() 를 통해 FlowBuilder 리턴
          - StepExecution의 ExitStatus에 따라 무엇을 수행할지(to, stop, fail, end, stopAndRestart) 결정
          - 특수문자로 *, ? 허용. * : 모든 문자, ? : 1개의 문자
          - 구체적인 pattern이 먼저 적용
        - from() : 이전 단계에서 정의한 Transition 새롭게 추가 정의. else if 느낌?
        - end() : Step의 ExitStutus가 FAILED 여도, Job의 BatchStatus가 COMPLETED로 종료하도록 하여 Job의 재시작이 불가능하도록 함

### 5-2. Step : 일의 단계. Tasklet을 실행
- Step
  - Job을 구성하는 독립적인 하나의 단계
  - 입력, 처리, 출력과 관련된 복잡한 비즈니스 로직을 포함
  - TaskletStep : 가장 기본 구현체. Tasklet 타입의 구현체 제어
  - PartitionStep : 멀티 스레드 방식으로 Step을 여러개로 분리해서 실행
  - JobStep : Step 내 Job 실행. 커다란 시스템을 작은 모듈로 쪼개고, job의 흐름을 관리하고자 사용.
  - FlowStep : Step 내 Flow 실행
- StepExecution
  - Step에 대한 한번의 실행
  - 시작/종료 시간, 상태, commit count, rollback count
  - Step이 매번 시도될 때마다 생성. Step 별로 생성
  - default : Job이 실패하면, 성공한 Step은 건너뛰고 실패한 Step부터 시작(옵션 설정 시 처음부터 다시 실행 가능)
  - BATCH_STEP_EXECUTION 과 매핑
  - JobExecution 과 1:M 관계
- StepContribution
  - chunk 기반 프로세스 관련 정보를 StepExecution에 전달
  - StepExecution의 apply()를 수행하면 StepExecution의 chunk 관련 정보가 StepExecution에 전달. 이 후 DB 저장
- StepBuilder
  - TaskletStepBuilder : tasklet(tasklet). TaskletStep을 생성하는 기본 빌더 클래스. 주로 사용자 정의 Tasklet 실행
  - SimpleStepBuilder : chunk(chunkSize) or chunk(completionPolicy).TaskletStep을 생성하며, 내부적으로 청크기반의 작업을 처리하는 ChunkOrientedTasklet 클래스 생성
  - PartitionStepBuilder : PartitionStep 생성. 멀티스레드 방식으로 Job 실행
  - JobStepBuilder : job(job)
  - FlowStepBuilder : flow(flow)
- TaskletStep
  - Tasklet 실행하는 Step
  - RepeatTemplate을 사용해서 Tasklet을 트랜잭션 경계 내에서 반복 실행(RepeatStatus를 조정해서 종료 가능. 디폴트 값은 반복)
    - Tasklet은 Transaction 처리가 되어 실행된다는 의미(?)
  - Task 기반과 Chunk 기반으로 나누어서 Tasklet 실행
  - Task 기반 : 청크 기반 작업보다 단일 작업 기반으로 처리되는 것이 더 효율적인 경우
  - Chunk 기반 : 하나의 큰 덩어리를 n개씩 나누어서 실행. ItemReader, ItemProcessor, ItemWriter 사용. ChunkOrientedTasklet 구현체 제공.

### 5-3. Tasklet : 실제 수행 비즈니스 로직. Step에서 수행하는 Task 정의
- Tasklet
  - Step : Tasklet = 1:1
  - Step 내에서 실행되며, 주로 단일 태스크 수행하기 위함
  - RepeatStatus : Tasklet의 반복 여부 상태값
    - FINISHED : 종료(null)
    - CONTINUABLE : 계속 수행

### 5-4. ExecutionContext
- ExecutionContext
  - key, value 컬렉션으로, JobExecution, StepExecution 객체의 상태(state)를 저장하는 공유 객체
  - DB에 직렬화한 값으로 저장(json)
- 공유 범위
  - Step 범위 : StepExecution에 저장. Step간 공유 x. Listener, Tasklet 등에서 가져가서 사용.
  - Job 범위 : 각 Job의 JobExecution에 저장. Job간 공유 x. 해당 Job의 Step들 간에는 공유.
- 주 사용 용도 : 
  - Job 실패 후 재 시작시, 이미 성공한 Step은 건너뛰고, 실패한 Job부터 실행 시 해당 상태정보 활용
  - Caching
- BATCH_JOB_EXECUTION_CONTEXT, BATCH_STEP_EXECUTION_CONTEXT 에 대응
- ExecutionContext에 넣은 값은 별도의 조치가 없는 이상 Job 실행 도중 에러가 발생해도 롤백되지 않는다! -> 재시작 등의 로직에 활용 가능

## 6. Spring batch metadata

- 배치 도메인(Job, Step, JobParameters) 관련 정보
- 스프링 배치 실행 및 관리를 위해 이러한 도메인 정보를 저장, 업데이트, 조회할 수 있는 스키마 제공
  - 실행 정보, 성공 실패 여부 등
  - DB와 연동 시 필수적으로 메타 테이블이 생성 되어야함
- org/springframework/batch/core/schema-*.sql 형태로 제공. 즉, 해당 모듈에 가보면 sql 파일 존재
  - DB 유형별로 제공
- 스키마 생성 설정
  - 수동 생성 : 직접 쿼리 복붙
  - 자동 생성 : spring.batch.jdbc.initialize-schema  설정
    - ALWAYS
    - EMBEDDED
    - NEVER

### 6-1. Job 관련 테이블
- BATCH_JOB_INSTANCE
  - Job 실행 시 JobInstance 정보 저장. jobname과 jobkey를 키로 하여 하나의 데이터 저장
- BATCH_JOB_EXECUTION
  - Job의 실행정보 저장
  - Job 생성, 시작/종료시간, 실행상태, 메시지 관리
  - BatchStatus
    - Job, Step의 실행상태 및 최종 결과 상태
  - ExitStatus
    - Job, Step 이 어떤 상태로 종료되었는 지 정의
- BATCH_JOB_EXECUTION_PARAMS
  - JobParameter 정보 저장
- BATCH_JOB_EXECUTION_CONTEXT
  - Job 실행동안 여러 상태정보, 공유데이터를 직렬화(json)해서 저장
  - Step 간 공유 가능

### 6-2. Step 관련 테이블
- BATCH_STEP_EXECUTION
  - Step의 실행정보 저장
  - Step 생성, 시작/종료 시간, 실행상태, 메시지 관리
- BATCH_STEP_EXECUTION_CONTEXT
  - Step 실행동안 상태정보, 공유데이터 직렬화해서 저장
  - Step 별로 저장, Step 간 공유 불가

## 7. 스프링 배치 청크 프로세스(1)

### 7-1. Chunk

- chunk : 여러 개의 아이템을 묶은 하나의 덩어리
- 한번에 하나씩 아이템을 입력받아 Chunk 단위의 덩어리로 만든 후, ***Chunk 단위로 트랜잭션을 처리***
- Chunk<I> vs Chunk<O>
  - Chunk<I> : ItemReader가 item을 읽어와서 chunkSize 만큼 item이 담긴 Chunk를 생성
  - Chunk<O> : ItemProcessor가 item을 하나하나 trasform하여 <O> 타입으로 변형하여 Chunk<O>에 적재
  - ItemWriter에서 Chunk<O>를 처리

### 7-2. ChunkOrientedTasklet

- Chunk 기반 프로세싱을 담당하는 도메인 객체
- ItemReader, ItemProcessor, ItemWriter 를 사용해 Chunk 기반 입출력 처리
- TaskletStep에 의해 반복적으로 실행되며, ***실행될 때마다 새로운 트랜잭션 생성*** 및 처리
- ChunkProvider
  - ItemReader 핸들링
    - 읽는 소스 종류
      - 플랫 파일 : csv, txt 등
      - XML, Json
      - Database
      - Message Queue : JMS, RabbitMQ
      - CustomReader
    - 보통 ItemStream과 함께 구현
      - ItemStream : 파일의 스트림을 열거나 종료. DB 커넥션을 열거나 종료 등
  - 구현체 : SimpleChunkProvider, FaultTolerantChunkProvider
- ChunkProcessor
  - ItemProcessor(변형, 가공, 필터링), ItemWriter(저장, 출력) 핸들링
  - ItemProcessor
    - ItemProcessor에서 null 반환 시 ItemWriter에 전달 x
  - ItemWriter
    - 쓰는 목적지 종류
      - 플랫 파일 : csv, txt 등
      - XML, Json
      - Database
      - Message Queue : JMS, RabbitMQ
      - Mail Service
      - Custom Writer
    - 아이템 하나가 아닌 ***아이템 리스트***를 전달. 일괄처리
    - 보통 ItemStream을 같이 구현. ItemReader 구현체와 1:1 매핑
  - 구현체 : SimpleChunkProcessor, FaultTolerantChunkProcessor
- API
  - chunk(int) : chunk size 설정. commit interval 의미
  - chunk(CompletionPolicy) : Chunk 프로세스를 완료하기 위한 정책 설정 클래스 지원. chunk(int)가 숫자 조건이라면, 이 api는 정책 조건
  - stream(ItemStream) : 재시작 데이터를 관리하는 롤백에 대한 스트림 등록. execution context 활용하여 재시작 시 활용
  - readerIsTransactionalQueue() : Item이 JMS, Message Queue Server와 같은 트랜잭션 외부에서 읽혀지고 캐시할 것인지
  - listener(ChunkListener) : Chunk 프로세스 진행되는 특정 시점에 콜백 제공받도록 리스너 설정
- 이중 반복문으로 이해하면 됨
  - 1. ItemReader에서 데이터가 없을 때까지 chunk size 단위로 반복(총 100개를 10개 씩)
  - 2. chunk size 만큼 순회(10개 데이터를 하나씩)

## 8. 스프링 배치 청크 프로세스(2)

### 8-1. Cursor & Paging

Cursor Based
- JDBC ResultSet의 기본 메커니즘 사용
- 현재 행에 커서를 유지하며, 다음 데이터를 호출하면 다음 행으로 커서를 이동하며 데이터 반환이 이루어지는 Steaming 방식의 I/O
- ResultSet 이 open 될 때마다 next() 메소드가 호출되어 DB 데이터 반환 및 객체와 매핑이 이루어진다
- DB Connection이 연결되면 배치 처리가 완료될 때까지 데이터를 읽어옴 -> DB와 SocketTimeout 충분히 크게 설정 필요
- 모든 결과를 메모리에 할당 -> 메모리 사용량 많음
- fetchSize 조절 가능 -> 한번 커서를 이동할 때 fetchSize 만큼의 데이터를 가져옴(?)

Paging Based
- Page Size 만큼 조회
- offset, limit

## 99. 기타

### 99-1. 사용자 정의 ExitStatus
- ExitStatus에 존재하지 않는 exitCode 새롭게 정의 -> ExitStatus는 Enum이 아니다!
- afterStep에서 특정 조건에 따라 커스텀한 ExitStatus를 return 하는 StepExecutionListner를 정의 후, step에 붙여주면 다른 ExitStatus 리턴 가능