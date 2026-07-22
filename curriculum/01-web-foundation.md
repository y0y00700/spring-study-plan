# 1주차: 웹 애플리케이션 기반

## 핵심 질문

- Spring이 없어도 Java 웹 애플리케이션은 어떻게 요청을 처리하는가?
- Servlet Container는 무엇을 생성하고 관리하는가?
- Tomcat의 스레드와 Spring Singleton Bean은 어떤 관계가 있는가?
- DispatcherServlet은 일반 Servlet과 무엇이 다른가?

## 최소 실험

1. Spring 없이 간단한 Servlet 요청을 처리한다.
2. Filter 전후 로그와 Servlet 실행 순서를 확인한다.
3. 동시에 여러 요청을 보내 스레드 이름과 객체 identity를 관찰한다.

## 완료 증거

- 요청 처리 순서를 자신의 말로 작성한 세션 기록
- 실행 전 예상과 실제 로그 비교
- 회상 문제 3개에 자료 없이 답변
