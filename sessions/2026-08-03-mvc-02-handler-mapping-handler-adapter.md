# 2026-08-03: MVC-02 HandlerMapping과 HandlerAdapter

## 진행 상태

- roadmap_item: `MVC-02 HandlerMapping과 HandlerAdapter`
- completion_status: `needs_review`
- recommended_next: `MVC-02 Handler와 HandlerAdapter 역할 구분 재설명`

## 학습 전 내 생각

- 정상 요청의 순서를 `DispatcherServlet → HandlerMapping → HandlerAdapter → Controller`로 예측했다.
- HandlerMapping이 Handler를 찾지 못하면 DispatcherServlet과 HandlerMapping까지만 실행된다고 예측했다.
- HandlerMapping과 HandlerAdapter를 분리하면 DispatcherServlet의 변경을 줄일 수 있다고 답했지만 어떤 변경이 어느 구성요소로 이동하는지는 설명하지 못했다.

## 튜터의 질문

1. HandlerMapping은 요청을 보고 무엇을 반환하는가?
2. DispatcherServlet은 Handler를 호출할 HandlerAdapter를 어떻게 선택하는가?
3. Adapter의 `supports()` 검사와 `handle()` 실행은 어떻게 다른가?
4. Handler 탐색과 호출 방식을 분리하면 새로운 Handler 호출 방식이 추가될 때 어떤 변경을 줄일 수 있는가?

## 실행 결과 예측

- `/method-handler`에서는 `handler-mapping → method-style-adapter → method-style-handler`를 예측했다.
- `/direct-handler`에서는 처음에 `handler-mapping → method-style-adapter → direct-style-adapter`를 예측해, `supports()` 검사와 실제 `handle()` 실행을 섞고 최종 Handler 호출을 빠뜨렸다.
- 설명을 다시 추적한 뒤 `/direct-handler`를 `handler-mapping → direct-style-adapter → direct-style-handler`로 수정했다.
- `/missing`은 `handler-mapping`만 기록되고 `404 Not Found`를 반환한다고 수정했다.

## 예상의 근거

- HandlerMapping의 `handlersByPath`가 요청 URI로 `MethodStyleHandler`, `DirectStyleHandler`, `null` 중 하나를 반환한다.
- DispatcherServlet은 반환된 Handler를 각 HandlerAdapter의 `supports(handler)`에 전달하고 지원하는 Adapter 하나를 선택한다.
- 이벤트 기록은 Adapter의 `supports()`가 아니라 선택된 Adapter의 `handle()`과 실제 Handler 메서드에 있으므로 후보 검사와 실제 호출을 구분해야 한다.

## 실험

- 기준 디렉터리: `labs/spring-lab`
- 파일: `src/test/java/mvc/HandlerMappingAdapterSeparationTest.java`
- 클래스: `HandlerMappingAdapterSeparationTest`
- 메서드:
  - `usesMethodStyleAdapterForMethodStyleHandler()`
  - `usesDirectStyleAdapterForDirectStyleHandler()`
  - `doesNotInvokeAdapterWhenNoHandlerIsMapped()`
- 하나의 학습용 HandlerMapping에 두 경로와 서로 다른 호출 모양의 Handler 두 개를 등록했다.
- 각 Handler 타입을 지원하는 HandlerAdapter 두 개를 등록하고 선택된 Adapter와 Handler의 실행 이벤트를 검증했다.
- 학습자가 세 경로의 이벤트 순서와 매핑 실패 상태 코드 assertion을 작성했다.

## 실제 결과

- `/method-handler`: `handler-mapping → method-style-adapter → method-style-handler`, 본문 `method-ok`.
- `/direct-handler`: `handler-mapping → direct-style-adapter → direct-style-handler`, 본문 `direct-ok`.
- `/missing`: `handler-mapping`만 기록되고 상태 코드 `404`.
- `HandlerMappingAdapterSeparationTest` 단독 실행 성공.
- 전체 Gradle 테스트 29개 성공, 실패·오류·건너뜀 0개.

## 예상과 달랐던 부분

- 기존 `DispatcherServletDelegationTest`는 URI에 따라 여러 Handler 중 하나를 선택한 실험이 아니라 `/orders/1`이면 유일한 Handler를 반환하고 아니면 `null`을 반환한 실험이었다. 이를 여러 Handler 선택까지 일반화할 수 없었다.
- `/direct-handler`에서 Method Adapter의 `supports()`가 검사될 수 있다는 사실과 Method Adapter의 `handle()`이 실행되는 것을 혼동했다.
- `EVENTS.add("direct-style-adapter")`가 있는 Direct Adapter의 `handle()`은 실제로 실행되며, 실행되지 않는 것은 지원하지 않는 Method Adapter의 `handle()`이다.
- `401 Unauthorized`와 Handler를 찾지 못한 `404 Not Found`를 처음에 구분하지 못했다.

## 내가 다시 설명하기

- 정상 요청 순서와 매핑 실패 시 중단 지점은 설명했다.
- Handler와 HandlerAdapter의 역할을 여러 차례 바꾸어 답했고, 세션 종료 시점에도 독립적인 재설명은 확인되지 않았다.
- 다음 세션에서 Handler를 `실제 호출 대상`, HandlerAdapter를 `대상의 타입을 검사하고 호출 방법을 적용하는 객체`로 실제 테스트 클래스에 대입해 다시 설명해야 한다.
- 완료 기준인 “두 구성요소를 분리한 이유를 실행 순서로 설명한다”가 아직 충족되지 않아 `MVC-02`를 `needs_review`로 유지한다.

## 남은 질문

- 새로 추가된 구현 질문은 없다.
- Handler와 HandlerAdapter의 역할 구분 및 `supports()`와 `handle()`의 실행 차이는 다음 세션에서 재검증한다.

## 회상 문제

1. HandlerMapping이 반환하는 객체와 HandlerAdapter가 맡는 역할을 각각 설명해보세요.
2. `/direct-handler`에서 Method Adapter의 `supports()`가 false이면 어떤 메서드는 실행되지 않으며, 어떤 Adapter가 선택되나요?
3. HandlerMapping이 `null`을 반환할 때 Adapter와 Handler 호출 및 HTTP 상태는 어떻게 되나요?

## 면접 질문

1. Spring MVC가 HandlerMapping과 HandlerAdapter를 분리한 이유를 새로운 Handler 호출 방식 추가 상황으로 설명해보세요.
2. DispatcherServlet이 Handler를 찾고 호출하기까지의 과정을 `HandlerExecutionChain`, `supports()`, `handle()`을 포함해 설명해보세요.

## 다음 복습일

2026-08-06
