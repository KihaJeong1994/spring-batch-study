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
- JobInstance
  - 고유하게 식별 가능한 작업 실행
  - ex) 오늘 실행한 A Job과 내일 실행한 A Job은 같은 Job이지만, 다른 인스턴스이고, 실행 시기, 내용 등이 다름
  - JobName + jobKey(JobParameters의 해시값) 을 식별값으로 가짐 -> 같은 JobKey를 가지고 있는 JobInstance는 중복해서 실행 불가
  - BATCH_JOB_INSTANCE 테이블과 매핑
- JobLauncher
  - Job 실행. Job과 JobParameters를 인자로 받아서 실행
- JobRepository
  - Job 실행 중 발생하는 메타데이터를 DB에 저장

### 5-2. Step : 일의 단계. Tasklet을 실행

### 5-3. Tasklet : 실제 수행 비즈니스 로직

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