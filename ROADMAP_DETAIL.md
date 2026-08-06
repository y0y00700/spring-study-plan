# 세부 학습 로드맵

이 문서는 `ROADMAP.md`의 8주 방향을 실제 튜터링 세션 단위로 나눈 기준 문서다.
기간보다 완료 기준을 우선하며, 이해가 부족한 항목을 일정 때문에 건너뛰지 않는다.

## 문서별 책임

- `AGENTS.md`: 튜터링 방식과 진도 통제 규칙
- `ROADMAP.md`: 전체 단계와 장기 완료 기준
- `ROADMAP_DETAIL.md`: 세션 순서, 선수 항목, 실험, 완료 기준
- `CURRENT.md`: 현재 완료 항목과 다음 진행 항목
- `sessions/`: 실제 학습 내용과 검증 결과

## 현재 저장소의 진행 순서

기초 진단 결과에 따라 Spring Container 학습을 웹 기반 학습보다 먼저 시작했다.
이미 시작한 Container 흐름을 중간에 끊지 않고 다음 순서로 진행한다.

```text
CON Spring Container
→ WEB 웹 애플리케이션 기반
→ MVC Spring MVC
→ AOP/TX 프록시와 트랜잭션
→ JPA ORM과 Hibernate
→ TST/OPS 테스트와 운영
→ CAP 종합 실습
```

현재 위치와 다음 항목은 반드시 `CURRENT.md`를 기준으로 한다.

## 진도 상태

- `completed`: 최소 실험과 완료 기준을 충족하고 회상 설명까지 확인한 상태
- `in_progress`: 현재 학습 중인 상태
- `needs_review`: 실험은 끝났지만 설명이나 회상 검증이 부족한 상태
- `pending`: 아직 시작하지 않은 상태

새 항목은 바로 앞의 선수 항목이 `completed`일 때만 시작한다.
예외적으로 순서를 바꾸려면 사용자가 명시적으로 요청하고 `CURRENT.md`에 이유를 남긴다.

---

## CON: Spring Container와 DI

### CON-01 객체 생성 책임과 생성자 주입

- 선수 항목: 기초 진단
- 핵심 개념: 인터페이스와 생성 책임, 외부 조립
- 최소 실험: 두 구현체를 직접 조립하고 생성자에 전달해 반환값 비교
- 완료 기준: 구현체 변경이 사용하는 객체에 전파되지 않는 이유를 참조 흐름으로 설명한다.

### CON-02 Singleton과 공유 상태

- 선수 항목: CON-01
- 핵심 개념: Singleton 동일성, 변경 가능한 공유 상태
- 최소 실험: 같은 Bean 또는 같은 변경 가능 객체를 두 호출이 공유할 때 간섭 재현
- 완료 기준: Singleton Bean을 무상태로 설계해야 하는 이유를 동시 요청 관점에서 설명한다.

### CON-03 동일 타입 Bean 후보 선택

- 선수 항목: CON-02
- 핵심 개념: 타입 후보 검색, `@Primary`, `@Qualifier`
- 최소 실험: 후보 충돌 실패와 두 선택 규칙의 우선순위 비교
- 완료 기준: 후보 검색 조건과 최종 선택 규칙을 분리해 설명한다.

### CON-04 BeanDefinition과 컨테이너 책임

- 선수 항목: CON-03
- 핵심 개념: BeanDefinition, BeanFactory, ApplicationContext
- 최소 실험: Bean 정의 등록 시점과 Singleton 객체 조회 결과 비교
- 완료 기준: 메타데이터와 실제 객체를 구분하고 두 컨테이너 인터페이스의 책임을 설명한다.

### CON-05 메타데이터 탐색과 생성자 의존성 해결

- 선수 항목: CON-04
- 핵심 개념: annotation·Reflection, 생성자 호출 전 의존 객체 준비
- 최소 실험: annotation 조회와 메서드 호출을 분리하고 의존 객체 생성 순서 기록
- 완료 기준: 후보 Bean 정의 검색부터 생성자 인자 전달까지 순서대로 설명한다.

