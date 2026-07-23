# Current Learning Context

마지막 갱신일: 2026-07-24

## 현재 단계

기초 진단 진행 중 — Java와 객체 1~5번을 진단했고, 최소 재현 실험으로 이해를 검증하는 단계

## 현재 주제

생성자 주입의 생성 책임 분리와 annotation·Reflection의 역할

## 설명할 수 있게 된 것

- 인터페이스 타입으로 선언하는 것만으로는 생성 책임이 분리되지 않는다.
- 사용하는 객체 내부에서 구체 구현체를 `new`하면 구현체 변경이 사용하는 객체의 변경으로 전파된다.
- 생성자 주입에서는 외부 조립 코드가 구체 구현체를 만들고, 사용하는 객체는 생성자 매개변수로 전달받는다.
- Singleton이 공유하는 변경 가능한 객체는 필드뿐 아니라 메서드 인자로 전달된 객체도 호출 간 간섭을 만들 수 있다.
- annotation은 메타데이터이며 스스로 코드를 실행하지 않는다.
- 컴파일러, annotation processor, Spring 같은 처리 주체가 annotation을 읽고 해석해야 동작이 발생한다.
- Reflection은 실행 중 클래스 구조와 annotation 정보를 조사하는 기능이며, 조사만으로 대상 메서드가 실행되지는 않는다.

## 아직 실험으로 검증하지 못한 것

- `OrderService`가 두 `PaymentProcessor` 구현체를 생성자로 전달받아 각각 다른 결과를 내는지
- 잘못된 기대값을 넣었을 때 생성자 주입 검증문이 실제로 실패하는지
- `isAnnotationPresent()`가 annotation 존재 여부만 반환하고 대상 메서드는 실행하지 않는지
- Spring이 Reflection으로 얻은 생성자·매개변수 정보를 Bean 생성 및 의존성 연결에 사용하는 구체적인 흐름
- 요청이 Controller까지 도착하는 전체 과정
- ApplicationContext와 BeanDefinition의 역할
- Bean이 생성되고 초기화되는 시점
- Spring 프록시와 `@Transactional`의 동작 원리
- JPA, Hibernate, Spring Data JPA의 역할 차이
- 영속성 컨텍스트와 flush 시점

## 현재 실습 환경

- `labs/spring-lab`: Java 17, Spring Boot 4.1.0, Gradle Wrapper 9.5.1
- 2026-07-24 전체 테스트 성공: 2개 실행, 실패 0개
- 현재 테스트는 컨텍스트 로딩과 변경 가능한 공유 리스트 간섭만 검증한다.

## 다음 행동

1. 학습자가 생성자 주입과 구현체 교체를 검증하는 최소 테스트를 먼저 작성한다.
2. 실행 전에 각 구현체의 예상 반환값과 근거를 기록하고, 통과와 의도적 실패를 모두 확인한다.
3. annotation 존재 확인과 메서드 실행을 분리한 최소 Reflection 테스트를 작성한다.
4. 두 실험을 자신의 말로 다시 설명한 뒤 웹 진단 1번으로 진행한다.

## 다음 세션 시작 요청

```text
AGENTS.md와 CURRENT.md,
sessions/inbox/2026-07-23-02-constructor-injection-annotation-reflection.md.md를 읽고
생성자 주입 최소 테스트의 예상 결과부터 질문해 줘.
내가 코드를 작성하기 전에는 완성 코드를 말하지 마.
```
