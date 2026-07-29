---
date: 2026-07-29
environment: company_pc_limited
roadmap_item: CON-10
completion_status: completed_pending_home_verification
recommended_next: ROADMAP_DETAIL 전체 확인 후 다음 WEB 항목 확정
processed: true
---

# CON-10 Container 종합 진단

## 진행 사유

CON-09는 회사 PC에서 최소 실험과 완료 기준을 충족했지만 집 PC 검증이 남아 있다.
사용자의 명시적인 요청에 따라 같은 대화에서 CON-10을 이어서 진행했다.
CON-09와 CON-10의 확정 진도 반영은 집 PC에서 테스트를 확인한 뒤 수행한다.

## 세션 목표

- 등록, 선택, 생성, 초기화, 공개, 소멸의 전체 흐름을 연결한다.
- 객체 생성과 의존관계를 누가 언제 결정하는지 설명한다.
- 새 Bean의 전체 생명주기를 실행 전에 예측하고 assertion으로 검증한다.

## 선수 항목

- CON-09 소멸 콜백과 Scope
- 회사 PC 실험 완료
- 집 PC 최종 검증 대기

## 최소 재현 실험

### 파일

`labs/spring-lab/src/test/java/com/example/springlab/container/ContainerLifecycleIntegrationTest.java`

### 테스트 메서드

`beanTravelsFromDefinitionRegistrationToDestruction`

### 실험 대상

- `ReportRepository`
- `ReportRepository`에 의존하는 `ReportService`
- 기본 non-lazy Singleton
- `ReportService`의 `@PostConstruct`와 `@PreDestroy`

### 예측한 이벤트 순서

```text
repository.constructor
service.constructor
service.postConstruct
service.preDestroy
```

명시적인 getBean() 호출 전까지 생성과 초기화가 완료되고, 컨텍스트 종료 시 service.preDestroy가 추가될 것으로 예측했다.
검증 내용
reportService BeanDefinition 등록
ReportRepository가 ReportService보다 먼저 생성됨
ReportService 생성자 다음에 @PostConstruct 실행
두 번 조회한 ReportService의 참조 동일성
주입된 ReportRepository와 조회한 Singleton의 참조 동일성
조회 시점에 초기화가 완료된 상태
반복 조회가 생성·초기화 이벤트를 추가하지 않음
컨텍스트 종료 시 @PreDestroy 실행

assertTrue(context.containsBeanDefinition("reportService"));

assertEquals(
        List.of(
                "repository.constructor",
                "service.constructor",
                "service.postConstruct"
        ),
        events
);

var eventsAfterInitialization = List.copyOf(events);

assertSame(service1, service2);
assertSame(repository, service1.repository);
assertTrue(service1.initialized);
assertEquals(eventsAfterInitialization, events);

assertEquals(
        List.of(
                "repository.constructor",
                "service.constructor",
                "service.postConstruct",
                "service.preDestroy"
        ),
        events
);

테스트 결과
사용자 실행 결과: 성공
회사 PC 제한으로 Codex가 독립적으로 실행하지 못함
집 PC에서 실제 파일과 전체 테스트 성공 여부 확인 필요
학습자 최종 설명과 보완
ApplicationContext 생성 시 설정 처리기가 @Bean 메타데이터를 분석하여 BeanDefinition을 등록한다.
등록 단계에서는 실제 객체 참조를 연결하지 않고 타입, 팩토리 메서드, Scope 등 생성에 필요한 메타데이터를 기록한다.
기본 non-lazy Singleton은 컨텍스트 초기화 과정에서 생성된다.
BeanFactory는 ReportService를 생성하기 전에 팩토리 메서드 매개변수의 타입을 확인하고 의존성 후보를 검색한다.
후보 Singleton이 없으면 먼저 생성·초기화·저장하고, 이미 있으면 저장된 객체를 조회한다.
확보한 ReportRepository 참조를 reportService(...) 설정 메서드에 전달한다.
설정 메서드는 그 참조를 ReportService 생성자에 전달해 객체를 생성한다.
Spring의 초기화 처리 과정에서 @PostConstruct가 호출된다.
생성과 초기화가 끝난 Singleton을 BeanFactory가 저장하고 이후 getBean()에서 반환한다.
같은 Singleton을 반복 조회해도 생성자와 @PostConstruct는 다시 실행되지 않는다.
컨텍스트를 닫으면 소멸 과정에서 @PreDestroy가 호출된다.
의존 BeanDefinition이 없다면 BeanFactory의 후보 검색 단계에서 실패하므로 서비스 생성자, @PostConstruct, Singleton 저장은 실행되지 않는다.
발견한 오개념 및 교정
의존 객체가 없으면 서비스 생성자에서 실패한다고 표현했다.
교정: BeanFactory가 생성자를 호출하기 전 의존성을 먼저 해결하므로 생성자 이전에 실패한다.

설정 처리기가 실제 의존관계를 등록 시점에 완성하는 것처럼 표현했다.
교정: 등록 시에는 메타데이터를 기록하고, 실제 후보 선택과 객체 참조 연결은 Bean 생성 시점에 BeanFactory가 수행한다.

ReportRepository를 인자로 전달해 다시 ReportRepository를 생성한다고 설명했다.
교정: 관리 중인 ReportRepository 참조를 전달받아 생성하는 객체는 ReportService다.

해결되지 않은 질문
없음
집 PC 처리 사항
CON-09 및 CON-10 테스트 파일의 실제 경로와 내용을 확인한다.
관련 테스트와 전체 테스트를 실행한다.
테스트 성공과 완료 기준을 확인한 뒤 CURRENT.md에 CON-09와 CON-10 완료를 반영한다.
확인된 오개념을 questions/misconceptions.md에 반영한다.
전체 ROADMAP_DETAIL.md를 확인한 뒤 정확한 다음 WEB 항목을 결정한다.
모든 반영과 검증이 끝난 뒤에만 각 inbox 파일의 processed를 true로 변경한다.

## 집 PC 검증 결과

- 검증일: 2026-07-30
- 실제 파일: `labs/spring-lab/src/test/java/com/study/springlab/container/ContainerLifecycleIntegrationTest.java`
- 관련 테스트와 전체 테스트가 성공했다.
- 전체 테스트 결과: 20개 실행, 실패·오류 0개
- 최소 실험, assertion, 실패 분석, 학습자의 재설명과 완료 기준을 확인해 `CON-10`을 완료로 확정했다.
- `ROADMAP_DETAIL.md`의 다음 항목을 확인해 다음 진행 항목을 `WEB-01 HTTP 요청과 응답 경계`로 확정했다.