### CON-06 Bean 초기화와 BeanPostProcessor

- 선수 항목: CON-05
- 핵심 개념: 인스턴스화와 초기화, 초기화 전후 후처리
- 최소 실험: 생성자부터 초기화 콜백까지 순서 기록, 후처리기가 반환한 프록시 조회
- 완료 기준: 초기화 이벤트 순서와 컨테이너에 공개되는 객체가 바뀌는 이유를 설명한다.

### CON-07 Component Scan

- 선수 항목: CON-06
- 핵심 개념: 탐색 대상과 BeanDefinition 등록
- 최소 실험: scan 범위 안팎의 컴포넌트와 include·exclude filter 결과 비교
- 완료 기준: 객체 생성 전에 어떤 클래스가 Bean 후보로 등록되는지 설명한다.

### CON-08 `@Configuration`과 `@Bean` 프록시

- 선수 항목: CON-07
- 핵심 개념: 설정 클래스 프록시, Bean 메서드 호출과 Singleton 보장
- 최소 실험: `proxyBeanMethods` 설정에 따른 직접 메서드 호출 결과 비교
- 완료 기준: 설정 클래스 프록시가 필요한 경우와 불필요한 경우를 설명한다.

### CON-09 소멸 콜백과 Scope

- 선수 항목: CON-08
- 핵심 개념: 컨텍스트 종료와 소멸 콜백, Singleton·Prototype 책임
- 최소 실험: 컨텍스트 종료 시 콜백과 Prototype Bean의 소멸 처리 차이 관찰
- 완료 기준: 생성과 소멸을 컨테이너가 어디까지 책임지는지 Scope별로 설명한다.

### CON-10 Container 종합 진단

- 선수 항목: CON-09
- 핵심 개념: 등록, 선택, 생성, 초기화, 공개, 소멸의 전체 흐름
- 최소 실험: 새 Bean 하나의 전체 생명주기를 실행 전에 예측하고 assertion으로 검증
- 완료 기준: 객체 생성과 의존관계를 누가 언제 결정하는지 자료 없이 설명한다.

---

## WEB: 웹 애플리케이션 기반

### WEB-01 HTTP 요청과 응답 경계

- 선수 항목: CON-10
- 핵심 개념: 요청·응답 메시지, 메서드·경로·헤더·본문
- 최소 실험: 같은 경로에 서로 다른 메서드와 본문을 보내 결과 비교
- 완료 기준: 네트워크 메시지와 Java 객체를 구분해 설명한다.

### WEB-02 Servlet과 Servlet Container

- 선수 항목: WEB-01
- 핵심 개념: Servlet 생명주기, Container의 생성·호출 책임
- 최소 실험: `init`, `service`, `destroy` 호출 횟수와 순서 기록
- 완료 기준: Servlet을 누가 만들고 언제 호출하는지 설명한다.

### WEB-03 Tomcat 스레드와 공유 객체

- 선수 항목: WEB-02
- 핵심 개념: 요청별 스레드, 여러 요청이 공유하는 인스턴스
- 최소 실험: 두 동시 요청이 같은 Servlet 또는 Singleton 상태에 접근하는 상황 재현
- 완료 기준: 요청 스레드와 Singleton Bean의 관계를 실패 가능성과 함께 설명한다.

### WEB-04 Filter, Listener, DispatcherServlet의 위치

- 선수 항목: WEB-03
- 핵심 개념: Servlet Container 확장 지점, Spring MVC 진입점
- 최소 실험: Filter 전후와 Controller 호출 이벤트 순서 기록
- 완료 기준: HTTP 요청이 DispatcherServlet에 도착하기 전 단계를 순서대로 설명한다.

---

## MVC: Spring MVC

### MVC-01 DispatcherServlet 요청 처리

