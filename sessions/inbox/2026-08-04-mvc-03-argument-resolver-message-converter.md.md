---
date: 2026-08-04
environment: company_restricted
roadmap_item: MVC-03 ArgumentResolver와 HttpMessageConverter
completion_status: completed
recommended_next: MVC-04 Validation과 예외 처리
processed: true
---

# MVC-03 ArgumentResolver와 HttpMessageConverter

## 선수 항목

- MVC-02 HandlerMapping과 HandlerAdapter: completed

## 핵심 개념

- ArgumentResolver의 Controller 매개변수 해석과 인자 준비
- HttpMessageConverter의 요청 본문 읽기와 응답 본문 쓰기

## 실험 환경

- 기준 디렉터리: `labs/spring-lab`
- 테스트 파일: `src/test/java/mvc/ArgumentResolverMessageConverterTest.java`
- 실제 내장 서버의 임의 포트에 Java HttpClient로 요청
- 실행 결과: 테스트 4개 성공

## 성공한 테스트

- `resolvesThreeArgumentsAndWritesJsonResponse()`
- `invalidPathVariableStopsBeforeController()`
- `missingRequestParameterStopsBeforeController()`
- `malformedJsonStopsBeforeController()`

## 검증한 실행 흐름

```text
HTTP 요청
→ Controller 매개변수마다 ArgumentResolver 선택
→ @PathVariable과 @RequestParam 값 조회 및 타입 변환
→ @RequestBody용 Resolver가 HttpMessageConverter에 위임
→ HttpMessageConverter.read()가 JSON 본문을 DTO로 변환
→ 모든 인자가 준비된 뒤 Controller 호출
→ Controller가 Java 객체 반환
→ 반환값 처리기가 HttpMessageConverter에 위임
→ HttpMessageConverter.write()가 객체를 JSON 응답 본문으로 변환
실험 결과
정상 요청에서는 경로 변수 10, 요청 파라미터 true, JSON 본문의 book이 각각 Controller 인자로 준비됐다.
Controller가 반환한 OrderResponse 객체는 JSON 응답 본문으로 변환됐다.
잘못된 경로 변수 abc는 Long 타입 변환에 실패하여 400이 반환됐고 Controller는 호출되지 않았다.
필수 urgent 요청 파라미터가 없으면 400이 반환됐고 Controller는 호출되지 않았다.
깨진 JSON은 HttpMessageConverter가 DTO로 읽는 과정에서 실패하여 400이 반환됐고 Controller는 호출되지 않았다.
인자 하나라도 준비되지 않으면 Controller 메서드는 실행되지 않는다.
역할 구분
ArgumentResolver는 Controller의 각 매개변수를 지원하는지 판단하고 실제 인자 값을 준비한다.
@RequestBody용 ArgumentResolver는 HTTP 본문의 변환을 HttpMessageConverter에 위임한다.
HttpMessageConverter는 HTTP 전체가 아니라 요청·응답 본문과 Java 객체 사이를 변환한다.
@PathVariable과 @RequestParam의 문자열 기반 변환에는 HttpMessageConverter가 사용되지 않는다.
@RestController 또는 @ResponseBody가 응답 본문 처리 경로를 선택하게 한다.
일반 @Controller에서 @ResponseBody가 없다면 반환 객체가 자동으로 JSON 응답 본문이 되지 않는다.
교정한 예측
@RequestBody CreateOrderRequest에 전달되는 값은 "book" 문자열이 아니라 CreateOrderRequest 객체다.
Handler는 찾았지만 필수 요청 파라미터가 누락된 경우 404가 아니라 인자 준비 실패에 따른 400이다.
Java boolean true는 JSON 문자열 "true"가 아니라 JSON boolean true로 표현된다.
MessageConverter가 등록되어 있는 것과 실제 응답 변환 경로에서 선택되는 것은 구분해야 한다.
완료 판단
요청 파라미터·경로 변수·JSON 본문의 서로 다른 준비 경로를 비교했다.
성공과 세 가지 실패 경로에 assertion을 두고 테스트했다.
JSON이 DTO가 되는 시점과 반환 객체가 JSON이 되는 시점을 설명했다.
ArgumentResolver와 HttpMessageConverter의 책임을 구분했다.
따라서 MVC-03을 completed로 기록한다.
회상 문제
인자 하나의 준비가 실패하면 Controller 메서드가 실행되지 않는 이유는 무엇인가?
@RequestParam boolean과 @RequestBody DTO는 어떤 변환 경로를 사용하는가?
HttpMessageConverter의 read()와 write()는 각각 언제 호출되는가?
면접 질문
Spring MVC에서 JSON 요청이 Controller DTO로 전달되는 과정을 설명해 주세요.
경로 변수 타입 오류와 깨진 JSON이 모두 400을 반환하더라도 실패 원인이 다른 이유를 설명해 주세요.
misconceptions
HttpMessageConverter가 HTTP 전체 문자열을 변환하는 것은 아니다. 요청·응답 본문과 Java 객체 사이를 변환한다.
필수 요청 파라미터 누락은 Handler 탐색 실패가 아니라 Controller 인자 준비 실패다.
unresolved
없음
