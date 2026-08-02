# 2026-08-02: WEB-04 Filter, Listener, DispatcherServlet의 위치

## 학습 전 내 생각

- Listener, Filter, DispatcherServlet, Controller의 전체 실행 순서를 예측하기 어려웠다.
- Filter가 `chain.doFilter()`를 호출하지 않았을 때 Controller가 실행되는지 알지 못했다.
- `chain.doFilter()`를 전체 Filter 호출 사슬의 시작점으로 생각했다.

## 튜터의 질문

1. Listener·Filter 전처리·Controller·Filter 후처리·Listener 종료 이벤트의 순서는 어떻게 되는가?
2. Filter가 `chain.doFilter()`를 호출하지 않으면 Controller는 실행되는가?
3. Filter가 차단해도 Listener의 `requestDestroyed()`는 실행되는가?
4. Listener와 Filter의 책임은 어떻게 다른가?

## 실행 결과 예측

- 정상 요청은 `200`과 `request-initialized → filter-before → controller → filter-after → request-destroyed`를 예측했다.
- 차단 요청은 `401`을 예측했다.
- 차단 Filter에서 `return`한 뒤에도 같은 Filter의 `filter-after`가 실행될 것으로 예측했다.

## 예상의 근거

- Filter 전처리 뒤 `chain.doFilter()`를 통해 Spring MVC 영역으로 요청이 전달된다고 판단했다.
- `chain.doFilter()` 밖에 전처리와 후처리 코드가 있으므로 Controller가 그 사이에서 실행된다고 판단했다.

## 실험

- 기준 디렉터리: `labs/spring-lab`
- 파일: `src/test/java/webfoundation/FilterListenerDispatcherServletOrderTest.java`
- 클래스: `FilterListenerDispatcherServletOrderTest`
- 메서드: `requestPassesThroughFilterToController()`, `filterCanStopRequestBeforeController()`
- Listener, Filter, Controller가 공유 이벤트 목록에 관찰값을 기록하게 했다.
- 학습자가 상태 코드와 이벤트 순서 assertion을 테스트 파일에 직접 작성했다.

## 실제 결과

- 정상 요청: `200`, `request-initialized → filter-before → controller → filter-after → request-destroyed`.
- 차단 요청: `401`, `request-initialized → filter-before → filter-blocked → request-destroyed`.
- 정상 요청 assertion에서 `controller`를 누락해 한 번 실패한 뒤 실행 흐름에 맞게 수정했다.
- 전체 Gradle 테스트 25개 성공, 실패·오류·건너뜀 0개.

## 예상과 달랐던 부분

- 같은 Filter에서 `return`하면 그 뒤의 `filter-after`에는 도달하지 않았다.
- Filter가 Controller를 차단해도 Tomcat은 요청 처리가 종료되면 `requestDestroyed()`를 호출했다.
- 정상 요청의 응답은 항상 Filter가 생성하는 것이 아니다.

## 내가 다시 설명하기

- Tomcat은 요청 처리 시작과 종료에 Listener의 콜백을 호출한다.
- Listener는 요청 생명주기를 관찰하고, Filter는 호출 사슬에 참여해 요청 진행을 통제한다.
- `chain.doFilter()`는 남은 Filter 또는 DispatcherServlet을 호출하고, Controller 처리가 끝나면 Filter로 복귀한다.
- Filter가 `chain.doFilter()` 없이 401로 반환하면 DispatcherServlet과 Controller는 실행되지 않지만 Tomcat은 종료 콜백을 호출한다.

## 남은 질문

- 새로 추가된 질문 없음.

## 회상 문제

1. 정상 요청에서 Listener·Filter·DispatcherServlet·Controller의 실행 순서는 어떻게 되는가?
2. `chain.doFilter()` 전후 코드가 Controller 전후에 실행되는 이유는 무엇인가?
3. Filter가 요청을 차단하면 Controller, 같은 Filter의 후처리, `requestDestroyed()`는 각각 어떻게 되는가?

## 면접 질문

1. 인증되지 않은 요청을 Controller 도달 전에 차단하려면 Listener와 Filter 중 무엇을 선택하고 왜 그렇게 하겠는가?
2. ServletRequestListener와 Filter의 차이를 관찰, 호출 사슬, 요청 진행 통제 관점에서 설명해보세요.

## 다음 복습일

2026-08-05
