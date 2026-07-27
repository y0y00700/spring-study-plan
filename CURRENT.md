# Current Learning Context

마지막 갱신일: 2026-07-28

## 현재 단계

Spring 컨테이너 기초 진행 중 — Bean 인스턴스화와 초기화를 구분하고, 초기화 콜백 전후의 BeanPostProcessor 실행 및 반환 객체 공개 흐름을 최소 재현 실험으로 검증한 단계

## 현재 주제

Spring Bean 초기화 생명주기와 BeanPostProcessor

## 로드맵 진행 위치

- 상세 기준: `ROADMAP_DETAIL.md`
- 최근 완료 항목: `CON-06 Bean 초기화와 BeanPostProcessor`
- 다음 진행 항목: `CON-07 Component Scan`
- 진행 순서: `CON` 완료 후 `WEB → MVC → AOP/TX → JPA → TST/OPS → CAP`

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
- `Method.isAnnotationPresent()`는 해당 `Method`가 나타내는 메서드에 지정 annotation이 있는지 확인할 뿐, 메서드 본문을 실행하지 않는다.
- `Method.invoke(target)`는 annotation 존재 여부와 독립적으로 `target` 객체의 대상 메서드를 실제 호출한다.
- `@Retention(RUNTIME)` annotation은 실행 중 Reflection으로 조회할 수 있지만, `CLASS` annotation은 `.class` 파일에 남아도 일반적인 실행 중 Reflection 조회 결과에는 나타나지 않는다.
- annotation은 동작을 지시하는 메타데이터이며, Spring 같은 처리 주체가 이를 읽고 해석하여 Bean 정의 등록·객체 생성 같은 실제 동작을 수행한다.
- Spring의 모든 메타데이터 탐색이 Java Reflection인 것은 아니며, 클래스패스 탐색 과정에서는 `.class` 메타데이터를 직접 읽을 수도 있다.
- BeanFactory는 의존 객체를 필요로 하는 생성자를 호출하기 전에 생성자 매개변수 타입을 확인하고 후보 Bean 정의를 검색한다.
- 후보 Singleton 객체가 이미 있으면 조회하고, 없으면 먼저 생성한 뒤 그 객체 참조를 생성자 인자로 전달한다.
- Reflection으로 `OrderService` 생성자의 매개변수 타입이 `PaymentProcessor`임을 확인했고, 실제 Spring 컨텍스트에서 `PaymentProcessor 생성 → OrderService 생성` 순서를 검증했다.
- 인스턴스화는 생성자를 호출해 객체를 만드는 단계이고, 초기화는 객체 생성과 의존관계·컨테이너 정보 설정 후 준비 작업을 수행하는 단계다.
- `@PostConstruct`는 생성자 기능이 아니라 Spring의 후처리기가 annotation을 찾아 호출하는 초기화 콜백이다.
- 현재 실험에서는 `constructor → setBeanName → BeanPostProcessor before → @PostConstruct → afterPropertiesSet → BeanPostProcessor after` 순서로 실행됐다.
- `@PostConstruct`와 `InitializingBean.afterPropertiesSet()`을 함께 사용하면 현재 Spring 환경에서는 `@PostConstruct`가 먼저 호출된다.
- `BeanPostProcessor`는 개별 Bean 내부가 아니라 컨테이너 차원에서 여러 Bean의 초기화 전후를 가공하는 확장 지점이다.
- `postProcessAfterInitialization()`이 원본 대신 프록시를 반환하면 컨텍스트 조회 결과는 원본이 아니라 그 프록시가 되며, 프록시가 대상 객체 호출을 위임할 수 있다.

## 아직 실험으로 검증하지 못한 것

- Spring 내부에서 생성자 선택·매개변수 의존성 해결·생성자 호출을 담당하는 실제 클래스와 메서드
- 요청이 Controller까지 도착하는 전체 과정
- Component Scan의 탐색 범위와 BeanDefinition 등록 과정
- `@Configuration` 설정 클래스 프록시와 `@Bean` 메서드 호출
- 소멸 콜백과 Scope별 컨테이너 책임
- Spring 프록시와 `@Transactional`의 동작 원리
- JPA, Hibernate, Spring Data JPA의 역할 차이
- 영속성 컨텍스트와 flush 시점

## 현재 실습 환경

- `labs/spring-lab`: Java 17, Spring Boot 4.1.0, Gradle Wrapper 9.5.1
- 2026-07-28 전체 테스트 성공: 14개 실행, 실패 0개
- 기존 컨테이너 실험에 더해 Bean 인스턴스화·Aware 콜백·초기화 콜백·BeanPostProcessor의 실행 순서와 후처리기가 반환한 프록시의 공개를 검증한다.

## 다음 행동

1. 인스턴스화와 초기화의 차이 및 여섯 생명주기 이벤트의 순서를 회상한다.
2. BeanPostProcessor가 원본 대신 다른 객체를 반환했을 때 컨텍스트 조회 결과가 달라지는 이유를 설명한다.
3. `CON-07 Component Scan`에서 scan 범위 안팎의 클래스가 BeanDefinition으로 등록되는 결과를 예측하고 최소 실험으로 검증한다.

## 다음 세션 시작 요청

```text
AGENTS.md, ROADMAP_DETAIL.md, CURRENT.md를 모두 읽고,
현재 roadmap item, 선수 항목, 오늘의 핵심 개념,
최소 실험과 완료 기준을 먼저 알려 줘.
오늘 검증한 Bean 인스턴스화·초기화의 차이와
constructor부터 BeanPostProcessor after까지의 실행 순서를 먼저 회상시켜 줘.
그 다음 BeanPostProcessor가 원본 대신 프록시를 반환했을 때
컨텍스트가 어떤 객체를 공개하는지 내가 설명하게 해 줘.
회상이 끝나면 `CON-07 Component Scan`을 진행하되,
scan 범위 안팎의 Bean 등록 결과를 내가 먼저 예측하게 해 줘.
문서에 지정되지 않은 다음 주제를 임의로 추가하지 마.
테스트 보일러플레이트는 제공하고 실행 순서 예측과 assertion에 집중시켜 줘.
```
