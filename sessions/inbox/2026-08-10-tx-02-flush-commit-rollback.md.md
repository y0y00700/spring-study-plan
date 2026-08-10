---
date: 2026-08-10
roadmap_item: "TX-02 flush, commit, rollback"
completion_status: completed
recommended_next: "TX-03 rollback 규칙과 readOnly"
processed: true
environment_mode: restricted
---

# TX-02 flush, commit, rollback

## 기준 문서

- AGENTS.md 확인
- ROADMAP_DETAIL.md 확인
- CURRENT.md 확인
- 문서 간 진행 순서 충돌 없음

## 선수 항목

- TX-01 트랜잭션 시작과 Connection: completed

## 핵심 개념

- `persist()`는 엔티티를 영속성 컨텍스트에서 관리하게 한다.
- `flush()`는 영속성 컨텍스트의 변경을 SQL 실행 단계와 동기화한다.
- `commit`은 DB 트랜잭션을 정상적으로 완료하여 변경을 최종 확정한다.
- `rollback`은 해당 트랜잭션의 미완료 변경을 폐기한다.
- SQL 실행과 DB 트랜잭션의 최종 반영은 서로 다른 사건이다.

## 최소 실험

`FlushCommitRollbackTest`에서 다음 두 경로를 비교했다.

1. `persist → flush → 정상 반환 → commit`
2. `persist → flush → RuntimeException → rollback`

실험 조건:

- 직접 `EntityManager.persist()`를 사용해 Spring Data JPA `save()`의 `persist`·`merge` 선택 변수를 제거했다.
- 수동 할당 ID를 사용해 ID 생성 전략에 따른 조기 INSERT 가능성을 제거했다.
- flush 전후에는 `JdbcTemplate`으로 현재 트랜잭션 Connection의 DB 상태를 조회했다.
- 완료 전 외부 관찰은 별도의 물리 Connection을 사용했다.
- 격리 수준은 `READ_COMMITTED`로 고정했다.
- SQL 로그는 보조 자료로만 사용하고 행 개수 assertion으로 결과를 판정했다.

## 예측 및 assertion 결과

| 관찰 시점 | 정상 반환 | RuntimeException |
|---|---:|---:|
| persist 후, flush 전 같은 Connection | 0 | 0 |
| flush 후, 같은 Connection | 1 | 1 |
| flush 후, 완료 전 별도 Connection | 0 | 0 |
| 트랜잭션 완료 후 별도 Connection | 1 | 0 |

작성한 모든 핵심 assertion이 통과했고 테스트 성공을 확인했다.

## 실행 흐름 재설명

### 정상 반환

```text
persist
→ 영속성 컨텍스트에서 엔티티 관리
→ flush
→ INSERT SQL 실행
→ 같은 Connection에서는 행 1개 관찰
→ READ_COMMITTED의 별도 Connection에서는 행 0개 관찰
→ target 정상 반환
→ TransactionInterceptor가 commit 요청
→ 최종 DB 행 1개
RuntimeException
persist
→ 영속성 컨텍스트에서 엔티티 관리
→ flush
→ INSERT SQL 실행
→ 같은 Connection에서는 행 1개 관찰
→ READ_COMMITTED의 별도 Connection에서는 행 0개 관찰
→ RuntimeException 전파
→ TransactionInterceptor가 rollback 요청
→ 미커밋 변경 폐기
→ 최종 DB 행 0개
확인 및 교정한 오개념
rollback과 이전 관찰
초기에는 예외 경로가 나중에 rollback된다는 이유로 flush 직후 같은 Connection의 행 수도 0일 것으로 예측했다.
rollback은 과거에 SQL이 실행되지 않은 것으로 시간을 되돌리는 것이 아니다. SQL은 실제로 실행됐으며 rollback 전 같은 Connection에서는 변경을 관찰할 수 있다. rollback은 그 미완료 변경이 최종 DB 상태에 반영되지 않도록 폐기한다.
스레드와 Connection 가시성
초기에는 별도 Connection에서 행이 보이지 않는 이유를 별도 스레드이기 때문이라고 설명했다.
같은 스레드에서도 별도의 물리 Connection을 얻을 수 있다. 이번 실험에서 별도 Connection이 행을 보지 못한 직접적인 이유는 READ_COMMITTED 격리 수준에서 다른 트랜잭션의 미커밋 변경을 읽을 수 없기 때문이다.
현재 트랜잭션 Connection 재사용은 다음 경로로 이루어진다.
JdbcTemplate
→ DataSourceUtils
→ TransactionSynchronizationManager가 현재 스레드에 연결한 자원 조회
EntityManager 조회와 SQL 증명
persist() 직후 entityManager.find()가 엔티티를 반환해도 INSERT 실행을 증명하지 못한다.
영속성 컨텍스트의 1차 캐시에서 DB SELECT 없이 같은 객체를 반환할 수 있기 때문이다. 따라서 SQL 실행 시점을 검증하려면 flush 전후 같은 DB Connection의 행 개수를 비교해야 한다.
완료 기준 확인
SQL 실행과 최종 DB 반영이 같은 사건이 아닌 이유를 설명했다.
flush 후에도 rollback될 수 있는 이유를 실행 순서로 설명했다.
정상 반환과 예외 경로의 결과를 실행 전에 예측했다.
flush 전후와 트랜잭션 완료 전후의 DB 상태를 assertion으로 검증했다.
SQL 로그만으로 commit을 단정할 수 없는 이유를 설명했다.
회상 문제 3개와 면접 질문 2개에 답하고 불명확한 부분을 교정했다.
후속 확인 후보
ID 생성 전략에 따라 INSERT 시점이 달라지는 경우
Spring Data JPA save()가 persist()와 merge() 중 하나를 선택하는 기준
위 항목들은 이번 세션에 추가하지 않고 JPA 관련 roadmap item의 후속 후보로 남긴다.
다음 진행 제안
TX-03 rollback 규칙과 readOnly
recommended_next는 제안이며 확정 진도가 아니다. 쓰기 가능 환경에서 이 세션의 테스트 결과와 roadmap 완료 기준을 다시 확인한 뒤 CURRENT.md에 반영한다.

2026-08-11 쓰기 가능 환경에서 전체 테스트 50개 성공을 확인한 뒤 `CURRENT.md`와 관련 문서에 반영했습니다.
