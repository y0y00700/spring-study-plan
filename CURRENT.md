# Current Learning Context

마지막 갱신일: 2026-07-30

## 현재 단계

Spring 컨테이너 기초 완료 — Bean 등록, 의존성 선택·해결, 생성, 초기화, 공개, 소멸의 전체 흐름과 Scope별 컨테이너 책임을 최소 재현 실험으로 검증한 단계

## 현재 주제

Container 종합 진단

## 로드맵 진행 위치

- 상세 기준: `ROADMAP_DETAIL.md`
- 최근 완료 항목: `CON-10 Container 종합 진단`
- 다음 진행 항목: `WEB-01 HTTP 요청과 응답 경계`
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
- `@SpringBootApplication`은 `@ComponentScan`을 포함하며, 별도 base package 지정이 없으면 애플리케이션 클래스의 패키지와 하위 패키지를 탐색한다.
- `@Component`는 스스로 Bean을 등록하지 않으며, 스캐너가 탐색 범위 안의 클래스 메타데이터를 읽고 후보로 판단해야 BeanDefinition이 등록된다.
- Component Scan은 먼저 base package로 탐색 범위를 정하고, 그 범위 안의 클래스에 기본·include·exclude filter를 적용한다.
- include filter는 base package 밖으로 탐색 범위를 확장하지 않고, exclude filter와 일치한 클래스는 Bean 후보에서 제외한다.
- 스캔 과정의 BeanDefinition 등록과 실제 Bean 인스턴스 생성은 별개 단계다.
- `lazyInit`은 BeanDefinition 등록 여부가 아니라 Singleton 인스턴스 생성 시점을 변경한다.
- `proxyBeanMethods = true`이면 Spring이 설정 클래스를 프록시로 확장하고, `@Bean` 메서드 간 직접 호출을 가로채 BeanFactory가 관리하는 객체를 반환한다.
- 설정 클래스 프록시는 `@Bean` 메서드 호출을 막는 것이 아니라 가로채며, 대상 Bean의 Singleton·Prototype 같은 Scope 규칙을 적용한다.
- `proxyBeanMethods = false`이면 `@Bean` 메서드 간 직접 호출은 일반 Java 호출이므로 메서드 본문의 `new`가 다시 실행될 수 있다.
- `proxyBeanMethods = false`여도 `@Bean` 분석과 BeanDefinition 등록, BeanFactory의 의존성 검색·객체 생성·저장·조회는 계속 동작한다.
- `@Bean` 메서드의 의존성을 다른 `@Bean` 메서드 직접 호출 대신 메서드 매개변수로 받으면, BeanFactory가 관리 객체를 전달하므로 설정 클래스 프록시 없이도 중복 생성을 피할 수 있다.
- 기본 non-lazy Singleton은 컨텍스트 초기화 중 미리 생성되며, Prototype은 BeanFactory에 요청할 때마다 새로 생성된다.
- Spring 컨테이너는 Prototype의 생성, 의존관계 설정, 초기화까지만 담당하고 호출자에게 전달한 뒤에는 인스턴스와 소멸 콜백을 추적하지 않는다.
- 컨텍스트를 닫으면 추적 중인 Singleton의 `@PreDestroy`는 호출되지만 Prototype의 `@PreDestroy`는 자동 호출되지 않는다.
- Prototype이 외부 자원을 보유하면 호출자나 별도의 관리 Bean이 각 인스턴스의 명시적인 정리 메서드를 호출해야 한다.
- BeanDefinition 등록 단계에서는 타입, 팩토리 메서드, Scope 등 생성 메타데이터를 기록하고 실제 후보 선택과 객체 참조 연결은 Bean 생성 시점에 수행한다.
- BeanFactory는 의존 Bean을 먼저 생성·초기화하거나 저장된 Singleton을 조회한 뒤 그 참조를 팩토리 메서드에 전달해 의존 객체를 생성한다.
- 기본 Singleton은 생성과 초기화가 끝난 뒤 저장·공개되므로 반복 조회해도 생성자와 `@PostConstruct`가 다시 실행되지 않는다.
- 의존 BeanDefinition이 없으면 BeanFactory의 후보 검색 단계에서 실패하므로 의존 객체를 받는 서비스 생성자와 이후 초기화·저장은 실행되지 않는다.

## 아직 실험으로 검증하지 못한 것

- Spring 내부에서 생성자 선택·매개변수 의존성 해결·생성자 호출을 담당하는 실제 클래스와 메서드
- 요청이 Controller까지 도착하는 전체 과정
- Spring 프록시와 `@Transactional`의 동작 원리
- JPA, Hibernate, Spring Data JPA의 역할 차이
- 영속성 컨텍스트와 flush 시점

## 현재 실습 환경

- `labs/spring-lab`: Java 17, Spring Boot 4.1.0, Gradle Wrapper 9.5.1
- 2026-07-30 전체 테스트 성공: 20개 실행, 실패·오류 0개
- `BeanDestructionScopeTest`: Singleton·Prototype의 생성 횟수, 참조 동일성, 컨텍스트 종료 후 소멸 콜백 횟수를 검증한다.
- `ContainerLifecycleIntegrationTest`: BeanDefinition 등록부터 의존 Bean 우선 생성, 초기화, Singleton 공개·반복 조회, 컨텍스트 종료 시 소멸까지 전체 이벤트 순서를 검증한다.

## 다음 행동

1. Singleton과 Prototype의 생성·소멸 책임 차이를 회상한다.
2. BeanDefinition 등록부터 Singleton 소멸까지 컨테이너의 전체 흐름을 자료 없이 설명한다.
3. `WEB-01 HTTP 요청과 응답 경계`에서 네트워크 요청·응답 메시지와 Java 객체를 구분하고, 같은 경로에 서로 다른 메서드와 본문을 보내 결과를 비교한다.

## 다음 세션 시작 요청

```text
AGENTS.md, ROADMAP_DETAIL.md, CURRENT.md를 모두 읽고,
현재 roadmap item, 선수 항목, 오늘의 핵심 개념,
최소 실험과 완료 기준을 먼저 알려 줘.
Singleton과 Prototype의 생성·소멸 책임 차이와
BeanDefinition 등록부터 Singleton 소멸까지의 흐름을 먼저 회상시켜 줘.
회상이 끝나면 `WEB-01 HTTP 요청과 응답 경계`를 진행하되,
같은 경로에 서로 다른 메서드와 본문을 보냈을 때의 결과를 내가 먼저 예측하게 해 줘.
문서에 지정되지 않은 다음 주제를 임의로 추가하지 마.
테스트 보일러플레이트는 제공하고 실행 순서 예측과 assertion에 집중시켜 줘.
```
