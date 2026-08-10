# Current Learning Context

마지막 갱신일: 2026-08-11

## 현재 단계

AOP/TX 진행 중 — flush와 commit을 분리하고 정상 반환·RuntimeException 경로의 최종 DB 상태를 검증한 단계

## 현재 주제

flush, commit, rollback (`completed`)

## 로드맵 진행 위치

- 상세 기준: `ROADMAP_DETAIL.md`
- 최근 완료 항목: `TX-02 flush, commit, rollback`
- 현재 진행 항목: 없음
- 다음 진행 항목: `TX-03 rollback 규칙과 readOnly`
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
- HTTP 요청과 응답은 메서드·경로·헤더·본문 등의 요소로 구성된 네트워크 메시지다.
- 서버와 클라이언트는 서로 독립된 실행 환경과 메모리 공간을 사용하므로 Java 객체 참조가 네트워크를 직접 통과하지 않는다.
- 전송 시 요청 객체의 정보와 본문 데이터는 HTTP 메시지의 바이트 표현으로 바뀌고, 수신자는 데이터를 자신의 실행 환경에서 새 값이나 객체로 구성한다.
- 같은 경로라도 HTTP 메서드가 다르면 다른 처리 코드가 선택될 수 있고, 해당 메서드를 처리하는 매핑이 없으면 `405 Method Not Allowed`가 반환될 수 있다.
- HTTP 본문 바이트는 전송할 데이터를 인코딩한 표현이고, Java 바이트코드는 JVM이 실행하는 `.class` 명령이므로 서로 다른 개념이다.
- `loadOnStartup=1`인 Servlet은 서버 시작 과정에서 Servlet Container가 인스턴스를 생성하고 `init()`을 한 번 호출한다.
- 요청마다 Servlet Container가 같은 Servlet 인스턴스의 `service(request, response)`를 호출하므로 생성자·`init()`의 횟수와 `service()`의 횟수는 다르다.
- 서버 종료 시 Servlet Container가 `destroy()`를 한 번 호출하지만, 이는 자원 정리 기회를 주는 콜백이며 객체 메모리를 직접 회수하는 연산은 아니다.
- Servlet 객체의 실제 메모리 회수는 참조 관계가 끊어진 이후 JVM GC가 담당한다.
- 이번 실험에서 Spring ApplicationContext는 `ServletContextInitializer` Bean을 관리했고, Initializer는 Servlet 클래스를 등록했다.
- 등록된 `LifecycleServlet` 객체의 생성과 `init()`·`service()`·`destroy()` 호출은 Servlet Container인 Tomcat이 담당했다.
- Tomcat은 동시 HTTP 요청을 서로 다른 요청 스레드에서 처리할 수 있다.
- Spring Singleton Controller는 인스턴스가 하나이며 여러 요청 스레드가 같은 인스턴스를 호출한다.
- Singleton Controller의 인스턴스 필드에 요청별 변경 상태를 저장하면 다른 요청이 그 값을 덮어써 요청 간 간섭이 발생할 수 있다.
- 메서드 지역변수는 호출별 스택 프레임에 별도로 존재하므로, Singleton이라는 이유만으로 지역변수 자체가 요청 간 공유되지는 않는다.
- 서로 다른 지역변수가 Singleton 필드에 저장된 같은 `ArrayList` 같은 변경 가능 객체를 가리키면, 지역변수는 분리되어도 참조 대상의 상태는 공유된다.
- `CountDownLatch` 실험으로 `A 저장 → B 덮어쓰기 → A 재개`를 고정했고, A 요청도 최종적으로 공유 필드의 `B`를 읽는 결과를 검증했다.
- Tomcat은 요청 처리 시작과 종료에 `ServletRequestListener` 콜백을 호출하며, Listener는 요청을 다음 단계로 전달하는 호출 사슬이 아니라 요청 생명주기를 관찰하는 확장 지점이다.
- Filter의 `chain.doFilter()`는 남아 있는 다음 Filter 또는 최종 Servlet을 호출하고 그 처리가 끝나면 복귀하는 중첩 메서드 호출이다.
- Spring MVC 웹 요청에서 Filter chain 뒤의 최종 Servlet은 DispatcherServlet이고, Controller는 `chain.doFilter()` 호출 동안 DispatcherServlet 내부에서 호출된다.
- 정상 요청의 이벤트는 `requestInitialized → filter-before → controller → filter-after → requestDestroyed` 순서로 실행됐다.
- Filter가 상태 코드를 설정하고 `chain.doFilter()` 없이 `return`하면 DispatcherServlet과 Controller는 호출되지 않고, 같은 Filter의 `return` 뒤 후처리도 실행되지 않는다.
- Filter가 요청을 차단해도 요청 처리 생명주기는 종료되므로 Tomcat은 Listener의 `requestDestroyed()`를 호출한다.
- Filter는 요청·응답을 검사·변경하거나 다음 단계 진행을 통제할 수 있지만, 정상 요청의 응답을 항상 Filter가 생성하는 것은 아니다.
- Handler는 DispatcherServlet 관점에서 현재 요청을 처리하도록 선택된 대상이며, 이번 실험에서는 Spring Bean인 `TestController` 객체였다.
- DispatcherServlet은 모든 요청을 받는 Front Controller이며, 자신이 특정 Controller의 비즈니스 로직을 직접 실행하지 않고 Handler 탐색과 호출을 협력 객체에 위임한다.
- HandlerMapping은 요청 URI를 보고 Handler를 담은 `HandlerExecutionChain`을 반환하고, 일치하는 Handler가 없으면 `null`을 반환한다.
- DispatcherServlet은 HandlerMapping이 찾은 Handler를 호출 가능한 HandlerAdapter에 전달하고, HandlerAdapter가 실제 Controller 메서드를 호출한다.
- 실제 요청 이벤트는 `dispatcher-servlet → handler-mapping → handler-adapter → controller` 순서로 실행됐다.
- HandlerMapping이 Handler를 반환하지 않으면 DispatcherServlet은 HandlerAdapter와 Controller를 호출할 수 없다.
- DispatcherServlet이 탐색과 호출을 위임하면 새 Controller와 호출 방식이 추가되어도 공통 요청 진입점의 코드를 직접 수정하지 않고 전체 흐름 조정 역할을 유지할 수 있다.
- Handler는 HandlerMapping이 요청을 처리하도록 선택한 대상이고, HandlerAdapter는 선택된 Handler를 지원하는지 `supports(handler)`로 검사한 뒤 `handle()`로 호출하는 별도 협력 객체다.
- DispatcherServlet은 등록 순서대로 HandlerAdapter 후보를 검사하고, 처음으로 `supports(handler)`가 `true`인 Adapter를 선택한다.
- 후보 Adapter의 `supports()` 검사는 실제 Handler 호출이 아니며, 선택된 Adapter 하나의 `handle()`만 실행된다.
- Controller 메서드 호출 전에 각 매개변수에 맞는 ArgumentResolver가 선택되어 실제 인자 값을 준비한다.
- `@PathVariable`과 `@RequestParam`은 문자열 기반 값 조회와 타입 변환 경로를 사용하고, `@RequestBody`용 ArgumentResolver는 본문 변환을 HttpMessageConverter에 위임한다.
- HttpMessageConverter의 `read()`는 요청 JSON 본문을 DTO로 만들고, 반환값 처리기가 위임한 `write()`는 Controller의 반환 객체를 JSON 응답 본문으로 만든다.
- HttpMessageConverter는 HTTP 전체를 변환하지 않고 요청·응답 본문과 Java 객체 사이를 변환한다.
- 인자 하나라도 타입 변환, 필수 값 조회, JSON 읽기에 실패하면 모든 인자가 준비되지 않으므로 Controller 메서드는 실행되지 않는다.
- Handler를 찾은 뒤 필수 요청 파라미터가 누락된 경우는 매핑 실패가 아니라 인자 준비 실패이므로 이번 실험에서는 400이 반환됐다.
- `/orders/abc`도 `/orders/{id}` 경로 패턴에는 일치하며, 실패는 HandlerMapping이 아니라 Controller 인자를 `long`으로 변환하는 단계에서 발생한다.
- 정상 JSON은 먼저 DTO로 변환되고, 그 뒤 `@Valid`와 Bean Validation 제약조건이 검사되므로 DTO 생성과 검증은 별도 단계다.
- 타입 변환 실패와 DTO 검증 실패는 Controller 호출 전에 발생하지만, 재고 없음 같은 비즈니스 예외는 인자 준비와 검증이 끝나 Controller에 진입한 뒤 발생한다.
- 요청 처리 중 발생한 예외는 DispatcherServlet이 `HandlerExceptionResolver` 체인에 위임하고, `ExceptionHandlerExceptionResolver`가 일치하는 `@ExceptionHandler`를 찾아 HTTP 오류 응답으로 변환할 수 있다.
- `@RestControllerAdvice`는 Controller 메서드 밖의 Spring Bean이므로 Controller 진입 전의 인자 처리 예외와 진입 후의 비즈니스 예외를 모두 처리할 수 있다.
- Filter는 Servlet Filter chain 바깥에서 HTTP 요청 전체를 감싸고, Interceptor는 DispatcherServlet 내부의 선택된 Handler 전후에, Spring AOP는 포인트컷과 일치하는 프록시 Bean의 메서드 호출 경계에 적용된다.
- 정상 요청은 `filter-before → interceptor-preHandle → aop-before → controller → aop-after-returning → aop-finally → interceptor-postHandle → interceptor-afterCompletion:null → filter-after` 순서로 실행됐다.
- Controller가 던진 예외는 가까운 호출자인 AOP가 `after-throwing`과 `finally`에서 먼저 관찰한 뒤 DispatcherServlet로 전달되고, 그 뒤 `@RestControllerAdvice`가 HTTP 응답으로 변환한다.
- `postHandle`은 Handler가 정상 반환했을 때 실행되지만, `afterCompletion`은 예외 처리와 렌더링을 포함한 MVC 처리가 끝난 뒤 실행된다.
- `afterCompletion`의 예외 인자는 예외 발생 이력이 아니라 MVC가 최종적으로 해결하지 못한 예외이며, Advice가 처리한 예외 경로에서는 `null`이었다.
- Advice가 Controller 예외를 해결하면 DispatcherServlet과 `chain.doFilter()`가 정상 복귀하므로 일반 `filter-after` 코드가 실행된다.
- Filter가 `chain.doFilter()` 전에 예외를 던지면 DispatcherServlet에 진입하지 않으므로 Interceptor·AOP·Controller와 MVC의 `@RestControllerAdvice`는 실행되지 않는다.
- Aspect가 등록돼도 포인트컷이 대상 Bean 이름이나 메서드와 일치하지 않으면 AOP 이벤트가 실행되지 않는다.
- AOP는 Controller에 한정되지 않으며, HTTP·배치 등 호출자와 무관하게 포인트컷과 일치하고 프록시를 통과한 Spring Bean 메서드 호출에 적용할 수 있다.
- JDK Dynamic Proxy는 원본 구현 클래스의 하위 클래스가 아니라 대상 인터페이스를 구현하는 별도의 런타임 프록시 클래스다.
- CGLIB 프록시는 원본 구현 클래스의 하위 클래스로 생성되므로 구현 클래스와 그 클래스가 구현한 인터페이스 타입 모두에 대입할 수 있다.
- 두 프록시 모두 원본 객체와는 참조 및 런타임 클래스가 다른 별도 객체이며, Advice 실행 뒤 원본 객체에 메서드 호출을 위임한다.
- JDK 프록시는 원본 구현 클래스 타입으로 캐스팅할 수 없지만 CGLIB 프록시는 구현 클래스의 하위 타입이므로 캐스팅할 수 있다. 캐스팅은 새 객체를 만들지 않아 전후 참조는 동일하다.
- CGLIB는 대상 클래스를 상속해야 하므로 `final class`의 프록시 생성은 실패한다.
- 클래스가 상속 가능하더라도 `final method`는 재정의할 수 없어 CGLIB 프록시 생성은 성공하지만 해당 메서드에 Advice가 적용되지 않는다.
- Advice의 `invocation.proceed()`는 다음 Advice 또는 최종 원본 target으로 호출 체인을 진행시키며, 반환값은 역순으로 Advice와 프록시를 거쳐 외부 호출자에게 전달된다.
- Advice가 `proceed()`를 호출하지 않고 값을 바로 반환하면 원본 target 메서드는 실행되지 않고 Advice가 만든 값이 프록시의 반환값이 된다.
- Pointcut은 Advice를 적용할 메서드 호출을 선택하는 규칙이고, Advice는 선택된 호출 전후에 실행할 부가 기능이다.
- 외부의 `proxy.inner()` 호출은 프록시를 통과해 Pointcut 검사와 Advice 실행 기회를 얻는다.
- `target.outer()` 내부의 `this.inner()`에서 `this`는 target이므로 호출은 `target → target`으로 진행되고 프록시를 다시 통과하지 않는다.
- self-invocation은 Advice를 우회하지만 원본 `inner()`의 비즈니스 로직 자체는 정상 실행된다.
- Pointcut이 `outer()`와 `inner()`를 모두 선택해도 외부 `proxy.outer()`의 Advice만 적용되고, 내부 `this.inner()`는 프록시를 거치지 않아 별도의 Advice가 적용되지 않는다.
- `@Transactional`은 트랜잭션 적용 대상을 나타내는 메타데이터이며 annotation 자체가 트랜잭션을 시작하지 않는다.
- 트랜잭션 프록시는 외부 호출을 가로채 `TransactionInterceptor`가 실행되는 호출 사슬로 진입시킨다.
- `TransactionInterceptor`는 `PlatformTransactionManager`에 트랜잭션 시작과 완료를 요청하고 `proceed()`로 원본 target 메서드를 호출한다.
- 현재 JDBC 실험의 `DataSourceTransactionManager`는 DataSource에서 Connection을 얻어 실제 JDBC 트랜잭션을 준비하고 현재 스레드에 자원을 연결한다.
- target 메서드 안에서는 트랜잭션 활성 상태와 DataSource 자원 연결 상태가 모두 `true`였고, `DataSourceUtils`로 두 번 얻은 Connection 참조가 같았다.
- `@Transactional` 메서드 호출 전과 반환 후에는 트랜잭션 활성 상태가 `false`였으므로 트랜잭션 경계가 프록시 호출 내부에 한정됨을 확인했다.
- 같은 스레드의 Repository나 `JdbcTemplate`은 Connection을 매개변수로 전달받지 않아도 `DataSourceUtils`를 통해 현재 트랜잭션에 연결된 자원을 찾을 수 있다.
- `persist()`는 엔티티를 영속성 컨텍스트에서 관리하게 하지만, 이번 수동 ID 실험에서는 그 호출만으로 INSERT SQL이 실행되지 않았다.
- `flush()`는 영속성 컨텍스트의 변경을 DB에 SQL로 동기화하지만 DB 트랜잭션을 최종 확정하지는 않는다.
- flush 후에는 현재 트랜잭션의 같은 Connection에서 INSERT 결과를 관찰할 수 있지만, `READ_COMMITTED`의 별도 물리 Connection에서는 미커밋 변경을 관찰할 수 없다.
- 정상 반환 뒤 commit되면 flush로 실행된 INSERT가 최종 DB 상태에 남고, `RuntimeException` 뒤 rollback되면 같은 SQL이 이미 실행됐어도 최종 DB 상태에는 남지 않는다.
- rollback은 과거의 SQL 실행 사실을 없애는 것이 아니라 현재 트랜잭션의 미완료 변경을 최종 반영하지 않고 폐기한다.
- 별도 Connection에서 미커밋 행이 보이지 않는 직접 원인은 스레드가 다르기 때문이 아니라 `READ_COMMITTED` 격리 수준에서 다른 트랜잭션의 미커밋 변경을 읽을 수 없기 때문이다.
- `EntityManager.find()`는 1차 캐시에서 엔티티를 반환할 수 있으므로, 그 결과만으로 INSERT SQL 실행 여부를 증명할 수 없다.

