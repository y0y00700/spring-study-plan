# 2026-08-02: MVC-01 DispatcherServlet 요청 처리

## 학습 전 내 생각

- DispatcherServlet이 Controller를 직접 실행하기보다 Spring의 다른 구성요소에 위임할 것으로 예측했다.
- 그 이유를 “Spring이 관리하는 Bean 중 요청을 처리할 Bean을 찾기 때문”이라고 설명했지만, 구체적인 탐색·전달·호출 주체는 구분하지 못했다.
- `HandlerMapping`과 `HandlerAdapter` 설정 코드가 Bean 생성 시점과 요청 처리 시점을 섞어 표현해 연결 관계를 해석하기 어려웠다.

## 튜터의 질문

1. DispatcherServlet은 Controller를 직접 찾아 실행하는가, 다른 구성요소에 위임하는가?
2. HandlerMapping과 HandlerAdapter 중 어느 것이 먼저 실행되어야 하는가?
3. HandlerMapping이 Handler를 반환하지 않으면 이후 호출은 어떻게 되는가?
4. DispatcherServlet이 비즈니스 로직을 직접 실행하지 않는 이유는 무엇인가?

## 실행 결과 예측

- 처음에는 `dispatcher-servlet → handler-adapter → handler-mapping → controller`를 예측했다.
- Adapter가 호출할 Handler는 Mapping이 먼저 찾아야 한다는 인과관계를 확인한 뒤 `dispatcher-servlet → handler-mapping → handler-adapter → controller`로 수정했다.
- 매핑되지 않은 `/missing` 요청에서는 `dispatcher-servlet → handler-mapping`까지만 실행되고 Adapter와 Controller는 호출되지 않을 것으로 예측했다.

## 예상의 근거

- HandlerMapping이 요청 URI에 맞는 Handler를 반환해야 HandlerAdapter가 호출 대상을 전달받을 수 있다.
- HandlerMapping이 `null`을 반환하면 DispatcherServlet에 Adapter로 넘길 Handler가 존재하지 않는다.

## 실험

- 기준 디렉터리: `labs/spring-lab`
- 파일: `src/test/java/mvc/DispatcherServletDelegationTest.java`
- 클래스: `DispatcherServletDelegationTest`
- 메서드: `dispatcherDelegatesHandlerLookupAndInvocation()`
- 실제 DispatcherServlet에 학습용 HandlerMapping, HandlerAdapter, TestController Bean을 연결했다.
- 학습자가 DispatcherServlet 진입부터 Controller 호출까지 이벤트 순서 assertion을 직접 작성했다.

## 실제 결과

- 응답 상태는 `200`, 본문은 `order-1`이었다.
- 이벤트는 `dispatcher-servlet → handler-mapping → handler-adapter → controller` 순서로 기록됐다.
- 전체 Gradle 테스트 26개 성공, 실패·오류·건너뜀 0개.

## 예상과 달랐던 부분

- HandlerAdapter가 HandlerMapping보다 먼저 실행될 것으로 처음 예측했지만, Adapter는 Mapping이 찾은 Handler를 입력으로 받아야 하므로 Mapping이 먼저 실행됐다.
- `request -> { ... }` 람다가 요청 시 호출되는 HandlerMapping 구현 객체라는 점과, 람다가 Bean 생성 시 주입받은 Controller 참조를 보관한다는 점을 처음에는 연결하지 못했다.

## 내가 다시 설명하기

- Spring은 `testController()`가 반환한 객체를 Bean으로 관리하고, 같은 객체 참조를 `studyHandlerMapping(TestController controller)`의 매개변수로 주입한다.
- HandlerMapping은 요청에 맞는 Controller를 Handler로 반환한다.
- DispatcherServlet은 Mapping이 찾은 Handler를 호출 가능한 HandlerAdapter에 전달한다.
- HandlerAdapter 내부의 `handle()`이 실제 Controller 메서드를 호출한다.
- 새 Controller가 추가되어도 DispatcherServlet은 탐색과 호출을 협력 객체에 위임하므로 공통 요청 흐름 조정 역할을 유지할 수 있다.

## 남은 질문

- 새로 추가된 질문 없음.

## 회상 문제

1. HandlerMapping과 HandlerAdapter 중 HandlerMapping이 먼저 실행되어야 하는 이유는 무엇인가?
2. HandlerMapping이 요청에 맞는 Handler를 반환하지 않으면 어떤 단계까지 실행되는가?
3. Bean 생성 시점의 Controller 주입과 요청 시점의 Controller 호출을 구분해 설명해보세요.

## 면접 질문

1. DispatcherServlet의 Front Controller 역할과 직접 비즈니스 로직을 실행하지 않는 이유를 설명해보세요.
2. DispatcherServlet부터 Controller까지 요청이 위임되는 흐름을 HandlerMapping과 HandlerAdapter를 포함해 설명해보세요.

## 다음 복습일

2026-08-05