- 선수 항목: WEB-04
- 핵심 개념: Front Controller, 요청 위임
- 최소 실험: DispatcherServlet 진입부터 Controller 호출까지 디버거 또는 로그 이벤트 관찰
- 완료 기준: DispatcherServlet이 직접 비즈니스 로직을 실행하지 않는 이유를 설명한다.

### MVC-02 HandlerMapping과 HandlerAdapter

- 선수 항목: MVC-01
- 핵심 개념: Handler 탐색과 호출 방식의 분리
- 최소 실험: 매핑 성공·실패와 어댑터 호출 경로 비교
- 완료 기준: 두 구성요소를 분리한 이유를 실행 순서로 설명한다.

### MVC-03 ArgumentResolver와 HttpMessageConverter

- 선수 항목: MVC-02
- 핵심 개념: 메서드 인자 해석, HTTP 본문 변환
- 최소 실험: 요청 파라미터·경로 변수·JSON 본문의 변환 경로 비교
- 완료 기준: JSON이 언제 DTO가 되고 응답 객체가 언제 JSON이 되는지 설명한다.

### MVC-04 Validation과 예외 처리

- 선수 항목: MVC-03
- 핵심 개념: 변환 실패와 검증 실패, `@ControllerAdvice`
- 최소 실험: 잘못된 타입·검증 위반·비즈니스 예외의 응답 경로 비교
- 완료 기준: 세 실패가 발생하는 위치와 처리 주체를 구분한다.

### MVC-05 Filter, Interceptor, AOP 경계

- 선수 항목: MVC-04
- 핵심 개념: 실행 위치와 사용 가능한 컨텍스트
- 최소 실험: 세 확장 지점의 전후 이벤트와 예외 전달 범위 비교
- 완료 기준: 인증·로깅·실행 시간 측정의 배치 위치를 근거와 함께 선택한다.

---

## AOP/TX: 프록시와 트랜잭션

### AOP-01 JDK Dynamic Proxy와 CGLIB

- 선수 항목: MVC-05
- 핵심 개념: 인터페이스 기반 프록시와 클래스 기반 프록시
- 최소 실험: 두 프록시 방식의 런타임 타입과 호출 위임 비교
- 완료 기준: 원본과 프록시를 구분하고 각 방식의 제약을 설명한다.

### AOP-02 Advice 적용과 self-invocation

- 선수 항목: AOP-01
- 핵심 개념: 프록시 호출 경계, 내부 호출 우회
- 최소 실험: 외부 호출과 같은 객체 내부 호출의 Advice 적용 여부 비교
- 완료 기준: self-invocation에서 부가 기능이 적용되지 않는 이유를 호출 경로로 설명한다.

### TX-01 트랜잭션 시작과 Connection

- 선수 항목: AOP-02
- 핵심 개념: TransactionInterceptor, 스레드에 연결된 자원
- 최소 실험: 트랜잭션 안팎에서 활성 상태와 Connection 동일성 관찰
- 완료 기준: `@Transactional` 이후 누가 트랜잭션을 시작하는지 설명한다.

### TX-02 flush, commit, rollback

- 선수 항목: TX-01
- 핵심 개념: 영속성 컨텍스트 동기화와 DB 트랜잭션 완료
- 최소 실험: flush와 commit 시점을 분리하고 예외 전후 DB 상태 비교
- 완료 기준: SQL 실행과 최종 반영이 같은 사건이 아닌 이유를 설명한다.

### TX-03 rollback 규칙과 `readOnly`

- 선수 항목: TX-02
- 핵심 개념: checked·runtime exception 기본 규칙, 읽기 전용 힌트
- 최소 실험: 두 예외 유형과 rollback 설정의 DB 결과 비교
- 완료 기준: 예외가 발생했다는 사실만으로 rollback을 단정하지 않는다.

### TX-04 propagation과 isolation

