---
date: 2026-08-12
roadmap_item: "TX-03 rollback 규칙과 readOnly"
completion_status: "completed"
recommended_next: "TX-04 propagation과 isolation"
processed: false
environment_mode: "company_pc_restricted"
---

# TX-03 rollback 규칙과 readOnly

## 선수 항목

- `TX-02 flush, commit, rollback`: completed

## 학습한 핵심 개념

- 기본 `@Transactional`은 `RuntimeException`과 `Error`를 rollback 대상으로 판정한다.
- checked exception은 기본 rollback 대상이 아니므로 예외가 호출자에게 전파돼도 commit될 수 있다.
- `rollbackFor`는 지정한 예외를 rollback 대상으로 판정하게 만드는 명시적 규칙이다.
- 예외는 반환되는 것이 아니라 target 밖으로 전파된다.
- target 내부에서 예외를 catch하여 처리하면 프록시는 정상 반환을 관찰할 수 있다.
- `readOnly=true`는 모든 쓰기를 차단하거나 자동 rollback하는 강제 규칙이 아니라 읽기 전용 힌트다.
- JPA `persist()`와 `JdbcTemplate.update()`는 DB에 변경을 전달하는 시점이 다르다.

## 실행 전 예측

```text
기본 + checked exception: 최종 1행
기본 + runtime exception: 최종 0행
rollbackFor + checked exception: 최종 0행

readOnly + JPA persist:
- 트랜잭션 내부 JDBC count: 0
- 최종 count: 0

readOnly + JdbcTemplate.update:
- updateCount: 1
- 트랜잭션 내부 count: 1
- 최종 count: 1
실험 코드
새 테스트 파일:
labs/spring-lab/src/test/java/transaction/RollbackRuleReadOnlyTest.java
테스트 메서드:
rollback_result_depends_on_exception_type_and_rule
read_only_effect_depends_on_write_path_and_resource
준비 과정의 실패와 분석
최초 실행에서 다음 오류가 발생했다.
Table "TX03_RECORD" not found
원인은 실제 테스트 package가 transaction인데 JPA 스캔 범위를 다음처럼 다른 package로 지정했기 때문이다.
factory.setPackagesToScan("studyplan.tx.rollbackrule");
Hibernate가 TxRecord 엔티티를 찾지 못해 tx03_record 테이블을 생성하지 않았고, 테스트 본문 실행 전 @BeforeEach의 DELETE가 실패했다.
다음처럼 현재 테스트 package를 기준으로 스캔하도록 수정했다.
factory.setPackagesToScan(
        RollbackRuleReadOnlyTest.class.getPackageName()
);
수정 후 두 테스트가 모두 성공했다.
검증 결과
예외 유형과 rollback 규칙
기본 + checked exception: 최종 1행
기본 + runtime exception: 최종 0행
rollbackFor + checked exception: 최종 0행
실행 흐름:
target에서 예외 전파
→ TransactionInterceptor가 예외 관찰
→ 기본 또는 명시적 rollback 규칙 판정
→ 트랜잭션 관리자에게 commit 또는 rollback 요청
→ 최종 DB 상태 결정
checked exception은 기본 rollback 대상이 아니므로 commit됐다.
runtime exception은 기본 rollback 대상이므로 rollback됐다.
rollbackFor에 지정된 checked exception도 rollback됐다.
readOnly와 JPA persist
insideCount: 0
finalCount: 0
실행 흐름:
persist()
→ 엔티티를 영속성 컨텍스트에 등록
→ DB INSERT에는 flush 필요
→ 현재 Hibernate readOnly 환경에서 자동 flush 억제
→ INSERT SQL 실행 안 됨
→ 최종 0행
EntityManager.find()는 1차 캐시의 엔티티를 반환할 수 있으므로 DB INSERT 실행을 증명하지 않는다.
readOnly와 JdbcTemplate.update
updateCount: 1
insideCount: 1
finalCount: 1
실행 흐름:
JdbcTemplate.update()
→ JDBC INSERT SQL 즉시 실행
→ 현재 H2가 readOnly 힌트를 쓰기 금지로 강제하지 않음
→ target 정상 반환
→ commit
→ 최종 1행
따라서 readOnly=true는 모든 쓰기 시도의 차단이나 자동 rollback을 보장하지 않는다.
반례 확인
다음과 같이 target 내부에서 RuntimeException을 처리하면 행이 남는다.
@Transactional
public void save() {
    jdbcTemplate.update("insert ...");

    try {
        throw new RuntimeException();
    } catch (RuntimeException e) {
        // 내부 처리
    }
}
실행 흐름:
INSERT 즉시 실행
→ RuntimeException 발생
→ target 내부 catch에서 처리
→ 프록시까지 예외가 전파되지 않음
→ TransactionInterceptor가 정상 반환 관찰
→ commit
→ 행이 남음
예외가 발생했다는 사실만으로 rollback을 단정할 수 없으며, 프록시까지 전파된 종료 방식과 rollback 규칙을 확인해야 한다.
확인된 오개념과 교정
오개념
readOnly=true이면 모든 쓰기가 차단된다.
메서드 내부에서 RuntimeException이 발생하면 항상 rollback된다.
예외를 TransactionInterceptor가 반환받는다.
JdbcTemplate.update()도 DB 반영에 flush가 필요하다.
교정
readOnly=true는 강제 쓰기 차단이나 자동 rollback을 보장하지 않는다.
target 내부에서 예외를 처리하면 프록시는 정상 반환을 관찰하여 commit할 수 있다.
예외는 반환되는 것이 아니라 호출 스택을 따라 전파된다.
JdbcTemplate.update()는 JPA 영속성 컨텍스트의 flush를 기다리지 않고 JDBC SQL을 즉시 실행한다.
완료 판단
다음을 모두 확인했다.
checked exception, runtime exception, rollbackFor의 최종 DB 상태 assertion
JPA와 JDBC readOnly 경로의 SQL 전달 방식 및 최종 상태 비교
최초 설정 실패의 원인 분석과 수정
예외 발생 사실만으로 rollback을 단정할 수 없는 이유 재설명
readOnly가 보장하지 않는 범위 재설명
회상 문제 3개와 면접 질문 2개 수행
따라서 TX-03 rollback 규칙과 readOnly를 completed로 판단한다.
테스트 결과
RollbackRuleReadOnlyTest
- rollback_result_depends_on_exception_type_and_rule: 성공
- read_only_effect_depends_on_write_path_and_resource: 성공
다음 제안
TX-04 propagation과 isolation
이 값은 회사 PC 세션의 제안이다. 쓰기 가능한 환경에서 테스트와 완료 기준을 확인한 뒤 CURRENT.md에 반영한다.