## 이번 실험에서 확인한 것

- 실제 내장 서버에 `@PathVariable long`, `@RequestParam boolean`, `@RequestBody` DTO를 함께 사용하는 요청을 보내 세 인자가 준비된 뒤 Controller가 호출되는 흐름을 검증했다.
- 정상 요청에서 Controller의 반환 객체가 JSON 응답 본문으로 변환되는 것을 검증했다.
- 잘못된 경로 변수, 필수 요청 파라미터 누락, 깨진 JSON은 각각 400을 반환하고 Controller 호출 횟수가 0인 것을 검증했다.
- `MVC-02`의 기존 매핑 성공·실패 및 Adapter 선택 테스트와 재설명을 함께 확인하여 완료 기준을 충족했다.
- 잘못된 경로 변수 타입은 DTO를 만들거나 Controller를 호출하지 않고 Advice가 400 `TYPE_MISMATCH`로 변환하는 것을 검증했다.
- 정상 JSON으로 DTO를 만든 뒤 Bean Validation이 실패하면 Controller를 호출하지 않고 Advice가 400 `VALIDATION_FAILED`로 변환하는 것을 검증했다.
- DTO 검증을 통과한 뒤 Controller에서 발생한 비즈니스 예외는 Controller 호출 횟수가 1이고 Advice가 409 `SOLD_OUT`으로 변환하는 것을 검증했다.
- `ValidationExceptionAdviceTest` 3개를 포함한 전체 테스트 36개가 성공하여 `MVC-04` 완료 기준을 확인했다.
- 정상 요청에서 Filter·Interceptor·AOP·Controller의 전후 이벤트가 중첩 호출의 진입·복귀 순서와 일치하는 것을 검증했다.
- Controller의 `SoldOutException` 경로에서 `aop-after-throwing → aop-finally → advice-sold-out → interceptor-afterCompletion:null → filter-after` 순서를 검증했다.
- Filter가 DispatcherServlet 진입 전에 예외를 던지면 Filter 이벤트만 기록되고 Advice를 포함한 MVC 확장 지점이 실행되지 않으며 500이 반환되는 것을 검증했다.
- AOP 포인트컷의 Bean 이름 불일치로 AOP 이벤트가 누락되는 실패를 재현하고, 명시적인 Bean 이름 등록 후 같은 assertion이 통과하는 것을 확인했다.
- `FilterInterceptorAopBoundaryTest` 3개를 포함한 전체 테스트 39개가 성공하여 `MVC-05` 완료 기준을 확인했다.
- JDK Dynamic Proxy와 CGLIB 프록시의 참조 차이, 인터페이스·구현 클래스 타입 관계, 런타임 클래스 차이와 `advice-before → target → advice-after-returning → advice-finally` 호출 위임 순서를 검증했다.
- JDK 프록시의 구현 클래스 캐스팅 실패와 CGLIB 프록시의 구현 클래스 캐스팅 성공 및 참조 동일성을 검증했다.
- CGLIB에서 `final class`는 프록시 생성에 실패하고 `final method`는 프록시 생성 후에도 Advice가 적용되지 않는 차이를 검증했다.
- `JdkDynamicProxyCglibTest`의 차단 Advice 결과와 호출 체인을 재설명해 `AOP-01` 완료 기준을 충족했다.
- 외부 `proxy.inner()`에서는 `advice-before → target-inner → advice-after`, `proxy.outer()`의 self-invocation에서는 `target-outer → target-inner`가 기록되는 차이를 검증했다.
- Pointcut이 `outer()`와 `inner()`를 모두 선택하는 반례에서도 내부 `this.inner()`가 `target → target`으로 호출되어 Advice를 우회한다고 재설명해 `AOP-02` 완료 기준을 충족했다.
- `SelfInvocationAdviceTest` 2개를 포함한 전체 테스트 47개가 성공했다.
- `TransactionStartConnectionTest`에서 호출 전 `active=false`, target 내부 `active=true`·`resourceBound=true`·두 Connection 참조 동일, 호출 후 `active=false`를 assertion으로 검증했다.
- 프록시·`TransactionInterceptor`·`DataSourceTransactionManager`의 책임을 나누어 재설명해 `TX-01` 완료 기준을 충족했다.
- `TransactionStartConnectionTest` 1개를 포함한 전체 테스트 48개가 성공했다.
- `FlushCommitRollbackTest`에서 `persist` 직후와 명시적 flush 직후를 비교해 같은 Connection의 행 개수가 `0 → 1`이 되는 것을 검증했다.
- flush 뒤 트랜잭션 완료 전에는 `READ_COMMITTED`의 별도 Connection에서 행 개수가 0이고, 정상 반환 후에는 1, `RuntimeException`으로 rollback된 뒤에는 0인 것을 검증했다.
- SQL 실행과 최종 DB 반영이 서로 다른 사건인 이유와 rollback 전후의 관찰 차이를 재설명해 `TX-02` 완료 기준을 충족했다.
- `FlushCommitRollbackTest` 2개를 포함한 전체 테스트 50개가 성공했다.

