---
date: 2026-08-06
environment: company_restricted
roadmap_item: MVC-04 Validation과 예외 처리
completion_status: completed
recommended_next: MVC-05 Filter, Interceptor, AOP 경계
processed: false
---

# MVC-04 Validation과 예외 처리

## 선수 항목

- MVC-03 ArgumentResolver와 HttpMessageConverter: completed

## 핵심 개념

- 타입 변환 실패, DTO 검증 실패, 비즈니스 예외의 발생 위치
- DispatcherServlet의 예외 처리 체인과 `@RestControllerAdvice`

## 실험 환경

- 기준 디렉터리: `labs/spring-lab`
- 테스트 파일: `src/test/java/mvc/ValidationExceptionAdviceTest.java`
- Spring Boot 실제 내장 서버와 Java HttpClient 사용
- `ValidationExceptionAdviceTest` 테스트 3개 성공
- 이번 세션에서는 전체 프로젝트 테스트를 별도로 재실행하지 않았다.

## 성공한 테스트

- `typeMismatchIsHandledBeforeController()`
- `validationFailureIsHandledBeforeController()`
- `businessExceptionIsHandledAfterControllerEntry()`

## 검증 결과

| 실패 종류 | DTO 생성 | Controller 진입 | Advice 응답 |
|---|---:|---:|---|
| 경로 변수 타입 불일치 | X | X | 400 `TYPE_MISMATCH` |
| `@Valid` 검증 위반 | O | X | 400 `VALIDATION_FAILED` |
| 재고 없음 비즈니스 예외 | O | O | 409 `SOLD_OUT` |

## 경로 타입 변환 실패

```text
POST /orders/abc
→ HandlerMapping이 /orders/{id} Handler 탐색
→ ArgumentResolver가 "abc"를 long으로 변환
→ MethodArgumentTypeMismatchException
→ Controller 호출 안 됨
→ DispatcherServlet이 예외 처리 체인에 위임
→ @RestControllerAdvice
→ 400 TYPE_MISMATCH
"abc"도 /orders/{id} 경로 패턴에는 일치한다. 실패는 HandlerMapping 단계가 아니라 Controller 인자 타입 변환 단계에서 발생한다.
DTO 검증 실패
정상 JSON 본문
→ HttpMessageConverter가 CreateOrderRequest("", 0) 생성
→ @Valid에 의해 Bean Validation 실행
→ @NotBlank·@Min 위반
→ MethodArgumentNotValidException
→ Controller 호출 안 됨
→ @RestControllerAdvice
→ 400 VALIDATION_FAILED
DTO 생성과 DTO 검증은 별도 단계다. JSON 문법과 타입이 정상이라면 DTO가 먼저 생성되고, 이후 제약조건을 검사한다.
비즈니스 예외
경로 변환 성공
→ DTO 생성·검증 성공
→ Controller 진입
→ invocationCount 1 증가
→ SOLD_OUT 비즈니스 조건 확인
→ SoldOutException
→ 정상 반환문 실행 안 됨
→ @RestControllerAdvice
→ 409 SOLD_OUT
비즈니스 조건 검사는 Bean Validation이 아니라 Controller 또는 서비스의 업무 규칙 검사다.
예외 처리 흐름
요청 처리 중 예외
→ DispatcherServlet
→ HandlerExceptionResolver 체인
→ ExceptionHandlerExceptionResolver
→ 예외 타입과 일치하는 @ExceptionHandler 선택
→ ResponseEntity<ApiError> 반환
→ HTTP 상태와 JSON 오류 본문 생성
@RestControllerAdvice는 Spring Bean으로 등록된다. Controller 메서드 내부에 있는 코드가 아니므로 Controller 진입 전에 발생한 인자 처리 예외와 Controller 내부에서 발생한 비즈니스 예외를 모두 처리할 수 있다.
교정한 예측
401은 잘못된 요청 상태가 아니라 인증이 필요하거나 인증에 실패한 경우의 상태다.
경로 변수 타입 변환과 DTO 검증 실패는 400 Bad Request다.
검증 위반 DTO는 생성 자체가 실패한 것이 아니라 생성 후 검증에서 실패한다.
정상 URL과 DTO만으로 200을 보장할 수 없다. Controller 내부 비즈니스 예외가 발생할 수 있다.
Advice가 없으면 처리되지 않은 SoldOutException은 일반적으로 500으로 이어질 수 있다.
Advice는 Controller에 진입하지 않은 예외도 DispatcherServlet의 예외 처리 흐름에서 처리할 수 있다.
완료 판단
잘못된 타입, 검증 위반, 비즈니스 예외의 응답 경로를 실제 요청으로 비교했다.
세 실패의 Controller 호출 횟수와 상태 코드, 오류 본문에 assertion을 두었다.
DTO 생성과 검증 시점을 구분했다.
Controller 진입 전후의 예외가 Advice로 전달되는 흐름을 설명했다.
세 실패의 발생 위치와 처리 주체를 구분했다.
따라서 MVC-04를 completed로 기록한다.
다음 진행 후보
MVC-05 Filter, Interceptor, AOP 경계
선수 항목: MVC-04 completed
핵심 개념: 세 확장 지점의 실행 위치와 사용 가능한 컨텍스트
최소 실험: Filter·Interceptor·AOP의 전후 이벤트와 예외 전달 범위 비교
완료 기준: 인증·로깅·실행 시간 측정의 배치 위치를 근거와 함께 선택한다.
회상 문제
/orders/abc가 경로 패턴에는 일치하지만 Controller가 호출되지 않는 이유는 무엇인가?
JSON으로 DTO가 생성된 뒤 @Valid 검증이 실패하면 어떤 실행 단계가 중단되는가?
Controller 진입 전 예외도 @RestControllerAdvice가 처리할 수 있는 이유는 무엇인가?
면접 질문
타입 변환 실패, Bean Validation 실패, 비즈니스 예외를 발생 위치와 처리 주체에 따라 비교해 주세요.
전역 예외 처리기가 비즈니스 예외를 409 응답으로 바꾸는 실행 흐름을 설명해 주세요.
misconceptions
잘못된 입력을 모두 401로 판단하면 안 된다. 인증 문제와 요청값 문제를 구분해야 한다.
DTO 생성과 Bean Validation은 동일한 사건이 아니다.
HandlerMapping의 경로 패턴 일치와 Controller 인자의 타입 변환은 서로 다른 단계다.
비즈니스 조건 검사는 @Valid Bean Validation과 다른 책임이다.