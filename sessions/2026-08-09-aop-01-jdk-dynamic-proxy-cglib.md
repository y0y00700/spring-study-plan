# 2026-08-09: AOP-01 JDK Dynamic Proxy와 CGLIB

## 진행 상태

- roadmap_item: `AOP-01 JDK Dynamic Proxy와 CGLIB`
- completion_status: `needs_review`
- recommended_next: `AOP-01 호출 위임 복습 후 같은 세션에서 AOP-02 진행`

## 지난 세션 회상 복습

- 정상 요청의 Filter·Interceptor·AOP·Controller 진입과 복귀 순서를 회상했다.
- Filter 전처리 예외가 DispatcherServlet에 진입하지 않아 `@RestControllerAdvice`로 처리되지 않는 이유를 설명했다.
- `HandlerExceptionResolver`와 `ExceptionHandlerExceptionResolver` 용어를 다시 구분했다.

## 학습 전 내 생각

- JDK 프록시는 인터페이스, CGLIB 프록시는 구현 클래스와 타입 관계가 있다고 예상했지만 두 프록시의 원본 참조·런타임 클래스 동일성은 정확히 판단하지 못했다.
- CGLIB 프록시가 구현 클래스의 하위 타입이라는 점은 예상했지만, 그 구현 클래스가 구현한 인터페이스 타입도 함께 만족한다는 점은 불명확했다.
- CGLIB 프록시를 구현 클래스 타입으로 캐스팅한 참조가 기존 프록시와 다른 참조일 것으로 예상했다.
- `final` 클래스의 CGLIB 프록시 생성이 성공할 것으로 예상했고 `final` 메서드의 영향은 설명하지 못했다.

## 핵심 설명

- 프록시는 원본과 별도의 객체로 호출자 앞에서 Advice를 실행한 뒤 원본 객체에 호출을 위임한다.
- JDK Dynamic Proxy는 대상 인터페이스를 구현하는 런타임 클래스를 만들고, CGLIB는 구현 클래스를 상속하는 런타임 클래스를 만든다.
- JDK 프록시는 구현 클래스 타입에 대입할 수 없지만 CGLIB 프록시는 구현 클래스의 하위 타입이므로 대입할 수 있다.
- CGLIB는 `final class`를 상속할 수 없어 프록시 생성에 실패하며, `final method`는 재정의할 수 없어 Advice가 적용되지 않는다.

## 실험

- 기준 디렉터리: `labs/spring-lab`
- 파일: `src/test/java/aop/JdkDynamicProxyCglibTest.java`
- 클래스: `JdkDynamicProxyCglibTest`
- 메서드:
  - `jdkDynamicProxyRuntimeTypeAndDelegation()`
  - `cglibProxyRuntimeTypeAndDelegation()`
  - `concreteClassCastDependsOnProxyStrategy()`
  - `cglibProxyCreationFailsForFinalClass()`
  - `cglibProxyCannotInterceptFinalMethod()`
  - `adviceCanReturnWithoutCallingTarget()`
- 학습자가 각 테스트의 타입 관계, 호출 이벤트, 캐스팅 결과와 `final` 제약의 핵심 예상값을 직접 작성했다.

## 실제 결과

- 두 프록시 모두 원본과 참조 및 런타임 클래스가 달랐다.
- JDK 프록시는 `PaymentService` 타입이지만 `PaymentServiceImpl` 타입은 아니었다.
- CGLIB 프록시는 `PaymentServiceImpl`의 하위 타입이며 `PaymentService` 타입도 만족했다.
- 두 방식 모두 `advice-before → target → advice-after-returning → advice-finally` 순서로 호출을 위임했다.
- JDK 프록시의 구현 클래스 캐스팅은 `ClassCastException`이 발생했고 CGLIB 프록시 캐스팅은 성공했으며 캐스팅 전후 참조가 같았다.
- `final` 클래스는 CGLIB 프록시 생성에 실패했고, 상속 가능한 클래스의 `final` 메서드는 호출됐지만 Advice가 적용되지 않았다.
- `proceed()`를 호출하지 않고 `"blocked"`를 반환한 Advice에서는 원본 `target.pay()`가 실행되지 않고 프록시가 `"blocked"`를 반환했다.
- 프록시 테스트 6개와 전체 Gradle 테스트 45개가 성공했고 실패·오류·건너뜀은 0개였다.

## 실패 분석

- JDK 프록시 테스트에서 원본과 프록시의 런타임 클래스가 같다고 작성해 첫 실행이 실패했다. 별도의 프록시 클래스가 생성된다는 점을 반영해 수정했다.
- 정상 반환에서도 Advice의 `finally`가 실행되는데 예상 이벤트에서 빠뜨려 보완했다.
- `final` 클래스의 프록시 생성이 성공한다고 예상해 테스트가 실패했다. CGLIB의 하위 클래스 생성 방식과 Java의 `final` 상속 금지를 연결해 수정했다.

## 내가 다시 설명하기

- JDK 프록시는 인터페이스를 구현하고 CGLIB 프록시는 구현 클래스를 상속한다고 구분했다.
- JDK 프록시는 구현 클래스와 상속 관계가 없어 구현 클래스 타입 캐스팅이 실패하고, CGLIB 프록시는 구현 클래스의 하위 타입이라 가능하다고 설명했다.
- `final class`는 프록시 생성 단계에서 실패하고 `final method`는 프록시 생성은 가능하지만 Advice를 우회한다고 설명했다.

## 남은 질문

- `invocation.proceed()`가 호출되지 않을 때 원본 target 메서드와 반환값이 어떻게 되는지 재설명이 필요하다.
- 짧은 차단 Advice 실험으로 반환값과 target 호출 여부를 확인한 뒤 `AOP-01` 완료 여부를 다시 판단한다.

## 사용자 요청에 따른 다음 세션 순서

- 다음 강의에서 `AOP-01` 복습과 `AOP-02` 학습을 함께 진행한다.
- `AOP-02`의 선수 항목을 지키기 위해 `AOP-01`의 `invocation.proceed()` 호출 위임을 먼저 재설명하고 완료 여부를 판단한다.
- 복습을 통과하면 세션을 끊지 않고 즉시 `AOP-02 Advice 적용과 self-invocation`의 진단과 최소 실험으로 이어간다.
- 복습을 통과하지 못하면 `AOP-01 needs_review`를 유지하고 `AOP-02`는 시작하지 않는다.
- 변경 이유: 두 연결 주제를 같은 강의에서 연속해서 진행하겠다는 사용자의 명시적 요청.

## 회상 문제

1. JDK 프록시와 CGLIB 프록시의 런타임 타입 관계를 원본 객체와 비교한다.
2. 구현 클래스 타입 주입이 프록시 방식에 따라 달라지는 이유를 설명한다.
3. `final class`와 `final method`가 CGLIB에 미치는 영향을 발생 시점과 함께 구분한다.

## 면접 질문

1. JDK Dynamic Proxy와 CGLIB의 생성 방식, 타입 제약, 호출 위임을 비교한다.
2. 인터페이스 타입 대신 구현 클래스 타입으로 주입받을 때 프록시 전략 변경이 어떤 장애를 만들 수 있는지 설명한다.

## 다음 복습일

2026-08-12
