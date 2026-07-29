---
date: 2026-07-29
environment: company_pc_limited
roadmap_item: CON-09
completion_status: completed_pending_home_verification
recommended_next: CON-10
processed: true
---

# CON-09 소멸 콜백과 Scope

## 세션 목표

- 컨텍스트 종료와 소멸 콜백의 관계를 설명한다.
- Singleton과 Prototype의 생명주기에 대한 컨테이너 책임 범위를 비교한다.
- 컨텍스트 종료 시 Scope별 소멸 콜백 결과를 assertion으로 검증한다.

## 선수 항목

- CON-08 `@Configuration`과 `@Bean` 프록시: 완료
- 설정 클래스 프록시가 BeanFactory의 Scope 규칙을 적용하도록 `@Bean` 메서드 호출을 가로채는 흐름을 회상했다.
- `proxyBeanMethods=false`에서 직접 호출은 일반 Java 호출이지만, 메서드 매개변수 주입은 BeanFactory가 관리 객체를 전달한다는 차이를 설명했다.

## 최초 예측과 교정

최초에는 Prototype을 두 번 생성하면 컨텍스트 종료 시 `@PreDestroy`도 두 번 호출될 것으로 예측했다.

Spring은 Prototype Bean의 생성, 의존관계 설정, 초기화까지 담당한다.
그러나 인스턴스를 호출자에게 전달한 이후에는 소멸까지 추적하지 않는다.
따라서 컨텍스트를 종료해도 Prototype의 `@PreDestroy`는 자동 호출되지 않으며 실제 결과는 0회다.

## 최소 재현 실험

### 파일

`labs/spring-lab/src/test/java/com/example/springlab/container/BeanDestructionScopeTest.java`

### 테스트 메서드

`contextCloseInvokesSingletonDestroyButNotPrototypeDestroy`

### 실험 내용

- Singleton Bean을 두 번 조회했다.
- Prototype Bean을 두 번 조회했다.
- 객체 참조 동일성과 생성 횟수를 assertion으로 검증했다.
- 컨텍스트를 닫은 뒤 Scope별 소멸 콜백 횟수를 검증했다.
- 명시적인 `getBean()` 호출 전에 생성 횟수를 검사해 기본 non-lazy Singleton의 선행 생성을 검증했다.

### 핵심 assertion

```java
assertEquals(1, singletonCreated.get());
assertEquals(0, prototypeCreated.get());

assertSame(singleton1, singleton2);
assertNotSame(prototype1, prototype2);

assertEquals(1, singletonCreated.get());
assertEquals(2, prototypeCreated.get());

context.close();

assertEquals(1, singletonDestroyed.get());
assertEquals(0, prototypeDestroyed.get());
```

테스트 결과
사용자 실행 결과: 성공
집 PC에서 실제 파일과 테스트 성공 여부 재확인 필요
회사 PC 권한 제한으로 Codex가 독립적으로 실행하지는 못함
학습자 최종 설명
기본 non-lazy Singleton은 컨텍스트 초기화 시점에 생성된다.
Prototype은 BeanFactory에 요청할 때마다 생성된다.
Spring 컨테이너는 Prototype의 생성, 의존관계 설정, 초기화까지 담당한다.
Prototype 인스턴스를 전달한 이후의 추적과 소멸은 호출자 책임이다.
BeanDefinition은 객체 자체가 아니라 객체 생성에 필요한 메타데이터다.
Prototype을 반복해서 생성해야 하므로 인스턴스를 보관하지 않더라도 BeanDefinition은 컨테이너에 유지된다.
Singleton 객체가 자신의 전체 생명주기를 책임지는 것이 아니라, Spring 컨테이너가 Singleton 인스턴스를 저장·추적하고 컨텍스트 종료 시 소멸 콜백을 호출한다.
Prototype이 외부 자원을 보유한다면 호출자가 명시적으로 정리 메서드를 호출하도록 설계할 수 있다.
여러 Prototype 인스턴스를 관리하는 Singleton 관리 Bean을 둘 수도 있다. 이 경우 관리 Bean이 Prototype 참조를 보관하고 자신의 @PreDestroy에서 각 Prototype의 명시적인 정리 메서드를 호출해야 한다.
관리 Bean에 @PreDestroy를 붙이는 것만으로 Prototype의 @PreDestroy가 자동 호출되는 것은 아니다.
발견한 오개념 및 교정
Prototype을 두 번 생성하면 소멸 콜백도 두 번 호출될 것으로 예측했다.
교정: 컨테이너는 Prototype 인스턴스를 전달한 후 소멸까지 추적하지 않으므로 자동 소멸 콜백은 0회다.

기본 Singleton도 조회 시 생성된다고 설명했다.
교정: 이번 실험의 기본 non-lazy Singleton은 컨텍스트 초기화 중 미리 생성된다. @Lazy Singleton은 별도 경우다.

숫자 카운터 검증에 assertSame을 사용했다.
교정: 객체 참조에는 assertSame을 사용하고, 숫자 값에는 assertEquals를 사용한다.
작은 Integer 캐시로 인해 잘못된 assertSame이 우연히 통과할 수 있다.

Singleton 객체가 생성부터 소멸까지 스스로 책임지는 것으로 표현했다.
교정: Singleton 인스턴스를 생성·저장·추적하고 소멸 콜백을 호출하는 책임 주체는 Spring 컨테이너다.

해결되지 않은 질문
없음
집 PC 처리 사항
BeanDestructionScopeTest의 실제 경로와 내용을 확인한다.
관련 테스트를 실행해 성공 여부를 검증한다.
완료 기준 충족 여부를 확인한 뒤 CURRENT.md에 CON-09 완료를 반영한다.
확인된 오개념을 questions/misconceptions.md에 반영한다.
다음 진행 항목을 CON-10으로 확정한다.
모든 반영과 검증이 끝난 뒤에만 processed: true로 변경한다.

## 집 PC 검증 결과

- 검증일: 2026-07-30
- 실제 파일: `labs/spring-lab/src/test/java/com/study/springlab/container/BeanDestructionScopeTest.java`
- 관련 테스트와 전체 테스트가 성공했다.
- 전체 테스트 결과: 20개 실행, 실패·오류 0개
- 최소 실험, assertion, 실패 분석, 학습자의 재설명과 완료 기준을 확인해 `CON-09`를 완료로 확정했다.
