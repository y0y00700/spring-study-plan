---
date: 2026-07-29
environment: company_pc_limited
roadmap_item: CON-09
completion_status: completed_pending_home_verification
recommended_next: CON-10
processed: false
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