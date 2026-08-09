# 2026-08-09: AOP-01 복습과 AOP-02 self-invocation

## 진행 상태

- roadmap_item: `AOP-01 JDK Dynamic Proxy와 CGLIB 복습 / AOP-02 Advice 적용과 self-invocation`
- completion_status: `completed`
- recommended_next: `TX-01 트랜잭션 시작과 Connection`

## 지난 세션 회상 복습

- Advice가 `invocation.proceed()`를 호출하는 경우와 값을 바로 반환하는 경우의 target 실행 여부와 반환값을 구분했다.
- 이벤트 목록에 기록되는 값과 target 또는 Advice가 호출자에게 반환하는 값을 분리했다.
- `proceed()`가 다음 Advice 또는 최종 target으로 호출 체인을 진행시키고 반환값이 역순으로 복귀한다고 재설명했다.

## AOP-01 완료 판단

- 기존 `JdkDynamicProxyCglibTest`의 JDK 프록시·CGLIB 타입 관계와 제약 실험은 이미 통과한 상태였다.
- `proceed()`를 호출하면 Advice 다음의 호출 체인이 target까지 진행되고, 호출하지 않으면 target이 실행되지 않으며 Advice의 값이 반환된다고 설명했다.
- 이전에 부족했던 호출 위임 재설명을 확인해 `AOP-01`을 `completed`로 판단했다.

## AOP-02 학습 전 내 생각

- 외부 `proxy.inner()`와 `proxy.outer()` 내부의 `inner()` 호출에서 target 메서드 순서는 예상했지만 Advice 적용 여부를 판단하지 못했다.
- Pointcut을 메서드 내부에 위치하는 것으로 이해했고, self-invocation에서 Advice가 적용되지 않는 이유를 `outer()`가 Pointcut 대상이 아니기 때문이라고 생각했다.
- Advice가 우회되면 내부 `inner()`의 원본 비즈니스 로직도 실행되지 않는 것으로 예상했다.

## 핵심 설명

- Pointcut은 Advice를 적용할 메서드 호출을 선택하는 규칙이고 Advice는 선택된 호출 전후에 실행할 부가 기능이다.
- 외부 `proxy.inner()`는 프록시가 호출을 가로채 Pointcut을 검사하고 Advice를 실행한 뒤 target에 위임한다.
- `target.outer()` 안의 `this.inner()`에서 `this`는 target이므로 호출은 `target → target`으로 진행되고 프록시를 다시 통과하지 않는다.
- self-invocation에서는 Advice만 우회되며 원본 `inner()` 메서드의 비즈니스 로직은 정상 실행된다.
- Pointcut이 `outer()`와 `inner()`를 모두 선택해도 외부 `proxy.outer()`에 대한 Advice만 실행되고 내부 `this.inner()`에는 별도 Advice가 실행되지 않는다.

## 실험

- 기준 디렉터리: `labs/spring-lab`
- 파일: `src/test/java/aop/SelfInvocationAdviceTest.java`
- 클래스: `SelfInvocationAdviceTest`
- 메서드:
  - `externalInnerCallPassesProxy()`
  - `outerCallsInnerOnSameObject()`
- `NameMatchMethodPointcutAdvisor`가 `inner`만 선택하도록 구성하고 CGLIB 프록시의 외부 호출과 target 내부 호출 이벤트를 비교했다.
- 테스트 구조와 fixture는 제공하고 학습자가 두 경로의 `expectedEvents`를 직접 작성했다.

## 실제 결과

- 외부 `proxy.inner()`는 `advice-before → target-inner → advice-after` 순서로 실행됐다.
- 외부 `proxy.outer()`는 `target-outer → target-inner` 순서로 실행됐고 Advice 이벤트는 없었다.
- `SelfInvocationAdviceTest` 2개와 전체 Gradle 테스트 47개가 성공했으며 실패·오류·건너뜀은 0개였다.

## 실패 분석

- 첫 실행에서 `proxy.outer()`의 예상 이벤트를 `advice-before → target-outer → advice-after`로 작성해 실패했다.
- 실제 결과의 `target-outer → target-inner`를 통해 `outer()`는 Pointcut과 일치하지 않고 내부 `inner()`는 target에서 직접 실행되어 Advice만 우회한다는 점을 확인했다.
- 수정 과정에서 외부 `proxy.inner()`와 같은 예상값을 한 번 더 작성했지만, target 이벤트와 Advice 이벤트를 분리해 최종 assertion을 고쳤다.

## 내가 다시 설명하기

- `target.outer()`에서 `this`는 target이므로 `this.inner()`는 `target → target` 호출이라고 설명했다.
- 내부 호출이 프록시를 거치지 않으므로 `inner()`용 Advice가 실행되지 않지만 원본 `inner()`의 비즈니스 로직은 실행된다고 설명했다.
- Pointcut이 `outer()`와 `inner()`에 모두 일치하는 반례에서도 `outer()` Advice만 실행되고 내부 `inner()` Advice는 실행되지 않는 이유를 호출 경로로 설명했다.

## 마무리 회상 확인

- self-invocation에서는 원본 target 객체가 자신의 메서드를 직접 호출하므로 프록시를 통과하지 않고 Advice가 실행되지 않는다고 설명했다.
- 프록시가 외부 메서드 호출을 가로채 Advice를 실행하지만, 객체 내부의 `target(this) → target(this)` 호출에서는 그 경계를 우회한다고 면접 답변 형식으로 설명했다.
- `AOP` 자체가 target을 감싸는 객체라기보다, Spring이 만든 프록시가 target 앞에서 호출을 가로채 AOP의 Advice를 적용한다고 용어를 보완했다.

## 남은 질문

- `@Transactional` 프록시에서 self-invocation이 실제 트랜잭션 활성 상태에 어떤 영향을 주는지는 `TX-01` 이후 관련 실험에서 검증한다.
- 트랜잭션 안팎의 Connection 획득과 스레드에 연결된 자원 관계는 아직 검증하지 않았다.

## 회상 문제

1. `invocation.proceed()`를 호출하는 Advice와 호출하지 않는 Advice의 target 실행 여부와 반환값을 비교한다.
2. 외부 `proxy.inner()`와 `target.outer()` 내부의 `this.inner()` 호출 경로를 프록시·target 참조로 비교한다.
3. self-invocation에서 Advice는 적용되지 않지만 원본 메서드는 실행되는 이유를 설명한다.

## 면접 질문

1. Spring AOP의 self-invocation 문제를 프록시 호출 경계와 실제 객체 참조를 사용해 설명해 주세요.
2. Pointcut이 외부 메서드와 내부 메서드 모두에 일치해도 내부 호출의 Advice가 실행되지 않을 수 있는 이유는 무엇인가요?

## 다음 복습일

2026-08-12
