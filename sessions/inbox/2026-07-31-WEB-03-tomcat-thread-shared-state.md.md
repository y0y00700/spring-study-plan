---
date: 2026-07-31
roadmap_item: WEB-03
completion_status: completed
recommended_next: WEB-04
processed: true
environment: company_pc_restricted
---

# WEB-03 Tomcat 스레드와 공유 객체

## 세션 범위

- 요청별 Tomcat 실행 스레드
- 여러 요청 스레드가 공유하는 Singleton Controller
- 인스턴스 필드와 메서드 지역변수의 차이
- 지역변수가 공유 변경 가능 객체를 참조하는 경우의 위험

## 선수 항목

- `WEB-02 Servlet과 Servlet Container`: completed
- Servlet Container가 같은 Servlet 인스턴스의 `service()`를 여러 요청에 호출한다는 내용을 회상했다.
- HTTP 메시지가 네트워크를 통과하며 클라이언트와 서버가 같은 Java 객체 참조를 공유하지 않는다는 내용을 회상했다.

## 최소 재현 실험

생성한 테스트:

- 기준 디렉터리: `labs/spring-lab`
- 파일: `src/test/java/tomcat/TomcatThreadSharedStateTest.java`
- 클래스: `TomcatThreadSharedStateTest`
- 메서드: `concurrentRequestsOverwriteSingletonField`

실험 구성:

1. Spring Boot 테스트가 임의 포트에서 내장 Tomcat을 실행한다.
2. 두 클라이언트 스레드가 A와 B HTTP 요청을 동시에 보낸다.
3. 서로 다른 Tomcat 요청 스레드가 같은 Singleton Controller를 호출한다.
4. `CountDownLatch`로 실행 순서를 `A 저장 → B 덮어쓰기 → A 재개`로 고정한다.
5. 요청값, 공유 필드값, Controller 인스턴스 식별값, Tomcat 스레드명을 응답으로 관찰한다.

핵심 assertion:

```java
assertEquals("B", a.storedUser());
assertEquals("B", b.storedUser());
assertEquals(a.instanceId(), b.instanceId());
assertNotEquals(a.threadName(), b.threadName());
```

## 테스트 결과

IntelliJ에서 concurrentRequestsOverwriteSingletonField 실행
정상 종료
A와 B 요청은 서로 다른 Tomcat 스레드에서 실행됨
두 요청은 같은 Singleton Controller 인스턴스를 사용함
A 요청의 원래 요청값은 A지만 공유 필드에서 최종적으로 읽은 값은 B
B 요청도 공유 필드에서 B를 읽음
전체 테스트 스위트는 이 제한 모드 세션에서 실행 여부를 확인하지 않음

집 PC에서 2026-08-02에 `.\gradlew.bat test`로 전체 23개 테스트를 실행했고, 실패·오류·건너뜀 테스트 없이 성공했다.

## 실행 순서 분석

A: 공유 필드 currentUser = "A"
A: aStored.countDown()으로 B 진행 허용
A: bStored.await()에서 대기

B: aStored.await() 통과
B: 공유 필드 currentUser = "B"
B: bStored.countDown()으로 A 진행 허용
B: 공유 필드 읽기 → "B"

A: 대기에서 깨어남
A: 공유 필드 읽기 → "B"
CountDownLatch는 값을 전달하지 않는다. 여러 스레드가 공유하는 동기화 객체이며, countDown()으로 카운터를 0으로 만들면 await()에서 기다리던 스레드가 진행한다.
await(5, TimeUnit.SECONDS)는 최대 5초 동안 신호를 기다리고, 제한 시간 안에 신호를 받으면 true, 받지 못하면 false를 반환한다. 테스트가 무한 대기하지 않도록 둔 안전장치다.

## 학습자가 설명할 수 있게 된 것

Tomcat은 동시 HTTP 요청을 서로 다른 요청 스레드에서 처리할 수 있다.
Spring Singleton Controller는 인스턴스가 하나이며 여러 요청 스레드가 같은 인스턴스를 호출한다.
Singleton Controller의 인스턴스 필드에 요청별 변경 상태를 저장하면 다른 요청이 값을 덮어쓸 수 있다.
메서드 지역변수는 호출별 스택 프레임에 별도로 존재하므로 다른 호출이 같은 지역변수 자체를 덮어쓰지 않는다.
지역변수라는 이유만으로 항상 안전한 것은 아니다.
지역변수가 Singleton 필드에 저장된 동일한 변경 가능 객체를 가리키면 참조 대상의 상태는 여러 스레드에 공유된다.
Singleton 여부는 인스턴스와 인스턴스 필드의 공유 여부에 관한 것이며, 모든 메서드 지역변수가 공유된다는 뜻이 아니다.

## 확인된 오개념과 교정

### Singleton이면 메서드 지역변수도 공유된다

**초기 생각:**
서비스나 Controller가 Singleton이면 메서드 지역변수도 요청 사이에 공유될 수 있다고 생각했다.

**교정:**
각 메서드 호출에는 별도의 지역변수가 만들어진다.
요청 간에 직접 공유되는 것은 Singleton 인스턴스의 필드다.
단, 서로 다른 지역변수가 동일한 변경 가능 객체를 참조하면 그 객체의 상태는 공유된다.

### 요청값과 공유 필드값은 항상 같다

**초기 예측:**
assertEquals("A", a.storedUser());

**교정:**
requestedUser()는 A 요청에서 전달된 원래 값이다.
storedUser()는 응답 직전에 Singleton 공유 필드에서 다시 읽은 값이다.
B가 공유 필드를 덮어쓴 뒤 A가 읽으므로 a.storedUser()는 B다.

## 회상 문제 결과

두 동시 HTTP 요청을 처리한 Tomcat 스레드는 서로 달랐다.
currentUser 인스턴스 필드는 공유되지만 호출별 지역변수는 분리된다.
A가 A를 저장한 뒤 B가 B로 덮어쓰고 A가 다시 읽으면 A도 B를 읽는다.

## 면접 질문 결과

### Singleton Controller를 무상태로 설계해야 하는 이유

여러 요청 스레드가 같은 Singleton Controller 인스턴스를 동시에 호출한다. 요청별 변경 상태가 인스턴스 필드에 있으면 다른 스레드가 그 값을 읽거나 덮어써 요청 간 간섭이 발생할 수 있다.

### 지역변수가 공유 ArrayList를 가리킬 때 안전한지

안전하지 않다. 지역변수 자체는 호출마다 분리되지만, 각 지역변수가 가리키는 변경 가능한 ArrayList가 동일한 객체라면 그 객체의 상태는 여러 스레드가 공유한다.

## 해결되지 않은 질문

없음

## 완료 기준 판단

완료 기준을 충족했다.
실제 두 동시 HTTP 요청으로 공유 상태 간섭을 재현했다.
assertion으로 같은 Singleton 인스턴스와 서로 다른 요청 스레드를 검증했다.
A 요청이 B의 값을 읽는 실행 순서와 실패 원인을 설명했다.
Singleton 필드와 메서드 지역변수의 차이를 설명했다.
지역변수가 공유 변경 가능 객체를 참조하는 반례를 설명했다.

## 다음 진행 제안

WEB-04 Filter, Listener, DispatcherServlet의 위치
recommended_next는 제안이며, 집 PC에서 이 세션과 테스트 결과를 검토한 뒤 CURRENT.md에 확정한다.
