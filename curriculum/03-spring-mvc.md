# 3주차: Spring MVC

## 핵심 질문

- DispatcherServlet은 요청을 어떤 Controller 메서드에 전달할지 어떻게 찾는가?
- HandlerMapping과 HandlerAdapter를 분리한 이유는 무엇인가?
- JSON은 언제 Java 객체로 변환되는가?
- Filter, Interceptor, AOP 중 인증·로깅·실행시간 측정은 어디에 두어야 하는가?

## 최소 실험

1. 요청 파라미터, path variable, JSON body의 변환 과정을 비교한다.
2. 사용자 정의 ArgumentResolver를 작성한다.
3. 예외를 발생시키고 `@ControllerAdvice`의 처리 흐름을 확인한다.

## 완료 증거

- HTTP 요청부터 응답 직렬화까지의 실행 순서
- 변환 실패와 validation 실패의 차이 설명
- 웹 계층 슬라이스 테스트
