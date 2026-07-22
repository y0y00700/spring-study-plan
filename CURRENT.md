# Current Learning Context

마지막 갱신일: 2026-07-23

## 현재 단계

기초 진단 진행 중 — Java와 객체 1~3번

## 현재 주제

의존 객체의 생성 책임과 Singleton의 변경 가능한 공유 상태

## 알고 있다고 생각하는 것

- Controller가 HTTP 요청을 받는다.
- Service에서 비즈니스 로직을 처리한다.
- Repository에서 데이터베이스에 접근한다.
- Spring에서 의존성 주입을 사용해 객체를 연결한다.
- 구체 구현체를 사용하는 클래스 내부에서 직접 생성하면 구현체 변경이 그 클래스의 변경으로 전파된다.
- Singleton의 변경 가능한 필드는 여러 요청 스레드가 공유하므로 다른 요청의 값으로 덮어써질 수 있다.

## 아직 설명하기 어려운 것

- 요청이 Controller까지 도착하는 전체 과정
- ApplicationContext와 BeanDefinition의 역할
- Bean이 생성되고 초기화되는 시점
- Spring 프록시와 `@Transactional`의 동작 원리
- JPA, Hibernate, Spring Data JPA의 역할 차이
- 영속성 컨텍스트와 flush 시점
- 생성자 매개변수를 이용해 의존 객체를 외부에서 전달하는 코드 작성
- 인터페이스 타입 사용과 생성 책임 분리의 차이

## 다음 행동

1. `OrderService(PaymentProcessor processor)` 생성자와 `this.processor = processor`를 직접 완성한다.
2. `KakaoPaymentProcessor`에서 `NaverPaymentProcessor`로 변경할 때 어느 코드가 바뀌는지 설명한다.
3. 위 내용을 최소 실행 코드로 검증한 뒤 Java와 객체 진단 4~5번으로 진행한다.

## 다음 세션 시작 요청

```text
AGENTS.md와 CURRENT.md, sessions/2026-07-23-diagnostic-java-dependency.md를 읽고
중단한 생성자 주입 문제부터 이어서 진행해 줘. 내가 코드를 작성하기 전에는 완성 코드를 말하지 마.
```