## 아직 실험으로 검증하지 못한 것

- Spring 내부에서 생성자 선택·매개변수 의존성 해결·생성자 호출을 담당하는 실제 클래스와 메서드
- `@Transactional` self-invocation이 실제 트랜잭션 활성 상태에 미치는 영향
- JPA, Hibernate, Spring Data JPA의 역할 차이
- ID 생성 전략에 따라 INSERT 실행 시점이 달라지는 경우
- Spring Data JPA `save()`가 `persist()`와 `merge()` 중 하나를 선택하는 기준

## 현재 실습 환경

- `labs/spring-lab`: Java 17, Spring Boot 4.1.0, Gradle Wrapper 9.5.1
- 테스트용 웹 환경: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-aspectj`, 내장 Tomcat, Java `HttpClient`
- 테스트용 트랜잭션 환경: Spring JDBC, Spring Data JPA·Hibernate, H2 인메모리 DB, `DataSourceTransactionManager`
- 2026-08-11 전체 테스트 성공: 50개 실행, 실패·오류·건너뜀 0개
- `BeanDestructionScopeTest`: Singleton·Prototype의 생성 횟수, 참조 동일성, 컨텍스트 종료 후 소멸 콜백 횟수를 검증한다.
- `ContainerLifecycleIntegrationTest`: BeanDefinition 등록부터 의존 Bean 우선 생성, 초기화, Singleton 공개·반복 조회, 컨텍스트 종료 시 소멸까지 전체 이벤트 순서를 검증한다.
- `HttpRequestResponseBoundaryTest`: 실제 임의 포트 서버에 같은 경로의 GET·POST·PUT 요청을 보내 메서드·본문에 따른 상태 코드와 응답 본문 차이를 검증한다.
- `ServletLifecycleContainerTest`: Tomcat이 Servlet을 생성하고 `init()`·`service()`·`destroy()`를 호출하는 순서와 두 요청이 같은 Servlet 인스턴스를 사용하는 사실을 검증한다.
- `TomcatThreadSharedStateTest`: 두 동시 요청을 서로 다른 Tomcat 스레드가 처리하면서 같은 Singleton Controller의 필드를 공유해 A 요청이 B의 값을 읽는 간섭을 검증한다.
- `FilterListenerDispatcherServletOrderTest`: 정상 요청과 Filter 차단 요청에서 Listener·Filter·Controller 이벤트, HTTP 상태 코드, `chain.doFilter()` 호출 여부를 검증한다.
- `DispatcherServletDelegationTest`: 실제 DispatcherServlet이 HandlerMapping에서 Handler를 찾고 HandlerAdapter에 호출을 위임해 Controller까지 도달하는 이벤트 순서를 검증한다.
- `HandlerMappingAdapterSeparationTest`: 하나의 HandlerMapping이 서로 다른 Handler를 반환할 때 `supports()`로 선택된 HandlerAdapter만 Handler를 호출하고, 매핑 실패 시 Adapter 호출 없이 404가 되는 경로를 검증한다.
- `ArgumentResolverMessageConverterTest`: 경로 변수·요청 파라미터·JSON 본문의 정상 인자 준비와 응답 JSON 변환, 세 가지 인자 준비 실패 시 Controller 호출 중단을 검증한다.
- `ValidationExceptionAdviceTest`: 경로 변수 타입 불일치·DTO 검증 위반·비즈니스 예외의 Controller 진입 여부와 `@RestControllerAdvice`가 만든 상태 코드·오류 본문을 검증한다.
- `FilterInterceptorAopBoundaryTest`: 정상 요청·Controller 예외·Filter 예외에서 Filter·Interceptor·AOP·Controller·Advice의 이벤트 순서와 예외 전달 범위를 검증한다.
- `JdkDynamicProxyCglibTest`: JDK Dynamic Proxy·CGLIB의 런타임 타입과 원본 호출 위임, 구현 클래스 캐스팅, `final` 클래스·메서드 제약, `proceed()` 없는 Advice의 원본 호출 차단을 검증한다.
- `SelfInvocationAdviceTest`: 외부 `inner()` 호출과 같은 target 내부의 `this.inner()` 호출에서 Pointcut·Advice 적용 여부와 원본 메서드 실행 이벤트를 비교한다.
- `TransactionStartConnectionTest`: 트랜잭션 호출 전·내부·반환 후 활성 상태, DataSource 자원 연결 상태와 같은 트랜잭션의 Connection 참조 동일성을 검증한다.
- `FlushCommitRollbackTest`: 명시적 flush 전후의 같은 Connection 행 개수, 완료 전 별도 Connection의 가시성, 정상 commit과 RuntimeException rollback 뒤 최종 DB 상태를 검증한다.

## 다음 행동

1. checked exception과 runtime exception의 기본 rollback 규칙을 실행 전에 예측한다.
2. `rollbackFor` 같은 명시적 설정이 기본 규칙을 어떻게 바꾸는지 DB 상태 assertion으로 비교한다.
3. `readOnly`가 강제 불변 규칙인지 최적화 힌트인지 현재 환경에서 관찰한다.
4. 예외 발생 사실만으로 rollback을 단정할 수 없는 이유를 규칙과 실행 경로로 재설명한다.

## 다음 세션 준비

- `TX-03`의 선수 항목인 `TX-02`는 flush 전후·트랜잭션 완료 전후의 DB 상태 assertion과 실행 경로 재설명을 통해 완료했다.
- 다음 세션에서는 로드맵 순서대로 `TX-03 rollback 규칙과 readOnly`만 시작한다.
- 실험 보일러플레이트는 제공하고, 학습자는 실행 전 예외 유형·설정별 rollback 여부와 `readOnly` 결과를 예측하고 핵심 assertion을 작성한다.

## 다음 세션 시작 요청

```text
AGENTS.md, ROADMAP_DETAIL.md, CURRENT.md를 모두 읽고,
현재 roadmap item, 선수 항목, 오늘의 핵심 개념,
최소 실험과 완료 기준을 먼저 알려 줘.
`TX-02 flush, commit, rollback`까지 completed 상태야.
다음 순서인 `TX-03 rollback 규칙과 readOnly`의 사전 개념 설명과 진단부터 시작해 줘.
checked exception·runtime exception·명시적 rollback 설정의 최종 DB 상태와 `readOnly` 동작을 비교하는 최소 실험을 준비하되,
실행 전에 학습자가 결과와 이유를 예측하고 핵심 assertion을 작성하게 해 줘.
문서에 지정되지 않은 다음 주제를 임의로 추가하지 마.
테스트 보일러플레이트는 제공하고 실행 순서 예측과 assertion에 집중시켜 줘.
```
