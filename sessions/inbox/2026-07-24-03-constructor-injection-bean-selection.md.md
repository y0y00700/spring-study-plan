---
date: 2026-07-24
topic: 생성자 주입과 동일 타입 Bean 선택 규칙
processed: true
---

# 생성자 주입과 Spring Bean 선택 규칙

## 학습 목표

- 생성자에 전달되는 객체 참조의 흐름을 설명한다.
- 인터페이스와 생성자 주입의 역할을 구분한다.
- 동일 타입 Bean이 여러 개일 때 Spring의 후보 선택 과정을 설명한다.
- `@Primary`와 `@Qualifier`의 역할을 구분한다.

## 학습 환경

- `labs/spring-lab`
- Java 17
- Spring Boot 4.1.0
- Gradle Wrapper 9.5.1
- 회사 PC 제한 모드
- Codex 파일 작업에서 `CreateProcessWithLogonW 1385` 권한 오류가 발생했다.

## 튜터링 방식 추가 요청

학습자가 코드나 테스트를 작성해야 할 때는 작성 전에 항상 다음 정보를 먼저 제공한다.

- 기준 디렉터리와 파일 경로
- 생성 또는 수정할 클래스명
- 생성 또는 수정할 메서드명
- 새 파일인지 기존 파일 수정인지
- 프로덕션 코드인지 테스트 코드인지

지난 세션의 실습 코드가 저장소에 남아 있다고 가정하지 않는다. 필요한 코드가 없다면 실험 준비용 구현을 제공하거나, 이전 구현에 의존하지 않는 독립 문제로 구성한다.

## 생성한 프로덕션 코드

경로:

`labs/spring-lab/src/main/java/study/constructorinjection`

클래스:

- `PaymentProcessor`
  - 메서드: `String pay()`
- `KakaoPaymentProcessor`
  - `PaymentProcessor` 구현
  - `pay()`는 `"KAKAO"` 반환
- `NaverPaymentProcessor`
  - `PaymentProcessor` 구현
  - `pay()`는 `"NAVER"` 반환
- `OrderService`
  - 생성자: `OrderService(PaymentProcessor paymentProcessor)`
  - 메서드: `String pay()`

## 생성자 주입 실험

테스트 파일:

`labs/spring-lab/src/test/java/study/constructorinjection/ConstructorInjectionTest.java`

테스트 클래스:

`ConstructorInjectionTest`

### keepsInjectedProcessorWhenLocalVariableIsReassigned

실험 순서:

1. 지역변수 `processor`가 `KakaoPaymentProcessor` 객체를 참조했다.
2. 해당 참조를 `OrderService` 생성자에 전달했다.
3. 이후 지역변수 `processor`를 `NaverPaymentProcessor` 객체로 재대입했다.
4. `orderService.pay()`의 반환값을 검증했다.

예상:

- `OrderService` 필드에는 카카오 객체를 가리키는 참조가 유지된다.
- 반환값은 `"KAKAO"`다.

고의 실패 결과:

```text
Expected :NAVER
Actual   :KAKAO
```

고의 실패를 확인한 뒤 정상 검증문 `assertEquals("KAKAO", orderService.pay())`만 남겼다.

### usesProcessorProvidedAtConstructionTime

- 카카오와 네이버 구현체를 각각 다른 `OrderService` 생성자에 전달했다.
- 각 서비스는 주입받은 구현체에 따라 `"KAKAO"`, `"NAVER"`를 반환했다.

## 동일 타입 Bean 선택 실험

테스트 파일:

`labs/spring-lab/src/test/java/study/constructorinjection/SpringBeanSelectionTest.java`

- 선택 규칙 없이 `PaymentProcessor` Bean이 둘이면 `OrderService` 의존성 연결이 실패했다.
- 실패 원인은 `NoUniqueBeanDefinitionException`이었다.
- 카카오 Bean에 `@Primary`를 지정하면 카카오 구현체가 주입됐다.
- 카카오 Bean이 `@Primary`여도 주입 지점에 `@Qualifier("naverProcessor")`를 지정하면 네이버 구현체가 주입됐다.

## 정리된 이해

- 생성자에 전달되는 것은 객체 자체를 복제한 값이 아니라 객체를 가리키는 참조 값이다.
- 전달 후 원래 지역변수를 재대입해도 이미 필드에 저장된 참조에는 영향을 주지 않는다.
- 인터페이스는 사용할 수 있는 계약을 제한하고, 생성자 주입은 외부에서 선택한 구현체의 참조를 전달한다.
- Spring은 타입으로 후보를 찾은 뒤 선택 규칙을 적용한다.
- `@Primary`는 기본 우선 후보이고, 주입 지점의 `@Qualifier`는 특정 후보를 명시하므로 우선한다.

## 검증

- 2026-07-26 최초 전체 테스트: 7개 실행, 고의 실패 assertion 1개 실패
- 고의 실패 assertion 제거 후 전체 테스트 성공: 7개 실행, 실패 0개
