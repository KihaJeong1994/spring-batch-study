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

Job : 일, 일감. Step의 집합

Step : 일의 단계. Tasklet을 실행

Tasklet : 실제 수행 비즈니스 로직