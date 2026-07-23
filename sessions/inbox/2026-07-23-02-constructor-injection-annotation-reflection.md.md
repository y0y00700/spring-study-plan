---
processed: true
date: 2026-07-23
topic: 생성자 주입, annotation, Reflection
---

# 생성자 주입과 annotation·Reflection 진단

## 학습 전 내 생각

- 구현체에 의존하지 않으면 `OrderService`를 수정하지 않아도 된다고 생각했다.
- annotation은 직접 코드를 실행하지 않는다고 생각했다.
- annotation 정보는 애플리케이션 빌드 시 읽는다고 추측했다.
- Reflection의 역할은 알지 못했다.

## 학습한 내용

### 생성자 주입

- 인터페이스 타입으로 필드를 선언하는 것만으로 생성 책임이 분리되지는 않는다.
- 사용하는 클래스 내부에서 구체 구현체를 `new`하면 여전히 구현체에 의존한다.
- `OrderService`는 `PaymentProcessor` 타입만 알고, 구체 구현체의 생성은 외부 조립 코드가 담당해야 한다.
- 생성자에서 외부 객체를 전달받아 필드에 저장한다.
- 구현체 교체 시 외부 조립 코드만 변경되고 `OrderService`는 변경되지 않는다.

### Annotation

- annotation은 메타데이터이며 실행 주체가 아니다.
- annotation 자체는 추가 동작을 실행하지 않는다.
- 컴파일러, annotation processor 또는 Spring 같은 별도의 주체가 읽고 해석해야 동작이 발생한다.
- annotation을 읽는 시점은 항상 빌드 시점인 것이 아니다.
- Spring의 여러 annotation은 애플리케이션 실행 중에 조사될 수 있다.

### Reflection

- Reflection은 실행 중에 클래스 구조를 조사할 수 있는 Java 기능이다.
- 클래스, 필드, 생성자, 생성자 매개변수, 메서드, 반환 타입, annotation 정보를 조사할 수 있다.
- `isAnnotationPresent()`는 annotation 존재 여부만 `true` 또는 `false`로 반환한다.
- annotation 존재 여부를 확인한다고 대상 메서드가 자동으로 실행되지는 않는다.
- Spring은 조사한 정보를 객체 생성, 생성자 선택, 의존성 연결, annotation 기반 처리 등에 활용할 수 있다.
- Reflection은 직접 호출보다 추가 비용이 발생할 수 있고 실행 흐름을 추적하기 어려울 수 있다.

## 실행 결과 예측

- `KakaoPaymentProcessor`를 주입한 `OrderService.order()`는 `"KAKAO"`를 반환한다.
- `NaverPaymentProcessor`를 주입한 `OrderService.order()`는 `"NAVER"`를 반환한다.
- 수정된 두 검증문은 모두 통과하고 정상 종료할 것으로 예측했다.
- `@MyAnnotation`을 아무도 읽고 해석하지 않으면 추가 동작은 발생하지 않는다.
- `isAnnotationPresent(MyAnnotation.class)`는 annotation이 있으면 `true`, 없으면 `false`를 반환한다.

## 실험

- 생성자 주입과 구현체 교체를 확인하는 최소 코드를 작성했다.
- 출력 로그 대신 반환값과 `AssertionError`를 사용하도록 검증문을 작성했다.
- `labs/spring-lab`의 원본 소스와 Gradle 설정이 현재 브랜치의 커밋 `0df1b16`에서 삭제된 것을 확인했다.
- 이전 커밋 `ac0449c`에는 실습 환경 원본이 존재한다.

## 실제 결과

- 학습 당시에는 테스트 환경이 삭제되어 생성자 주입과 Reflection 코드를 실행하지 못했다.
- 2026-07-24 저장소 업데이트로 `labs/spring-lab` 실행 환경을 복구했다.
- `.\gradlew.bat test` 실행 결과 기존 테스트 2개가 모두 성공했다.
- 현재 테스트에는 생성자 주입 구현체 교체와 annotation·Reflection 검증이 없으므로 해당 개념의 실행 검증은 여전히 보류 상태다.

## 발견한 오개념과 실수

1. 인터페이스 타입을 사용하면서 내부에서 구현체를 직접 생성해도 구현체 의존이 제거된다고 혼동했다.
2. 생성자 주입 문제에서 `@Autowired` 필드 주입을 사용하려 했다.
3. 필드 초기화로 `KakaoPaymentProcessor`를 생성한 뒤 생성자에서 다시 덮어쓰려 했다.
4. 네이버 검증에서 `orderService2`가 아니라 카카오 객체인 `orderService`를 호출하고도 `"NAVER"`가 반환될 것으로 예측했다.
5. Reflection으로 annotation 존재 여부를 확인하면 annotation 동작과 대상 메서드가 함께 실행된다고 생각했다.
6. Reflection 자체와 Reflection으로 조사하는 메타데이터를 명확히 구분하지 못했다.

## 내가 다시 설명하기

- `OrderService`는 `PaymentProcessor` 인터페이스에만 의존하고, 구체 구현체의 생성 책임은 외부 조립 코드에 있으므로 구현체가 바뀌어도 수정할 필요가 없다.
- Annotation은 메타데이터이며 실행 주체가 아니다. 별도의 처리 주체가 읽고 해석해야 실제 동작이 발생한다.
- Reflection은 실행 중에 클래스 구조와 annotation을 조사하는 기능이다. 조사만으로 대상 메서드가 실행되지는 않는다.
- Spring은 Reflection으로 생성자와 매개변수 정보를 조사하고, 적절한 의존 객체를 전달하여 객체를 생성할 수 있다.

## 해결되지 않은 질문

- 복구된 실습 환경에서 생성자 주입 검증 코드가 예상대로 통과하는가?
- Spring이 Reflection으로 수집한 정보와 컨테이너의 객체 생성 로직은 구체적으로 어떻게 연결되는가?

## 다음 행동

1. 생성자 주입 검증 코드를 작성하고 두 구현체의 반환값을 확인한다.
2. 의도적으로 잘못된 기대값을 넣어 검증문이 실제로 실패하는지도 확인한다.
3. annotation 존재 확인과 대상 메서드 호출을 분리한 Reflection 테스트를 작성한다.
4. 실험 결과를 다시 설명한 뒤 웹 진단 1번으로 진행한다.

## 회상 문제

1. 인터페이스 타입을 사용해도 내부에서 구현체를 직접 생성하면 생성 책임이 분리되지 않는 이유는 무엇인가?
2. Annotation 자체가 코드를 실행하지 않는 이유는 무엇인가?
3. `isAnnotationPresent()`가 `true`를 반환할 때 자동으로 실행되는 것과 실행되지 않는 것은 무엇인가?

## 면접 질문

1. 생성자 주입이 구현체 교체와 테스트에 어떤 도움을 주는지 설명해 보세요.
2. Spring에서 Reflection이 필요한 이유와 단점을 설명해 보세요.

## 다음 복습일

2026-07-24
