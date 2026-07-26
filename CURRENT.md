# Current Learning Context

마지막 갱신일: 2026-07-26

## 현재 단계

Spring 컨테이너 기초 진행 중 — Bean 정의와 객체 생성을 구분하고, `ApplicationContext`와 내부 `BeanFactory`의 역할을 최소 재현 실험으로 검증한 단계

## 현재 주제

Bean 정의 기반 의존성 후보 탐색과 `ApplicationContext`·`BeanFactory`의 역할

## 설명할 수 있게 된 것

- 인터페이스 타입으로 선언하는 것만으로는 생성 책임이 분리되지 않는다.
- 사용하는 객체 내부에서 구체 구현체를 `new`하면 구현체 변경이 사용하는 객체의 변경으로 전파된다.
- 생성자 주입에서는 외부 조립 코드가 구체 구현체를 만들고, 사용하는 객체는 생성자 매개변수로 전달받는다.
- 생성자에 객체 참조를 전달한 뒤 원래 지역변수를 다른 객체로 재대입해도, 생성된 객체의 필드가 가리키는 참조는 바뀌지 않는다.
- 같은 타입의 Bean 후보가 둘인데 선택 규칙이 없으면 컨테이너 생성 중 `NoUniqueBeanDefinitionException`을 원인으로 의존성 연결이 실패한다.
- `@Primary`는 여러 후보 중 기본 우선 후보를 정하고, 주입 지점의 `@Qualifier`는 특정 후보를 명시적으로 선택하므로 `@Primary`보다 우선한다.
- Spring의 설정 처리기는 `@Bean` 메서드의 메서드명·반환 타입·선택 관련 메타데이터를 바탕으로 Bean 정의를 등록한다.
- 주입 지점의 매개변수 타입은 검색 조건이고, 등록된 Bean 정의의 타입 정보는 후보를 판별하는 정보다.
- `@Lazy` Bean도 정의와 타입 정보는 등록되므로 객체를 생성하지 않고 후보 충돌을 발견할 수 있다.
- `ApplicationContext`는 내부 `BeanFactory`를 준비하고 설정 분석과 컨테이너 초기화 흐름을 지휘한다.
- `BeanFactory`는 Bean 정의 보관, 의존성 해결, Bean 생성·저장·조회를 담당한다.
- `ApplicationContext.getBean()`은 내부 `BeanFactory`에 조회를 위임하며, 기본 Singleton은 같은 저장 객체를 반환한다.
- 기본 Singleton은 컨텍스트 초기화 중 미리 생성되고, `@Lazy` Singleton은 실제로 필요해질 때 생성된다.
- Singleton이 공유하는 변경 가능한 객체는 필드뿐 아니라 메서드 인자로 전달된 객체도 호출 간 간섭을 만들 수 있다.
- annotation은 메타데이터이며 스스로 코드를 실행하지 않는다.
- 컴파일러, annotation processor, Spring 같은 처리 주체가 annotation을 읽고 해석해야 동작이 발생한다.
- Reflection은 실행 중 클래스 구조와 annotation 정보를 조사하는 기능이며, 조사만으로 대상 메서드가 실행되지는 않는다.

## 아직 실험으로 검증하지 못한 것

- `isAnnotationPresent()`가 annotation 존재 여부만 반환하고 대상 메서드는 실행하지 않는지
- Spring이 Reflection으로 얻은 생성자·매개변수 정보를 Bean 생성 및 의존성 연결에 사용하는 구체적인 흐름
- 요청이 Controller까지 도착하는 전체 과정
- Bean 생성 이후 초기화 콜백과 BeanPostProcessor가 실행되는 구체적인 순서
- Spring 프록시와 `@Transactional`의 동작 원리
- JPA, Hibernate, Spring Data JPA의 역할 차이
- 영속성 컨텍스트와 flush 시점

## 현재 실습 환경

- `labs/spring-lab`: Java 17, Spring Boot 4.1.0, Gradle Wrapper 9.5.1
- 2026-07-26 전체 테스트 성공: 9개 실행, 실패 0개
- 생성자에 전달된 참조 유지, 구현체 교체, 동일 타입 후보 충돌, `@Primary`, `@Qualifier`, `@Lazy` 후보의 생성 시점, `ApplicationContext`와 내부 `BeanFactory`의 Singleton 조회를 검증한다.

## 다음 행동

1. annotation은 메타데이터이고 처리 주체가 동작을 만든다는 내용을 회상한다.
2. annotation 존재 확인과 대상 메서드 실행을 분리한 최소 Reflection 테스트를 수행한다.
3. Reflection으로 얻은 생성자·매개변수 정보가 Bean 정의와 의존성 해결로 이어지는 흐름을 단계별로 설명한다.

## 다음 세션 시작 요청

```text
AGENTS.md와 CURRENT.md를 읽고,
annotation 존재 확인과 대상 메서드 실행이 왜 별개의 동작인지
내가 먼저 설명하게 한 뒤 최소 Reflection 실험을 진행해 줘.
테스트 보일러플레이트는 제공하고 개념을 검증하는 예측과 assertion에 집중시켜 줘.
내가 답하기 전에는 정답이나 완성 코드를 말하지 마.
```