- 선수 항목: TX-03
- 핵심 개념: `REQUIRED`·`REQUIRES_NEW`, 격리 수준
- 최소 실험: 내부 트랜잭션의 독립 rollback과 동시 읽기 현상 비교
- 완료 기준: 전파와 격리 수준이 해결하는 문제를 구분한다.

---

## JPA: JPA와 Hibernate

### JPA-01 JPA, Hibernate, Spring Data JPA

- 선수 항목: TX-04
- 핵심 개념: 명세, 구현체, 저장소 추상화
- 최소 실험: Repository 호출부터 Hibernate SQL까지 실제 타입과 호출 경로 관찰
- 완료 기준: 세 기술의 책임을 대체 관계가 아닌 계층 관계로 설명한다.

### JPA-02 EntityManager와 영속성 컨텍스트

- 선수 항목: JPA-01
- 핵심 개념: 엔티티 관리, 1차 캐시와 동일성
- 최소 실험: 같은 ID를 두 번 조회하고 객체 동일성과 SQL 횟수 검증
- 완료 기준: “관리한다”는 표현을 상태와 기능으로 풀어 설명한다.

### JPA-03 엔티티 생명주기

- 선수 항목: JPA-02
- 핵심 개념: 비영속·영속·준영속·삭제
- 최소 실험: `persist`, `detach`, `remove` 전후 상태와 SQL 관찰
- 완료 기준: 각 상태에서 변경 감지 가능 여부를 예측한다.

### JPA-04 변경 감지와 flush

- 선수 항목: JPA-03
- 핵심 개념: 스냅샷 비교, 쓰기 지연 SQL 실행
- 최소 실험: 명시적 flush 전후와 commit 전후의 SQL·DB 상태 비교
- 완료 기준: setter 호출이 즉시 UPDATE SQL을 의미하지 않는 이유를 설명한다.

### JPA-05 연관관계의 주인

- 선수 항목: JPA-04
- 핵심 개념: 객체 참조 방향과 외래키 갱신 책임
- 최소 실험: 주인·비주인 측만 수정했을 때 외래키 결과 비교
- 완료 기준: 양방향 편의 메서드와 DB 갱신 책임을 구분한다.

### JPA-06 cascade와 orphanRemoval

- 선수 항목: JPA-05
- 핵심 개념: 영속성 전이와 고아 제거
- 최소 실험: 부모 저장·삭제·컬렉션 제거 시 SQL 비교
- 완료 기준: 두 설정을 생명주기 소유권 관점에서 선택한다.

### JPA-07 프록시와 지연 로딩

- 선수 항목: JPA-06
- 핵심 개념: 프록시 초기화, 영속성 컨텍스트 의존
- 최소 실험: 트랜잭션 안팎의 연관 객체 접근과 SQL 시점 비교
- 완료 기준: LazyInitializationException의 원인을 호출 위치로 진단한다.

### JPA-08 N+1과 조회 전략

- 선수 항목: JPA-07
- 핵심 개념: N+1, fetch join, EntityGraph
- 최소 실험: 동일 조회의 SQL 횟수를 세고 두 개선 방법과 비교
- 완료 기준: API 호출당 SQL 종류와 횟수를 실행 전에 예측한다.

### JPA-09 페이징과 OSIV

- 선수 항목: JPA-08
- 핵심 개념: 컬렉션 fetch join 페이징 제약, 영속성 컨텍스트 범위
- 최소 실험: 페이징 경고·쿼리와 OSIV 설정별 지연 로딩 범위 비교
- 완료 기준: 편의성과 Connection 점유의 trade-off를 설명한다.

### JPA-10 락과 동시성

- 선수 항목: JPA-09
- 핵심 개념: 낙관적 락, 비관적 락, 유실 업데이트
- 최소 실험: 두 트랜잭션의 동시 수정 충돌 재현
- 완료 기준: 업무 충돌 빈도와 실패 처리 방식에 따라 락을 선택한다.

---

## TST/OPS: 테스트와 운영

### TST-01 단위 테스트와 Spring 통합 테스트

