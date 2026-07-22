# 8주 학습 로드맵

하루 90분, 주 5일을 기본으로 하되 이해가 부족한 주제는 일정에 맞추기 위해 넘기지 않습니다.

## 1주차: 웹 애플리케이션 기반

- HTTP 요청과 응답
- Servlet과 Servlet Container
- Tomcat의 요청 처리와 스레드
- Filter와 Listener
- DispatcherServlet의 위치

완료 기준: 브라우저 요청이 Controller 메서드까지 도착하는 과정을 그림 없이 설명할 수 있다.

## 2주차: Spring Container와 DI

- IoC와 DI
- BeanDefinition과 ApplicationContext
- Bean 등록과 조회
- Component Scan
- 생성자 주입
- Bean 생명주기와 Singleton scope
- `@Configuration`과 `@Bean`

완료 기준: 객체 생성과 의존관계를 누가 언제 결정하는지 설명하고 실험으로 검증할 수 있다.

## 3주차: Spring MVC

- DispatcherServlet
- HandlerMapping과 HandlerAdapter
- ArgumentResolver
- HttpMessageConverter
- Validation
- 예외 처리
- Filter, Interceptor, AOP의 차이

완료 기준: JSON 요청이 DTO로 변환되고 응답 JSON이 만들어지는 흐름을 설명할 수 있다.

## 4주차: AOP와 트랜잭션

- JDK Dynamic Proxy와 CGLIB
- Spring AOP 적용 범위
- self-invocation
- 트랜잭션 시작, flush, commit, rollback
- propagation과 isolation
- rollback 규칙과 readOnly

완료 기준: `@Transactional`이 동작하지 않는 사례를 호출 구조와 로그로 진단할 수 있다.

## 5주차: JPA 기초

- JPA 명세, Hibernate 구현체, Spring Data JPA의 관계
- EntityManager와 영속성 컨텍스트
- 엔티티 생명주기
- 변경 감지와 flush
- 연관관계의 주인
- cascade와 orphanRemoval

완료 기준: 엔티티 상태 변화와 SQL 실행 시점을 예측할 수 있다.

## 6주차: JPA 조회와 성능

- 프록시와 지연 로딩
- N+1
- fetch join과 EntityGraph
- 페이징
- OSIV
- 락과 동시성

완료 기준: API 호출당 SQL의 종류와 횟수를 예측하고 N+1을 재현·개선할 수 있다.

## 7주차: 테스트와 Spring Boot

- 단위 테스트와 통합 테스트
- `@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`
- MockMvc와 Testcontainers
- Auto Configuration
- Configuration Properties와 Profile
- Actuator

완료 기준: 테스트 목적에 따라 Spring Context 범위를 선택하고 이유를 설명할 수 있다.

## 8주차: 운영 가능한 주문·재고 기능

- REST API와 예외 응답
- 트랜잭션 경계
- 동시 재고 차감
- DB 통합 테스트
- 부하 테스트
- 메트릭과 로그
- Docker 실행

완료 기준: 기능 구현뿐 아니라 정합성, 실패 시나리오, 테스트 근거를 README로 설명할 수 있다.
