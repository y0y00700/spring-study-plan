---
date: 2026-07-28
environment: company_pc
roadmap_item: CON-07 Component Scan
completion_status: completed
recommended_next: CON-08 @Configuration과 @Bean 프록시
processed: true
---

# CON-07 Component Scan 회사 PC 세션

## 문서 확인

- `AGENTS.md`: 대화에 제공된 내용 확인
- `CURRENT.md`: 대화에 제공된 2026-07-28 내용 확인
- `ROADMAP_DETAIL.md`: 사용자가 제공한 CON-06, CON-07 항목 확인
- 전체 ROADMAP 첨부 파일은 회사 PC 도구 제한으로 직접 읽지 못함
- CURRENT의 다음 항목과 제공된 ROADMAP 항목이 모두 `CON-07 Component Scan`으로 일치함

## 선수 항목 확인

- 선수 항목: `CON-06 Bean 초기화와 BeanPostProcessor`
- CURRENT상 완료 상태 확인
- 세션 시작 시 다음 내용을 회상함:
  - 인스턴스화는 생성자 호출로 객체를 만드는 단계다.
  - `setBeanName()`은 이미 생성된 객체에 컨테이너 정보를 전달하는 Aware 콜백이다.
  - 필드 주입은 생성자 실행 후 진행된다.
  - `@PostConstruct`는 의존관계 설정 후 Spring이 호출하는 초기화 콜백이다.
  - `BeanPostProcessor`는 초기화 전후의 컨테이너 확장 지점이다.
  - 후처리기가 프록시를 반환하면 컨테이너는 프록시를 공개한다.
  - 프록시는 기존 원본을 감싸므로 원본의 생성자와 `@PostConstruct`가 다시 실행되지 않는다.

## 세션 핵심 개념

1. Component Scan의 탐색 범위
2. 클래스 메타데이터 탐색과 BeanDefinition 등록

## 최소 실험

scan 범위 안팎의 클래스와 include·exclude filter 적용 결과를 비교했다.

### 테스트 파일

- `labs/spring-lab/src/test/java/study/componentscan/ComponentScanExperimentTest.java`
- `labs/spring-lab/src/test/java/study/componentscan/inside/InsideCandidates.java`
- `labs/spring-lab/src/test/java/study/componentscan/outside/OutsideCandidates.java`

### 테스트 설정

- base package: `study.componentscan.inside`
- `lazyInit = true`
- 기본 `@Component` 탐색 사용
- `@IncludedInScan`을 include filter로 사용
- `@ExcludedFromScan`을 exclude filter로 사용
- 각 후보 생성자에서 생성 횟수를 기록

### 예측 및 관찰 결과

| 대상 | 위치 및 조건 | BeanDefinition | getBean 전 생성 횟수 |
|---|---|---:|---:|
| `orderService` | 범위 안, `@Component` | 있음 | 0 |
| `specialService` | 범위 안, include filter 일치 | 있음 | 0 |
| `legacyService` | 범위 안, exclude filter 일치 | 없음 | 0 |
| `externalService` | 범위 밖, `@Component` | 없음 | 0 |
| `externalSpecialService` | 범위 밖, include 조건 일치 | 없음 | 0 |

`getBean("orderService")` 호출 후 결과:

- `orderService` 생성 횟수: 1
- `specialService` 생성 횟수: 0
- 나머지 후보 생성 횟수: 0

### 테스트 결과

- 사용자 IntelliJ 실행 결과: 성공
- 회사 PC 제한으로 튜터가 직접 재실행하지는 못함
- 집 PC에서 같은 테스트를 다시 실행해 성공을 확인해야 함

## 반례 예측

`lazyInit = true`를 제거하면:

- BeanDefinition 등록 결과는 변하지 않는다.
- 기본 singleton인 `orderService`와 `specialService`는 컨텍스트 초기화 중 생성되어 생성 횟수가 각각 1이 된다.
- exclude된 클래스와 scan 범위 밖의 클래스는 계속 생성되지 않는다.

이를 통해 `lazyInit`은 Bean 등록 여부가 아니라 객체 생성 시점을 변경한다는 것을 설명했다.

## 학습자가 설명할 수 있게 된 것

- `@SpringBootApplication`은 합성 annotation이며 `@ComponentScan`을 포함한다.
- base package를 별도로 지정하지 않으면 애플리케이션 클래스가 속한 패키지와 그 하위 패키지를 탐색한다.
- `@Component`는 스스로 Bean을 등록하지 않는 메타데이터다.
- 스캐너가 탐색 범위 안의 클래스 메타데이터를 읽고 후보를 판단해야 BeanDefinition이 등록된다.
- 먼저 base package로 탐색 범위를 결정하고, 그 안의 클래스에 기본/include/exclude filter를 적용한다.
- include filter는 base package 밖으로 탐색 범위를 확장하지 않는다.
- exclude filter와 일치하는 클래스는 Bean 후보에서 제외된다.
- 스캔 과정에서는 후보 객체를 먼저 만들지 않고 BeanDefinition을 등록한다.
- BeanDefinition 등록과 Bean 인스턴스 생성은 별개의 단계다.
- `lazyInit`은 등록 여부가 아니라 생성 시점을 변경한다.
- 기본 non-lazy singleton은 컨텍스트 초기화 중 생성되며, lazy Bean은 직접 조회하거나 다른 Bean의 의존성으로 필요할 때 생성된다.

## 발견하고 수정한 오개념

### 1. setBeanName까지 인스턴스화라는 생각

수정:

- 인스턴스화는 생성자 실행으로 끝난다.
- `setBeanName()`은 이미 만들어진 객체에 Bean 이름을 전달하는 Aware 콜백이다.

### 2. this를 사용할 수 있으면 인스턴스화가 완료됐다는 생각

