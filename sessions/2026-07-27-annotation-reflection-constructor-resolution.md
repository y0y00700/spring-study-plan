# 2026-07-27: Annotation Reflection과 생성자 의존성 해결

## 학습 전 내 생각

- `isAnnotationPresent()`는 annotation 존재 여부를 반환할 것으로 예상했다.
- `Method.invoke()`의 역할을 몰라 대상 메서드가 실제 실행되는지는 예측하지 못했다.
- Spring이 생성자와 매개변수 정보를 Reflection으로 언제 확인하는지 설명하지 못했다.

## 튜터의 질문

1. annotation 존재 확인과 대상 메서드 실행은 왜 별개의 동작인가?
2. `@Retention(CLASS)`로 바꾸면 실행 중 annotation 조회와 메서드 호출 결과는 어떻게 되는가?
3. BeanFactory는 `OrderService(PaymentProcessor)`를 어떤 순서로 생성하는가?
4. annotation을 실제 기능으로 만드는 주체는 누구인가?

## 실행 결과 예측

- `RUNTIME` annotation은 발견되어 `present == true`일 것으로 예측했다.
- annotation 확인 직후에는 `pay()`가 실행되지 않아 `callCount == 0`일 것으로 예측했다.
- `method.invoke(service)`가 실제 호출이라는 설명 후 `callCount == 1`로 예측을 수정했다.
- `CLASS` annotation은 실행 중 Reflection에 노출되지 않아 `present == false`지만, `invoke()`는 독립적으로 실행되어 `callCount == 1`일 것으로 예측했다.
- 의존 객체가 아직 없으면 `PaymentProcessor 생성 → OrderService 생성` 순서이고, 기존 Singleton이 있으면 이를 재사용할 것으로 예측했다.

## 예상의 근거

- annotation은 메타데이터이며 메서드 호출과 별개라고 판단했다.
- 생성자를 호출하려면 매개변수로 전달할 의존 객체가 먼저 준비되어야 한다고 판단했다.

## 실험

### Annotation 조회와 Method 호출 분리

- `@Retention(RUNTIME)`인 `@Tracked`가 붙은 `pay()`를 `Method.isAnnotationPresent()`로 조회했다.
- 조회 직후와 `Method.invoke()` 이후의 호출 횟수를 assertion으로 검증했다.

### CLASS 보존 정책 반례

- `@Retention(CLASS)`인 `@ClassTracked`를 사용했다.
- 실행 중 annotation은 발견되지 않지만 `Method.invoke()`로 메서드는 실행되는지 검증했다.

### Spring 생성자 의존성 해결 순서

- Reflection으로 `OrderService` 생성자의 첫 매개변수 타입이 `PaymentProcessor`인지 검증했다.
- `PaymentProcessor`를 지연 생성으로 등록하고 `OrderService`를 기본 Singleton으로 등록했다.
- 컨텍스트 초기화 시 생성 이벤트가 `PaymentProcessor 생성 → OrderService 생성` 순서인지 검증했다.

## 실제 결과

- `RUNTIME`: `present == true`, 조회 직후 `callCount == 0`, 호출 후 `callCount == 1`.
- `CLASS`: `present == false`, 호출 후 `callCount == 1`.
- 생성자 매개변수 타입은 `PaymentProcessor`였다.
- 객체 생성 순서는 `PaymentProcessor 생성 → OrderService 생성`이었다.
- 2026-07-27 전체 Gradle 테스트 12개 성공, 실패 0개.

## 예상과 달랐던 부분

- 처음에는 `Method.invoke()`가 실제 대상 메서드를 호출한다는 사실을 몰랐다.
- `method.isAnnotationPresent()`의 조회 대상을 클래스라고 표현했지만, 실제 대상은 해당 `Method`가 나타내는 메서드다.
- `OrderService` 생성자 호출 시점에 후보 객체를 확인한다고 표현했지만, 실제로는 호출 전에 후보 Bean 정의를 검색하고 의존 객체를 준비한다.
- Spring의 모든 annotation 탐색이 Java Reflection인 것은 아니며 `.class` 메타데이터를 직접 읽는 경로도 있다.

## 내가 다시 설명하기

- annotation은 메타데이터이고, Spring 같은 처리 주체가 읽고 해석해야 실제 등록·객체 생성 등의 동작이 발생한다.
- `isAnnotationPresent()`는 annotation 존재 여부를 조회할 뿐 대상 메서드를 실행하지 않는다.
- `Method.invoke(target)`는 annotation 존재 여부와 독립적으로 대상 메서드를 실제 호출한다.
- BeanFactory는 생성자 호출 전에 매개변수 타입을 확인하고 후보 Bean 정의를 선택한다.
- 선택한 Singleton이 있으면 조회하고 없으면 먼저 생성한 뒤, 그 객체 참조를 생성자 인자로 전달한다.

## 남은 질문

- Spring 내부에서 생성자 선택, 의존성 해결, 생성자 호출을 담당하는 실제 클래스와 메서드는 무엇인가?
- Bean 생성 이후 초기화 콜백과 BeanPostProcessor는 어떤 순서로 실행되는가?

## 회상 문제

1. `Method.isAnnotationPresent()`의 조회 대상과 `Method.invoke()`의 실행 효과는 어떻게 다른가?
2. `RUNTIME`과 `CLASS` 보존 정책은 실행 중 Reflection 조회 결과에 어떤 차이를 만드는가?
3. BeanFactory는 생성자 호출 전에 의존 객체를 어떻게 준비하는가?

## 면접 질문

1. “annotation을 붙이면 기능이 자동 실행된다”는 설명이 왜 부정확한지 처리 주체를 포함하여 설명해보세요.
2. Spring 생성자 주입 과정을 Bean 정의 후보 검색, Singleton 조회·생성, 생성자 호출 순서로 설명해보세요.

## 다음 복습일

2026-07-30
