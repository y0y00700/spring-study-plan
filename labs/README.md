# Labs

Spring 개념을 격리해 검증하는 실험 코드를 둡니다.

책의 목차와 기초 진단 결과를 확인한 뒤 `spring-lab` 프로젝트를 생성합니다. 처음에는 프로젝트를 주제마다 나누지 않고 테스트 패키지로 구분합니다.

## 현재 환경

- Java 17
- Spring Boot 4.1.0
- Gradle Wrapper 9.5.1
- 테스트 웹 환경: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-aspectj`, 내장 Tomcat, Java `HttpClient`
- 테스트 트랜잭션 환경: Spring JDBC, H2 인메모리 DB, `DataSourceTransactionManager`
- 테스트 실행: `labs/spring-lab`에서 `.\gradlew.bat test`

2026-08-09 기준 전체 테스트 48개가 성공했습니다. 실패·오류·건너뜀 테스트는 없습니다.

- `SpringLabApplicationTests`: Spring 컨텍스트 로딩
- `SingletonSharedStateTest`: 메서드 인자로 같은 변경 가능한 리스트를 공유할 때 호출 결과가 간섭하는지 검증
- `ConstructorInjectionTest`: 생성자에 전달된 객체 참조 유지와 두 `PaymentProcessor` 구현체 교체 검증
- `SpringBeanSelectionTest`: 동일 타입 Bean 후보 충돌, `@Primary` 기본 선택, `@Qualifier` 명시 선택, `@Lazy` 후보의 생성 지연, `ApplicationContext`와 내부 `BeanFactory`의 동일 Singleton 조회 검증
- `AnnotationReflectionTest`: `RUNTIME`·`CLASS` 보존 정책에 따른 annotation 조회 결과와 `Method.invoke()`의 독립적인 실행 검증
- `SpringConstructorResolutionTest`: 생성자 매개변수 타입 조회와 `PaymentProcessor 생성 → OrderService 생성` 순서 검증
- `BeanLifecycleOrderTest`: 인스턴스화·Aware 콜백·초기화 콜백·BeanPostProcessor의 실행 순서와 후처리기가 반환한 프록시 공개 검증
- `ComponentScanExperimentTest`: scan 범위 안팎과 include·exclude filter에 따른 BeanDefinition 등록 결과, lazy Bean의 조회 전후 생성 횟수 검증
- `ConfigurationProxyExperimentTest`: `proxyBeanMethods` 설정과 `@Bean` 직접 호출·메서드 매개변수 주입에 따른 객체 동일성 및 생성 횟수 검증
- `BeanDestructionScopeTest`: Singleton·Prototype의 조회별 생성 횟수와 참조 동일성, 컨텍스트 종료 후 Scope별 소멸 콜백 횟수 검증
- `ContainerLifecycleIntegrationTest`: BeanDefinition 등록, 의존 Bean 우선 생성, `@PostConstruct`, Singleton 공개·반복 조회, 컨텍스트 종료 후 `@PreDestroy`까지 전체 이벤트 순서 검증
- `HttpRequestResponseBoundaryTest`: 임의 포트의 실제 서버에 같은 경로의 GET·POST·PUT을 보내 메서드·본문별 상태 코드와 응답 본문 검증
- `ServletLifecycleContainerTest`: Tomcat이 Servlet을 생성하고 `init()`·`service()`·`destroy()`를 호출하는 순서, 두 요청의 Servlet 인스턴스 동일성 검증
- `TomcatThreadSharedStateTest`: 두 동시 요청을 서로 다른 Tomcat 스레드가 처리하면서 같은 Singleton Controller의 필드를 공유해 요청 간 간섭이 발생하는 것을 검증
- `FilterListenerDispatcherServletOrderTest`: 정상 요청과 Filter 차단 요청의 HTTP 상태 코드, Listener·Filter·Controller 이벤트 순서, `chain.doFilter()`의 진행 통제를 검증
- `DispatcherServletDelegationTest`: 실제 DispatcherServlet의 진입부터 HandlerMapping 탐색, HandlerAdapter 호출 위임, Controller 실행까지 이벤트 순서를 검증
- `HandlerMappingAdapterSeparationTest`: 하나의 HandlerMapping이 경로별로 다른 Handler를 반환할 때 지원 가능한 HandlerAdapter만 선택되어 Handler를 호출하고, 매핑 실패 시 Adapter 호출 없이 404가 되는 흐름을 검증
- `ArgumentResolverMessageConverterTest`: 경로 변수·요청 파라미터·JSON 본문을 Controller 인자로 준비하고 반환 객체를 JSON으로 쓰는 정상 경로와, 타입 오류·필수 파라미터 누락·깨진 JSON에서 Controller 호출이 중단되는 경로를 검증
- `ValidationExceptionAdviceTest`: 경로 변수 타입 불일치·DTO 검증 위반·비즈니스 예외가 발생하는 위치와 Controller 진입 여부, `@RestControllerAdvice`가 변환한 상태 코드·오류 본문을 검증
- `FilterInterceptorAopBoundaryTest`: 정상 요청·Controller 예외·Filter 예외에서 Filter·Interceptor·AOP·Controller·Advice의 전후 이벤트와 예외 전달 범위를 검증
- `JdkDynamicProxyCglibTest`: JDK Dynamic Proxy·CGLIB의 런타임 타입과 원본 호출 위임, 구현 클래스 캐스팅, `final` 클래스·메서드 제약, `proceed()` 없는 Advice의 원본 호출 차단을 검증
- `SelfInvocationAdviceTest`: 외부 `inner()` 호출과 `outer()` 내부의 `this.inner()` 호출에서 Advice 적용 여부와 target 메서드 실행 이벤트 비교
- `TransactionStartConnectionTest`: `@Transactional` 호출 전·내부·반환 후의 활성 상태, 스레드에 연결된 DataSource 자원과 같은 트랜잭션의 Connection 참조 동일성 검증

```text
spring-lab/src/test/java/com/study/springlab/
├─ container/
│  ├─ BeanDestructionScopeTest.java
│  └─ ContainerLifecycleIntegrationTest.java
├─ mvc/
├─ transaction/
└─ jpa/

