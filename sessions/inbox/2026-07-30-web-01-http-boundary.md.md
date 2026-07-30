---
date: 2026-07-30
roadmap_item: WEB-01
completion_status: completed
recommended_next: WEB-02
processed: false
environment: company_pc_limited_mode
---

# WEB-01 HTTP 요청과 응답 경계

## 선수 항목

- CON-10 Container 종합 진단: completed
- Singleton과 Prototype의 생성·초기화·추적·소멸 책임 차이를 회상했다.
- BeanDefinition 등록부터 의존성 해결, 생성, 초기화, Singleton 저장·공개, 조회, 소멸까지의 흐름을 회상했다.

## 핵심 개념

- HTTP 요청과 응답은 메서드·경로·헤더·본문 등의 요소로 구성된 메시지다.
- 서버와 클라이언트의 객체 참조가 네트워크를 직접 통과하지 않는다.
- 데이터는 전송 가능한 바이트 형태로 표현되며, 수신자는 자신의 실행 환경에서 값이나 객체를 새로 구성한다.
- 서버는 HTTP 메서드와 경로의 조합을 사용해 처리 코드를 선택한다.
- 상태 코드와 응답 본문은 HTTP 응답 메시지의 서로 다른 구성요소다.
- Java 클라이언트는 JVM에서 객체를 만들지만, 일반적인 HTTP 클라이언트가 반드시 JVM을 사용하는 것은 아니다.
- 일반화할 때는 서버와 클라이언트를 서로 독립된 실행 환경과 메모리 공간으로 설명한다.

## 실행 전 예측

- GET /boundary
  - 예상 상태: 200
  - 예상 본문: GET:no-body
- POST /boundary, 본문 hello
  - 예상 상태: 200
  - 예상 본문: POST:hello
- POST /boundary, 본문 world
  - 예상 상태: 200
  - 예상 본문: POST:world
- PUT /boundary, 본문 hello
  - 예상 상태: 405
  - 응답 본문은 검증하지 않음

## 실험 환경 변경

`labs/spring-lab/build.gradle`에 테스트용 웹 의존성을 추가했다.

```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-web'
기존 의존성에는 spring-boot-starter와 spring-context만 있어
RestController, GetMapping, PostMapping, RequestBody 등
Spring Web 클래스를 찾을 수 없었다.
최소 재현 실험
새 테스트 파일:
labs/spring-lab/src/test/java/webboundary/HttpRequestResponseBoundaryTest.java
테스트 메서드:
samePathWithDifferentMethodsAndBodies
실제 임의 포트의 웹 서버에 Java HttpClient로 다음 요청을 전송했다.
GET /boundary
POST /boundary, 본문 hello
POST /boundary, 본문 world
PUT /boundary, 본문 hello
Assertion 결과
assertThat(getResponse.statusCode()).isEqualTo(200);
assertThat(getResponse.body()).isEqualTo("GET:no-body");

assertThat(postHelloResponse.statusCode()).isEqualTo(200);
assertThat(postHelloResponse.body()).isEqualTo("POST:hello");

assertThat(postWorldResponse.statusCode()).isEqualTo(200);
assertThat(postWorldResponse.body()).isEqualTo("POST:world");

assertThat(putResponse.statusCode()).isEqualTo(405);
모든 assertion이 성공했다.
관찰 및 실패 분석
같은 경로라도 GET과 POST는 서로 다른 처리 코드에 연결됐다.
같은 POST 요청도 본문 데이터에 따라 응답 본문이 달라졌다.
PUT은 경로가 존재하지만 해당 메서드의 처리 코드가 없어 405가 반환됐다.
최초 import 실패는 HTTP 실행 흐름의 실패가 아니라 Spring Web과 내장 서버가 test classpath에 없었던 실험 환경 문제였다.
완료 기준 확인
네트워크 메시지와 Java 객체를 다음과 같이 구분했다.
클라이언트 실행 환경에서 요청을 표현하는 객체를 만든다.
전송 시 요청 정보와 본문 데이터가 HTTP 메시지의 바이트 표현으로 바뀐다.
서버는 메시지를 수신하고 메서드와 경로를 사용해 처리 코드를 선택한다.
서버는 본문 데이터를 서버 실행 환경의 값이나 객체로 구성한다.
Controller의 반환 객체는 HTTP 응답 메시지의 상태·헤더·본문으로 표현된다.
클라이언트는 응답 데이터를 자신의 실행 환경에서 새로운 값이나 객체로 구성한다.
서버와 클라이언트의 객체는 내용이 같더라도 같은 객체 참조가 아니다.
발견한 오개념
HTTP 본문 바이트와 Java 바이트코드 혼동
이전 생각: Controller 반환값이 바이트코드로 전송된다고 표현했다.
교정: Java 바이트코드는 JVM이 실행하는 .class 명령이다. HTTP 본문은 데이터를 인코딩한 바이트 표현이다.
추가 교정: `.java`를 `.class` 바이트코드로 변환하는 주체는 Java 컴파일러이며, JVM은 생성된 바이트코드를 실행한다.
객체가 다른 이유를 통신 코드 규격 차이로 설명
이전 생각: 서버와 클라이언트 객체가 다른 이유는 통신에 사용하는 코드 규격이 다르기 때문이라고 설명했다.
교정: 핵심 이유는 실행 환경과 메모리 공간이 독립되어 있고, 객체 참조가 아닌 데이터 표현만 네트워크로 전달되기 때문이다.
해결되지 않은 질문
없음
회상 문제
Java 객체가 HTTP를 통해 그대로 전달되지 않는 이유는 무엇인가?
같은 경로인데 POST는 200, PUT은 405가 된 이유는 무엇인가?
바이트코드와 HTTP 본문 바이트의 차이는 무엇인가?
면접 질문
“Controller가 반환한 객체가 클라이언트로 전달된다”라는 설명에서 생략된 변환 경계를 설명하라.
HTTP 요청의 메서드·경로·헤더·본문의 역할과 처리 코드 선택 기준을 설명하라.
다음 진행 제안
WEB-02 Servlet과 Servlet Container
핵심 개념: Servlet 생명주기, Container의 생성·호출 책임
최소 실험: init, service, destroy 호출 횟수와 순서 기록
완료 기준: Servlet을 누가 만들고 언제 호출하는지 설명한다.
