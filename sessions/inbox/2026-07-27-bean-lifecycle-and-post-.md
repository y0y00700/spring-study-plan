---
date: 2026-07-27
topic: Spring Bean 초기화 생명주기와 BeanPostProcessor
processed: true
environment: company-pc
---

# Spring Bean 초기화 생명주기와 BeanPostProcessor

## 세션 목표

- 객체 인스턴스화와 Spring 초기화를 구분한다.
- 생성자 이후 초기화 콜백과 BeanPostProcessor의 실행 순서를 검증한다.
- BeanPostProcessor가 반환한 프록시가 컨테이너에 공개되는 과정을 확인한다.

## 시작 전 회상

### Reflection

- `Method.isAnnotationPresent()`는 메서드 본문을 실행하지 않고 annotation 존재 여부만 조사한다.
- 조회 대상은 클래스가 아니라 해당 `Method`가 나타내는 메서드다.
- `Method.invoke(target)`는 annotation 존재 여부와 무관하게 대상 메서드를 호출한다.

### 생성자 의존성 해결

다음 순서를 회상했다.

1. `OrderService` Bean 정의 등록
2. `PaymentProcessor` 후보 탐색
3. `PaymentProcessor` 생성
4. 생성된 참조를 생성자 인자로 준비
5. `OrderService` 생성자 호출

Bean 정의 등록과 실제 의존성 해결 시점을 구분할 필요가 있음을 확인했다. 생성자 매개변수 후보 해결은 `OrderService` 객체 생성 단계에서 수행된다.

## 핵심 개념 1: 인스턴스화와 초기화

### 인스턴스화

생성자를 호출하여 실제 객체를 만드는 단계다.

### 초기화

객체가 생성되고 필요한 의존관계와 컨테이너 정보가 설정된 뒤, 검증이나 준비 작업을 위한 초기화 콜백을 실행하는 단계다.

초기화는 반드시 객체의 상태를 변경하는 작업만을 의미하지 않는다.

## 핵심 개념 2: 초기화 콜백과 BeanPostProcessor

### `@PostConstruct`

- 해당 Bean 내부에 작성하는 초기화 콜백이다.
- annotation은 스스로 실행되지 않는다.
- Spring의 후처리기가 annotation을 찾아 메서드를 호출한다.
- 의존관계가 설정된 이후 실행된다.
- 생성자 직후 자동 실행되는 Java 생성자 기능이 아니다.

### `InitializingBean.afterPropertiesSet()`

- Spring이 제공하는 초기화 콜백 인터페이스다.
- 필요한 프로퍼티와 의존관계가 설정된 뒤 호출된다.
- `@PostConstruct`와 함께 사용하면 `@PostConstruct`가 먼저 호출된다.

### `BeanPostProcessor`

- 개별 Bean 내부의 초기화 코드가 아니라 컨테이너 차원의 공통 확장 장치다.
- 여러 Bean을 검사하거나 가공할 수 있다.
- 반환값으로 원본과 다른 객체 또는 프록시를 제공할 수 있다.
- 반환된 객체가 이후 컨테이너에서 공개할 Bean 참조가 된다.

## 첫 번째 실험

### 파일

`labs/spring-lab/src/test/java/study/lifecycle/BeanLifecycleOrderTest.java`

### 테스트

`beanPostProcessorWrapsInitializationCallback`

### 검증한 순서

```text
constructor
→ setBeanName
→ beforeInitialization
→ postConstruct
→ afterPropertiesSet
→ afterInitialization
```

## 두 번째 실험

### 테스트

`postProcessorReturnValueBecomesBeanExposedByContext`

### 검증한 내용

- `postProcessAfterInitialization()`이 원본 `GreetingService` 대신 `GreetingServiceProxy`를 반환한다.
- 컨텍스트에서 이름으로 조회한 객체는 원본 객체와 동일하지 않다.
- 조회 결과는 `GreetingServiceProxy` 타입이다.
- 프록시의 `greet()` 호출은 대상 객체에 위임되어 `"proxy -> target"`을 반환한다.

## 집 PC 정리 및 검증

- 2026-07-28 `.\gradlew.bat test`를 실행했다.
- 전체 테스트 14개가 실행됐고 실패, 오류, 건너뜀 없이 모두 성공했다.
- `BeanLifecycleOrderTest`의 두 테스트가 모두 성공한 뒤 `processed`를 `true`로 변경했다.
