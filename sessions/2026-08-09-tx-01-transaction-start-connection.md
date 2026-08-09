# 2026-08-09: TX-01 트랜잭션 시작과 Connection

## 진행 상태

- roadmap_item: `TX-01 트랜잭션 시작과 Connection`
- completion_status: `completed`
- recommended_next: `TX-02 flush, commit, rollback`

## 지난 세션 회상 복습

- 외부 호출은 프록시의 Pointcut 검사와 Advice를 거쳐 `proceed()`로 원본 target 메서드까지 진행한다고 회상했다.
- `proceed()` 자체가 self-invocation을 의미하는 것은 아니며, target 메서드 본문에 `this.inner()`가 있을 때 별도의 내부 호출이 발생한다고 보완했다.
- target 내부의 `this.inner()`는 `target → target` 호출이므로 프록시와 Advice를 우회한다고 설명했다.

## 학습 전 내 생각

- `@Transactional` target 메서드 안의 트랜잭션 활성 상태와 DataSource 자원 연결 상태를 예측하지 못했다.
- 같은 트랜잭션 안에서 `DataSourceUtils`로 두 번 얻은 Connection이 같은 참조인지 예측하지 못했다.
- 메서드 반환 후 외부의 트랜잭션 활성 상태도 예측하지 못했다.

## 핵심 설명

- `@Transactional`은 트랜잭션 적용 대상을 나타내는 메타데이터이며 스스로 트랜잭션을 시작하지 않는다.
- 트랜잭션 프록시는 외부 호출을 가로채 `TransactionInterceptor`가 실행되는 호출 사슬로 진입시킨다.
- `TransactionInterceptor`는 트랜잭션 관리자에게 시작을 요청하고, 준비가 끝난 뒤 `proceed()`로 target 메서드를 호출한다.
- JDBC 실험의 `DataSourceTransactionManager`는 DataSource에서 Connection을 얻어 트랜잭션을 준비하고 현재 스레드에 자원을 연결한다.
- `DataSourceUtils`와 `JdbcTemplate`은 현재 스레드에 연결된 DataSource 자원을 고려해 같은 트랜잭션의 Connection을 사용할 수 있다.
- target 반환 후 `TransactionInterceptor`가 트랜잭션 완료를 요청하면 관리자가 스레드 연결을 해제하고 Connection을 반환한다.

## 실험

- 기준 디렉터리: `labs/spring-lab`
- 파일: `src/test/java/transaction/TransactionStartConnectionTest.java`
- 클래스: `TransactionStartConnectionTest`
- 메서드: `transactionStartsBeforeTargetAndReusesConnection()`
- Spring JDBC와 H2 인메모리 DB를 테스트 의존성으로 추가했다.
- `@EnableTransactionManagement`, CGLIB 프록시, `DataSourceTransactionManager`, H2 DataSource로 독립 컨텍스트를 구성했다.
- 테스트 구조와 fixture는 제공하고 학습자가 호출 전·내부·호출 후 상태의 예상 boolean을 직접 작성했다.

## 실제 결과

- `@Transactional` 호출 전의 트랜잭션 활성 상태는 `false`였다.
- target 메서드 안의 트랜잭션 활성 상태와 DataSource 자원 연결 상태는 모두 `true`였다.
- 같은 트랜잭션 안에서 `DataSourceUtils`로 얻은 두 Connection 참조는 같았다.
- target 반환과 트랜잭션 완료 후 외부의 활성 상태는 다시 `false`였다.
- `TransactionStartConnectionTest` 1개와 전체 Gradle 테스트 48개가 성공했으며 실패·오류·건너뜀은 0개였다.

## 실패 분석

- 최초 진단에서는 네 관찰값을 예측하지 못했으나 프록시 호출 경계와 스레드 연결 자원의 실행 순서를 설명한 뒤 모든 예상값을 올바르게 작성했다.
- 첫 테스트 실행은 sandbox에서 Gradle 배포 파일 접근이 거부되어 실패했고, 승인된 Gradle 테스트 실행으로 같은 명령을 다시 실행해 성공했다.
- assertion 실패는 없었다.

## 내가 다시 설명하기

- 외부 호출이 CGLIB 프록시를 거쳐 `TransactionInterceptor`로 전달되고, 트랜잭션과 Connection 준비 후 `proceed()`가 target을 호출한다고 설명했다.
- target 반환 후 트랜잭션 완료, 스레드의 Connection 연결 해제와 반환을 거쳐 외부 호출자에게 복귀한다고 설명했다.
- 프록시는 호출을 가로채고, `TransactionInterceptor`는 트랜잭션 시작을 요청하며, `DataSourceTransactionManager`는 Connection으로 실제 시작을 처리한다고 책임을 구분했다.

## 마무리 회상 확인

- `@Transactional`은 메타데이터, 프록시는 호출 가로채기, `TransactionInterceptor`는 시작·완료 요청, `DataSourceTransactionManager`는 Connection 기반 JDBC 트랜잭션 처리라고 구분했다.
- `DataSourceUtils`가 현재 스레드에 연결된 같은 DataSource의 자원을 우선 확인하므로 같은 트랜잭션의 Connection을 사용할 수 있다고 설명했다.
- `DataSourceTransactionManager`는 비즈니스 SQL을 직접 실행하지 않고 Connection의 트랜잭션 시작·완료·정리를 담당하며 실제 SQL은 Repository나 `JdbcTemplate`이 실행한다고 보완했다.
- 새 스레드에서는 기존 트랜잭션의 스레드 연결 정보가 자동 전파되지 않아 같은 DataSource를 사용해도 기존 Connection을 얻지 못한다고 보완했다.

## 남은 질문

- flush로 SQL이 실행되는 시점과 DB commit으로 다른 트랜잭션에 최종 반영되는 시점은 어떻게 다른가?
- SQL이 이미 실행된 뒤 예외가 발생하면 rollback이 DB 상태를 어떻게 되돌리는가?
- `@Transactional` self-invocation이 실제 트랜잭션 활성 상태에 미치는 영향은 후속 관련 실험에서 확인한다.

## 회상 문제

1. `@Transactional` annotation, 트랜잭션 프록시, `TransactionInterceptor`의 역할을 구분한다.
2. `TransactionInterceptor`와 `DataSourceTransactionManager`가 트랜잭션 시작 과정에서 각각 무엇을 담당하는지 설명한다.
3. 같은 트랜잭션 안에서 Repository들이 Connection 매개변수를 직접 전달받지 않고 같은 자원을 사용할 수 있는 이유를 설명한다.

## 면접 질문

1. Spring에서 `@Transactional` 메서드를 호출한 뒤 실제 JDBC 트랜잭션이 시작되기까지의 실행 순서를 설명해 주세요.
2. Spring이 JDBC Connection을 현재 스레드에 연결하는 이유와 이 방식의 경계를 설명해 주세요.

## 다음 복습일

2026-08-12
