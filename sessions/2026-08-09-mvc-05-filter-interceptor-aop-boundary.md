# 2026-08-09: MVC-05 Filter, Interceptor, AOP 경계

## 진행 상태

- roadmap_item: `MVC-05 Filter, Interceptor, AOP 경계`
- completion_status: `completed`
- recommended_next: `AOP-01 JDK Dynamic Proxy와 CGLIB`

## 학습 전 내 생각

- 정상 요청의 실행 순서를 `filter-before → interceptor-preHandle → aop-before → controller → aop-after → interceptor-postHandle → interceptor-afterCompletion → filter-after`로 정확히 예측했다.
- AOP와 `SoldOutException`, `@RestControllerAdvice`, 409 응답의 의미는 처음에 설명하지 못했다.
- Controller 예외가 처리되더라도 `afterCompletion`이 `SoldOutException`을 받을 것으로 예측했다.

## 핵심 설명

- Filter는 Servlet Filter chain에서 DispatcherServlet 바깥의 HTTP 요청 전체를 감싼다.
- Interceptor는 DispatcherServlet 내부에서 선택된 Handler의 실행 전후와 MVC 완료 시점에 동작한다.
- Spring AOP는 Controller에 한정되지 않고 포인트컷과 일치하며 프록시를 통과한 Spring Bean 메서드 호출 경계에 적용된다.
- `postHandle`은 Handler가 정상 반환했을 때 실행되고, `afterCompletion`은 예외 처리와 렌더링을 포함한 MVC 처리가 끝난 뒤 실행된다.
- `afterCompletion`의 예외 인자는 예외 발생 이력이 아니라 MVC가 해결하지 못한 예외를 나타낸다.

## 실험

- 기준 디렉터리: `labs/spring-lab`
- 파일: `src/test/java/mvc/FilterInterceptorAopBoundaryTest.java`
- 클래스: `FilterInterceptorAopBoundaryTest`
- 메서드:
  - `normalRequestShowsAllThreeBoundaries()`
  - `handledControllerExceptionShowsExceptionResolutionBoundary()`
  - `filterExceptionDoesNotReachControllerAdvice()`
- Spring Boot 4.1의 테스트용 `spring-boot-starter-aspectj`를 추가했다.
- 실제 임의 포트 서버에 정상 요청, Controller 예외 요청, Filter 예외 요청을 전송했다.
- 학습자가 세 경로의 핵심 이벤트 순서 assertion을 직접 작성했다.

## 실제 결과

- 정상 요청은 `filter-before → interceptor-preHandle → aop-before → controller → aop-after-returning → aop-finally → interceptor-postHandle → interceptor-afterCompletion:null → filter-after` 순서였다.
- Controller 예외는 `filter-before → interceptor-preHandle → aop-before → controller → aop-after-throwing → aop-finally → advice-sold-out → interceptor-afterCompletion:null → filter-after` 순서였고 409를 반환했다.
- Filter 예외는 `filter-before → filter-throwing`만 기록됐고 500을 반환했다.
- 새 테스트 3개를 함께 실행해 성공했다.
- 전체 Gradle 테스트 39개가 성공했고 실패·오류·건너뜀은 0개였다.

## 실패 분석

- 첫 정상 요청 실행에서는 AOP 이벤트가 누락되어 assertion이 실패했다.
- 원인은 `@Import(FlowController.class)`가 등록한 Bean 이름과 `bean(flowController)` 포인트컷의 불일치였다.
- Controller를 명시적인 `flowController` Bean으로 등록한 뒤 학습자의 기존 assertion을 바꾸지 않고 재실행하여 성공했다.
- 이 실패로 Aspect 등록만으로는 충분하지 않고 포인트컷이 실제 Bean과 일치해야 한다는 사실을 확인했다.

## 처음 예측과 달랐던 부분

- Advice가 AOP의 `after-throwing`보다 먼저 실행된다고 작성했지만, Controller의 직접 호출자인 AOP가 `catch`와 `finally`를 실행하고 예외를 재전파한 뒤 Advice가 실행된다.
- 예외를 다시 던지면 즉시 Advice로 이동한다고 생각해 `aop-finally`의 위치를 Advice 뒤로 두었지만, Java의 `finally`는 현재 메서드 밖으로 예외가 전달되기 전에 실행된다.
- Filter 예외 경로에 `advice-filter-failure`와 `filter-after`를 포함했지만, DispatcherServlet에 진입하지 않았고 예외 뒤의 일반 코드에도 도달하지 않았다.
- AOP를 Controller 전후 경계로만 설명했지만, 실제 적용 범위는 포인트컷과 프록시를 통과한 Spring Bean 메서드 호출이다.

## 내가 다시 설명하기

- Advice가 Controller 예외를 해결하면 DispatcherServlet과 `chain.doFilter()`가 정상 복귀하므로 `filter-after`가 실행된다고 설명했다.
- Filter가 `chain.doFilter()` 전에 예외를 던지면 DispatcherServlet에 진입하지 않아 MVC 확장 지점이 실행되지 않는다고 설명했다.
- 전체 HTTP 요청 로깅은 Filter, 선택된 Controller annotation 기반 권한 검사는 Interceptor, HTTP와 배치가 공유하는 Service 메서드 시간 측정은 AOP에 배치한다고 적용 범위와 컨텍스트를 근거로 선택했다.
- AOP가 Spring Bean의 메서드 호출 경계에 적용된다는 점을 재설명했다.

## 회상 문제

1. 정상 요청에서 Filter·Interceptor·AOP의 진입과 복귀 순서를 설명했다.
2. 처리된 Controller 예외에서 `postHandle`이 실행되지 않고 `afterCompletion`이 `null`을 받는 이유를 설명했다.
3. Filter가 DispatcherServlet 진입 전에 예외를 던지면 MVC의 Advice가 처리하지 못하는 이유를 설명했다.

## 면접 질문

1. Filter·Interceptor·AOP의 실행 위치와 적용 범위를 비교했다.
2. 전체 HTTP 로깅, Controller 권한 검사, Service 실행 시간 측정의 배치 위치를 근거와 함께 선택했다.

## 다음 복습일

2026-08-12
