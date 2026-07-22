# 4주차: AOP와 트랜잭션

## 핵심 질문

- Spring은 원본 객체 호출 전후에 어떻게 부가 기능을 실행하는가?
- 프록시를 거치지 않는 호출에는 왜 AOP가 적용되지 않는가?
- 트랜잭션 경계와 DB connection의 생명주기는 어떻게 연결되는가?
- runtime exception과 checked exception의 기본 rollback 규칙은 왜 다른가?

## 최소 실험

1. 외부 호출과 self-invocation을 비교한다.
2. public, protected, private 메서드의 적용 여부를 확인한다.
3. checked exception과 runtime exception의 rollback 결과를 비교한다.
4. `REQUIRED`와 `REQUIRES_NEW`의 독립 rollback을 확인한다.

## 완료 증거

- 각 실험의 예상 결과와 실제 DB 상태
- TransactionSynchronizationManager를 이용한 활성 상태 관찰
- 실무에서 발생 가능한 실패 시나리오 설명
