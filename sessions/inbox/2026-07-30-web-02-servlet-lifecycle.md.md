---
date: 2026-07-30
roadmap_item: WEB-02
completion_status: completed
recommended_next: WEB-03
processed: true
environment: company_pc_limited_mode
---

# WEB-02 Servlet과 Servlet Container

## 진행 배경

- WEB-01은 이전 회사 PC 세션에서 실험과 재설명까지 완료했다.
- CURRENT.md는 아직 집 PC에서 갱신되지 않았다.
- 사용자가 명시적으로 계속 진행을 요청하여 순서대로 WEB-02를 진행했다.
- WEB-03은 제안일 뿐이며 집 PC에서 inbox 처리 후 확정한다.

## 핵심 개념

- Servlet Container는 Servlet 클래스를 로드하고 인스턴스를 생성한다.
- `loadOnStartup=1`이면 서버 시작 과정에서 Servlet을 생성하고 초기화한다.
- Servlet Container는 생성 후 `init()`을 한 번 호출한다.
- 요청마다 같은 Servlet 인스턴스의 `service(request, response)`를 호출한다.
- 서버 종료 시 Servlet Container가 `destroy()`를 한 번 호출한다.
- `destroy()`는 자원 정리 기회를 제공하는 생명주기 콜백이며 객체를 직접 메모리에서 삭제하는 연산은 아니다.
- 실제 메모리 회수는 이후 JVM GC의 책임이다.
- Spring ApplicationContext와 Servlet Container는 통합되어 동작할 수 있지만 관리 책임은 다르다.
- 이번 실험에서 Spring은 `ServletContextInitializer` Bean을 관리했다.
- `ServletContextInitializer`는 Servlet 객체를 생성하지 않고 Servlet 클래스를 ServletContext에 등록했다.
- 실제 `LifecycleServlet` 객체는 Servlet Container인 Tomcat이 생성하고 관리했다.

## 최초 예측

처음에는 다음과 같이 예측했다.

- constructor: 요청마다 호출
- init: 요청마다 호출
- service: 요청마다 호출
- destroy: 요청마다 호출
- 두 요청은 같은 Servlet 객체가 처리
- 생명주기 호출 주체를 Spring 처리기로 표현

같은 객체가 여러 요청을 처리한다는 예측과 요청마다 생성자가 호출된다는 예측이 서로 모순됨을 확인했다.

## 교정된 예측

`loadOnStartup=1`, 요청 2회, 서버 종료 조건:

```text
서버 시작:
constructor → init

첫 번째 요청:
service

두 번째 요청:
service

서버 종료:
destroy

예상 호출 횟수:
constructor: 1회
init: 1회
service: 2회
destroy: 1회
```

## 실험 파일

- 새 테스트 파일: `labs/spring-lab/src/test/java/webboundary/ServletLifecycleContainerTest.java`
- 테스트 메서드: `containerCreatesAndCallsServletLifecycle`
- 프로덕션 코드는 수정하지 않았다.
- WEB-01에서 추가한 `testImplementation 'org.springframework.boot:spring-boot-starter-web'` 테스트 의존성을 그대로 사용했다.

## 실험 구성

Spring이 관리하는 ServletContextInitializer Bean에서 다음 정보를 등록했다.

```java
servletContext.addServlet(
        "lifecycleServlet",
        LifecycleServlet.class
);
```

객체 인스턴스가 아니라 LifecycleServlet.class를 등록했으므로 실제 Servlet 객체는 Tomcat이 생성했다.

`LifecycleServlet`은 `constructor`, `init`, `service`, `destroy` 이벤트를 기록했다.
각 `service()` 응답에는 `System.identityHashCode(this)`를 기록하여 두 요청이 같은 Servlet 인스턴스에서 처리됐는지 확인했다.

## Assertion

서버 시작 직후:

```java
assertThat(EVENTS).containsExactly(
        "constructor", "init"
);
```

두 요청의 Servlet 인스턴스 식별값:

```java
assertThat(secondResponse.body()).isEqualTo(
        firstResponse.body()
);
```

요청 2회 후:

```java
assertThat(EVENTS).containsExactly(
        "constructor", "init", "service", "service"
);
```

서버 종료 후:

```java
assertThat(EVENTS).containsExactly(
        "constructor", "init", "service", "service", "destroy"
);
```

모든 테스트가 성공했다.