수정:

- 생성자 안에서도 `this`를 사용할 수 있으므로 `this` 존재만으로 생성 완료를 증명할 수 없다.
- Spring이 `setBeanName()`을 호출한다는 것은 생성자 실행을 마친 객체 참조를 확보했다는 뜻이다.

### 3. 필드 주입 의존성을 생성자에서 사용할 수 있다는 생각

수정:

- 필드 공간은 객체 할당 시 존재하지만 참조 값은 기본적으로 `null`이다.
- 필드 주입은 객체 생성 후 진행된다.
- 생성자 주입은 Spring이 의존성을 먼저 확보해 생성자 인자로 전달한다.

### 4. 명시적인 ComponentScan이 없으므로 스캔 여부를 알 수 없다는 생각

수정:

- `@SpringBootApplication`이 `@ComponentScan`을 포함한다.
- 명시적인 base package가 없다면 애플리케이션 클래스의 패키지가 기본 탐색 기준이다.

### 5. scan 범위 밖의 @Component도 등록될 수 있다는 생각

수정:

- `@Component`는 스캐너가 탐색하고 해석해야 동작하는 메타데이터다.
- scan 범위 밖의 클래스는 filter 적용 대상에도 들어오지 않는다.
- include filter도 탐색 범위를 확장하지 않는다.

## 완료 기준에 대한 학습자 재설명

Spring은 base package 자체와 하위 패키지의 클래스 메타데이터를 조사한다. 기본 filter에서는 `@Component` 또는 이를 포함한 합성 annotation이 있는 클래스를 후보로 판단하며, include filter 조건과 일치하는 클래스도 포함한다. exclude filter와 일치하거나 탐색 범위 밖에 있는 클래스는 제외한다. 후보 클래스는 객체 생성 전에 BeanDefinition으로 등록된다. 객체 생성 시점은 lazy 설정과 실제 의존성 필요 시점에 따라 달라질 수 있다.

## 회상 문제 결과

1. scan 범위 밖의 `@Component`가 등록되지 않는 이유
   - base package 밖은 Component Scan의 탐색 대상이 아니기 때문이라고 설명함.

2. BeanDefinition 등록과 인스턴스 생성의 차이
   - BeanDefinition은 Bean 생성과 관리에 필요한 정의이고, 인스턴스화는 생성자를 호출해 실제 객체를 만드는 단계로 보완함.

3. lazy 설정 제거 시 결과
   - 등록 결과는 그대로이고 생성 시점만 바뀐다고 정확히 설명함.

## 면접 질문 결과

1. `@SpringBootApplication`만으로 Component Scan이 동작하는 이유
   - `@SpringBootApplication`이 `@ComponentScan`을 포함하며 기본적으로 자신의 패키지와 하위 패키지를 탐색한다고 설명함.

2. `@Component`가 등록되지 않을 때의 진단 순서
   - base package와 exclude filter를 우선 확인한다고 답함.
   - 다음 순서로 보완함:
     1. 실제 Component Scan 설정과 base package 확인
     2. 클래스가 기본 filter 또는 include filter 조건과 일치하는지 확인
     3. exclude filter와 일치하는지 확인
     4. BeanDefinition 등록 여부와 컨텍스트 시작 실패 여부 확인

## 해결되지 않은 항목 및 후속 후보

- 여러 BeanPostProcessor의 정렬 순서에 따라 `@PostConstruct` 전후의 관찰 순서가 어떻게 달라지는지는 이번 실험에서 검증하지 않음.
- 전체 ROADMAP_DETAIL.md를 직접 읽지 못했으므로 CON-07 이후의 정확한 roadmap item은 집 PC에서 확인해야 함.
- 위 항목을 현재 세션에 추가하지 말고 ROADMAP 순서와 기존 unresolved 문서를 확인한 뒤 처리할 것.

## 집 PC 처리 항목

1. 위 세 개의 Component Scan 테스트 파일이 존재하는지 확인한다.
2. `ComponentScanExperimentTest`를 실행한다.
3. 테스트 성공과 CON-07 완료 기준을 확인한다.
4. 확인 후 `CURRENT.md`에 CON-07 완료를 반영한다.
5. 다음 진행 항목은 `ROADMAP_DETAIL.md`의 CON-07 바로 다음 항목으로 확정한다.
6. 위 오개념을 `questions/misconceptions.md`에 반영한다.
7. 필요한 미해결 항목을 `questions/unresolved.md`에 반영한다.
8. 실습 환경 설명이 달라졌다면 `labs/README.md`를 갱신한다.
9. 모든 반영과 테스트 성공 후에만 이 파일의 `processed`를 `true`로 변경한다.
10. commit과 push는 사용자 요청 전까지 수행하지 않는다.

## 집 PC 검증 결과

- 검증일: 2026-07-28
- `ROADMAP_DETAIL.md` 확인 결과 다음 항목: `CON-08 @Configuration과 @Bean 프록시`
- 대상 테스트: `.\gradlew.bat test --tests study.componentscan.ComponentScanExperimentTest` → `BUILD SUCCESSFUL`
- 전체 테스트: `.\gradlew.bat test` → 15개 실행, 실패·오류 0개
- CON-07 완료 기준과 학습자 재설명 일치 확인
- `CURRENT.md`, `questions/misconceptions.md`, `questions/unresolved.md`, `labs/README.md` 반영 완료
- 전체 테스트 성공 확인 후 `processed: true`로 변경

## 참고 자료

- Spring Boot 4.1 `SpringBootApplication` API:
  https://docs.spring.io/spring-boot/api/java/org/springframework/boot/autoconfigure/SpringBootApplication.html
- Spring Framework Classpath Scanning:
  https://docs.spring.io/spring-framework/reference/core/beans/classpath-scanning.html