- 선수 항목: JPA-10
- 핵심 개념: 격리 범위, 테스트가 보장하는 사실
- 최소 실험: 같은 기능을 순수 단위 테스트와 `@SpringBootTest`로 검증
- 완료 기준: 테스트 속도가 아니라 검증 책임을 기준으로 종류를 선택한다.

### TST-02 테스트 Slice

- 선수 항목: TST-01
- 핵심 개념: `@WebMvcTest`, `@DataJpaTest`, Context 범위
- 최소 실험: 두 Slice와 전체 Context의 등록 Bean 차이 비교
- 완료 기준: Mock 대상과 실제 구성요소의 경계를 설명한다.

### TST-03 실제 DB 통합 테스트

- 선수 항목: TST-02
- 핵심 개념: 인메모리 DB 차이, Testcontainers
- 최소 실험: 실제 RDB에서 제약조건·쿼리·락 동작 검증
- 완료 기준: 운영 DB에서만 드러날 수 있는 실패를 테스트에 연결한다.

### OPS-01 Auto Configuration과 설정

- 선수 항목: TST-03
- 핵심 개념: 조건부 자동 설정, Configuration Properties·Profile
- 최소 실험: 조건과 Profile 변경에 따른 Bean 등록 결과 비교
- 완료 기준: 설정값이 실제 Bean 구성에 반영되는 흐름을 설명한다.

### OPS-02 관찰 가능성과 종료

- 선수 항목: OPS-01
- 핵심 개념: health·metric, graceful shutdown
- 최소 실험: readiness·liveness와 종료 신호 처리 관찰
- 완료 기준: 장애 가설을 로그·메트릭·상태 지표와 연결한다.

---

## CAP: 운영 가능한 주문·재고 기능

### CAP-01 REST API와 예외 계약

- 선수 항목: OPS-02
- 핵심 개념: 요청·응답 계약, 일관된 실패 응답
- 최소 실험: 정상·검증 실패·비즈니스 실패 API 구현과 테스트
- 완료 기준: HTTP 상태와 도메인 실패를 구분해 설계한다.

### CAP-02 주문·재고 트랜잭션

- 선수 항목: CAP-01
- 핵심 개념: 애플리케이션 서비스 경계, 정합성
- 최소 실험: 주문 성공·실패 시 재고와 주문 데이터의 원자성 검증
- 완료 기준: 트랜잭션 경계를 유스케이스 기준으로 설명한다.

### CAP-03 동시 재고 차감

- 선수 항목: CAP-02
- 핵심 개념: 경쟁 조건, 락과 재시도
- 최소 실험: 동시 요청으로 초과 차감 재현 후 선택한 전략으로 개선
- 완료 기준: 정합성 결과와 처리량 trade-off를 수치로 설명한다.

### CAP-04 운영 검증과 최종 설명

- 선수 항목: CAP-03
- 핵심 개념: DB 통합 테스트, 부하, 메트릭, Docker 실행
- 최소 실험: 재현 가능한 실행 환경에서 핵심 시나리오와 장애 지표 검증
- 완료 기준: 설계 이유, SQL 예측, 실패 시나리오, 테스트 근거를 README와 면접 답변으로 설명한다.

---

## 세션 시작 템플릿

튜터는 새 세션을 시작하기 전에 다음을 먼저 제시한다.

```text
현재 항목:
선수 항목과 완료 상태:
이번 핵심 개념 1~2개:
최소 실험:
완료 기준:
```

## 회사 PC 세션 종료 기록

회사 PC에서는 저장소 문서를 직접 수정하지 않고 단일 inbox Markdown에 다음 값을 포함한다.

```yaml
roadmap_item: CON-07
completion_status: completed
recommended_next: CON-08
processed: false
```

`recommended_next`는 제안일 뿐 확정 진도가 아니다.
집 PC에서 실험 결과와 완료 기준을 검증한 뒤 `CURRENT.md`의 다음 항목을 확정한다.