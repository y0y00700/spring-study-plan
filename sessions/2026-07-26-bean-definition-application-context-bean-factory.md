# 2026-07-26: Bean 정의와 ApplicationContext·BeanFactory

## 학습 전 내 생각

- Spring은 `OrderService`를 생성하기 위해 `PaymentProcessor`를 등록하거나 찾은 뒤, 주입 시점에 동일 타입 후보가 둘이면 실패할 것으로 예상했다.
- 후보 충돌 시 `OrderService`는 생성되기 전이라고 예상했다.
- Bean 정의의 타입 정보만으로 객체 생성 전 후보 수를 판단할 수 있을 것으로 예상했지만 이유는 설명하지 못했다.
- `ApplicationContext`가 내부 `BeanFactory`에 핵심 Bean 작업을 위임할 것으로 예상했지만 두 역할의 차이는 설명하지 못했다.

## 튜터의 질문

1. Spring은 객체를 생성하지 않고 동일 타입 Bean 후보가 둘이라는 것을 어떻게 알 수 있는가?
2. `@Lazy` 후보 둘과 선택 규칙이 없을 때 후보 객체 생성 횟수는 몇 번인가?
3. `ApplicationContext`와 `BeanFactory`는 각각 어떤 책임을 담당하는가?
4. 두 조회 경로로 얻은 기본 Singleton Bean은 같은 참조인가?
5. 기본 Singleton과 `@Lazy` Singleton의 생성 시점은 어떻게 다른가?

## 실행 결과 예측

- `@Lazy` 문법은 몰랐지만 문맥상 후보 객체 생성 횟수는 `0`일 것으로 예측했다.
- `ApplicationContext.getBean()`은 설정 분석 결과를 반환하고 내부 `BeanFactory.getBean()`은 자신이 생성한 객체를 반환하므로 두 객체가 다를 것으로 예측했다.
- 기본 Singleton을 세 번 조회해도 생성자는 한 번만 호출될 것으로 예측했다.

## 예상의 근거

- Bean 정의에 타입 정보가 있으므로 객체를 생성하지 않고 후보를 판별할 수 있다고 생각했다.
- Singleton은 한 번 만든 객체를 저장한 뒤 반복 반환한다고 이해했다.

## 실험

### @Lazy 후보 생성 여부

- `LazyPaymentBeanConfig`에 카카오·네이버 `PaymentProcessor` Bean을 `@Lazy`로 등록했다.
- 두 Bean 생성 메서드가 호출될 때 `processorCreationCount`를 증가시키도록 했다.
- 선택 규칙 없는 `OrderService` 생성 중 후보 충돌을 발생시키고 생성 횟수가 `0`인지 검증했다.

### ApplicationContext와 BeanFactory의 조회 결과

- `PrimaryPaymentBeanConfig`로 컨텍스트를 정상 생성했다.
- `context.getBean(OrderService.class)`와 `context.getBeanFactory().getBean(OrderService.class)`를 각각 호출했다.
- 두 결과를 `assertSame`으로 비교했다.

## 실제 결과

- 동일 타입 후보 충돌은 발생했지만 두 `@Lazy` Bean 생성 메서드는 호출되지 않았다.
- 생성 횟수는 `0`이었다.
- `ApplicationContext`와 내부 `BeanFactory`에서 조회한 `OrderService`는 같은 객체 참조였다.
- 전체 Gradle 테스트 9개가 성공했다.

## 예상과 달랐던 부분

- 주입 지점의 매개변수 타입은 필요한 타입을 나타내는 검색 조건일 뿐이며, 후보 수는 등록된 Bean 정의들의 타입 정보에서 판별한다.
- `ApplicationContext.getBean()`은 설정 클래스 분석 결과를 반환하지 않는다. 실제 Bean 조회를 내부 `BeanFactory`에 위임한다.
- 기본 Singleton은 첫 `getBean()` 때 반드시 생성되는 것이 아니라, 일반적으로 컨텍스트 초기화 과정에서 미리 생성된다.
- `@Lazy` Bean은 첫 조회에만 한정되지 않고 다른 Bean의 의존성으로 실제 필요해지는 시점에도 생성된다.

## 내가 다시 설명하기

- Spring의 설정 처리기는 `@Bean` 메서드를 분석하여 메서드명, 반환 타입 등의 정보를 Bean 정의로 등록한다.
- `orderService(PaymentProcessor)`의 매개변수 타입은 검색 조건이고, 각 `PaymentProcessor` Bean 정의의 타입 정보는 후보 판별 근거다.
- `ApplicationContext`는 내부 `BeanFactory`를 준비하고 전체 초기화를 지휘한다.
- `BeanFactory`는 Bean 정의를 보관하고 의존성을 해결하며 Bean 객체의 생성, Singleton 저장과 반환을 담당한다.
- 기본 Singleton은 초기화 과정에서 미리 생성되고 `@Lazy` Bean은 실제로 필요할 때 생성된다.

## 남은 질문

- Spring이 Reflection으로 얻은 생성자·매개변수 정보는 Bean 정의와 의존성 해결 과정에 구체적으로 어떻게 사용되는가?
- Bean 생성 이후 초기화 콜백과 BeanPostProcessor는 어떤 순서로 실행되는가?

## 회상 문제

1. Spring은 Bean 객체를 생성하지 않고도 동일 타입 후보가 둘이라는 것을 어떻게 알 수 있는가?
2. `ApplicationContext`와 `BeanFactory`의 역할은 어떻게 다른가?
3. 기본 Singleton과 `@Lazy` Singleton의 생성 시점은 어떻게 다른가?

## 면접 질문

1. 동일 타입 Bean이 두 개일 때 `OrderService` 생성이 실패하는 과정을 Bean 정의 등록부터 설명해보세요.
2. `ApplicationContext.getBean()`과 내부 `BeanFactory.getBean()`이 같은 Singleton 객체를 반환하는 이유를 설명해보세요.

## 다음 복습일

2026-07-27