spring-lab/src/test/java/study/constructorinjection/
├─ ConstructorInjectionTest.java
└─ SpringBeanSelectionTest.java

spring-lab/src/test/java/study/reflection/
├─ AnnotationReflectionTest.java
└─ SpringConstructorResolutionTest.java

spring-lab/src/test/java/study/lifecycle/
└─ BeanLifecycleOrderTest.java

spring-lab/src/test/java/study/componentscan/
├─ ComponentScanExperimentTest.java
├─ inside/InsideCandidates.java
└─ outside/OutsideCandidates.java

spring-lab/src/test/java/study/configuration/
└─ ConfigurationProxyExperimentTest.java

spring-lab/src/test/java/webboundary/
├─ HttpRequestResponseBoundaryTest.java
└─ ServletLifecycleContainerTest.java

spring-lab/src/test/java/tomcat/
└─ TomcatThreadSharedStateTest.java

spring-lab/src/test/java/webfoundation/
└─ FilterListenerDispatcherServletOrderTest.java

spring-lab/src/test/java/mvc/
├─ DispatcherServletDelegationTest.java
├─ HandlerMappingAdapterSeparationTest.java
├─ ArgumentResolverMessageConverterTest.java
├─ ValidationExceptionAdviceTest.java
└─ FilterInterceptorAopBoundaryTest.java

spring-lab/src/test/java/aop/
├─ JdkDynamicProxyCglibTest.java
└─ SelfInvocationAdviceTest.java

spring-lab/src/test/java/transaction/
└─ TransactionStartConnectionTest.java
```

모든 실험에는 다음 내용을 남깁니다.

1. 실행 전 예상
2. 예상의 근거
3. 관찰할 대상
4. 실제 결과를 검증하는 assertion
5. 예상과 실제 결과가 달랐던 이유