## AssertJ 학습

- `@Test`는 JUnit이 실행할 테스트 메서드를 지정한다.
- `assertThat`은 AssertJ의 검증 시작 메서드다.
- `assertThat(actual).isEqualTo(expected)` 형태로 실제값과 예상값을 비교한다.
- `containsExactly`는 컬렉션의 개수, 내용, 순서를 모두 검증한다.
- 검증 실패 시 `AssertionError`가 발생하고 JUnit이 테스트를 실패로 처리한다.

## 완료 기준 재설명

- Tomcat은 서버 시작 시 Servlet을 생성하고 `init()`을 한 번 호출한다.
- 요청이 들어올 때마다 같은 Servlet 인스턴스의 `service()`를 호출한다.
- 요청이 100번이면 `service()`도 100번 호출된다.
- 서버 종료 시 Tomcat은 `destroy()`를 한 번 호출한다.
- Spring ApplicationContext는 이번 실험에서 `ServletContextInitializer` Bean을 관리했다.
- Initializer는 Servlet 등록 정보를 제공했고, 실제 Servlet 객체의 생성과 생명주기 호출은 Tomcat이 담당했다.

## 발견한 오개념

### 같은 인스턴스와 요청별 생성의 모순

- 이전 생각: 같은 Servlet 객체가 요청을 처리하지만 constructor와 init도 요청마다 호출된다고 예측했다.
- 교정: 하나의 Servlet 인스턴스가 여러 요청을 처리하므로 constructor와 init은 한 번이고 service가 요청마다 호출된다.

### Servlet 생명주기 호출 주체

- 이전 생각: Spring 처리기가 init과 service를 호출한다고 표현했다.
- 교정: 직접적인 호출 주체는 Servlet Container인 Tomcat이다.

### ServletContextInitializer의 책임

- 이전 생각: ServletContextInitializer가 Servlet 객체를 생성한다고 판단했다.
- 교정: Initializer는 Servlet 클래스를 등록하며, 실제 객체는 Servlet Container가 생성한다.

### service 반환 표현

- 이전 생각: 요청마다 서비스를 반환한다고 표현했다.
- 교정: Servlet Container가 요청마다 같은 Servlet 인스턴스의 `service(request, response)`를 호출한다.

### destroy와 객체 삭제

- 이전 생각: destroy가 Servlet 객체를 직접 소멸시킨다고 표현했다.
- 교정: destroy는 서비스 종료와 자원 정리를 위한 콜백이며 실제 메모리 회수는 GC가 담당한다.

## 해결되지 않은 질문

없음

## 회상 문제

1. `loadOnStartup=1`인 Servlet에 요청이 3번 들어오면 각 생명주기 메서드는 몇 번 호출되는가?
2. `ServletContextInitializer`와 Servlet Container의 책임은 어떻게 다른가?
3. `destroy()` 호출과 JVM 객체 메모리 회수는 왜 같은 사건이 아닌가?

## 면접 질문

1. Servlet의 전체 생명주기와 각 단계의 호출 주체를 설명하라.
2. Spring Boot 내장 Tomcat 환경에서 Spring ApplicationContext와 Servlet Container의 책임 경계를 설명하라.

## 다음 진행 제안

- WEB-03 Tomcat 스레드와 공유 객체
- 핵심 개념: 요청별 스레드, 여러 요청이 공유하는 인스턴스
- 최소 실험: 두 동시 요청이 같은 Servlet 또는 Singleton 상태에 접근하는 상황 재현
- 완료 기준: 요청 스레드와 Singleton Bean의 관계를 실패 가능성과 함께 설명한다.

다음 권장 항목은 `WEB-03 Tomcat 스레드와 공유 객체`입니다.

## 집 PC 검증 결과

- 검증일: 2026-07-30
- 실제 파일: `labs/spring-lab/src/test/java/webboundary/ServletLifecycleContainerTest.java`
- 관련 테스트를 포함한 전체 테스트가 성공했다.
- 전체 테스트 결과: 22개 실행, 실패·오류 0개
- 최소 실험, assertion, 실패 분석, 학습자의 재설명과 완료 기준을 확인해 `WEB-02`를 완료로 확정했다.
- `ROADMAP_DETAIL.md`의 선수 관계와 순서를 확인해 다음 진행 항목을 `WEB-03 Tomcat 스레드와 공유 객체`로 확정했다.
