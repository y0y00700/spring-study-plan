# 2026-07-29: `@Configuration`과 `@Bean` 프록시

## 학습 전 내 생각

- `proxyBeanMethods`의 기능과 프록시의 의미를 알지 못했다.
- 설정 클래스 프록시가 있으면 `@Bean` 메서드 직접 호출에 Spring이 개입한다고 예상했다.
- `proxyBeanMethods=false`에서는 메서드 매개변수로 의존성을 받아도 별도 객체가 생성될 것으로 예상했다.

## 튜터의 질문

1. `proxyBeanMethods=true`와 `false`에서 `@Bean` 메서드 간 직접 호출의 객체 동일성과 생성 횟수는 어떻게 다른가?
2. 프록시는 어떤 객체이며 설정 클래스의 메서드 호출을 어떻게 가로채는가?
3. `proxyBeanMethods=false`에서 `@Bean` 메서드 매개변수 주입을 사용하면 결과가 어떻게 달라지는가?
4. 설정 클래스 프록시가 필요한 경우와 불필요한 경우는 언제인가?

## 실행 결과 예측

- 직접 호출에서 `proxyBeanMethods=true`이면 `Member`가 1개, `false`이면 2개 생성될 것으로 예측했다.
- 처음 assertion 작성에서는 `assertSame`과 `assertNotSame`을 반대로 적었지만 호출 흐름을 다시 확인해 수정했다.
- `proxyBeanMethods=false`의 메서드 매개변수 주입에서도 처음에는 서로 다른 `Member` 2개를 예상했으나 BeanFactory의 의존성 해결 설명 후 같은 객체 1개로 예측을 수정했다.

## 예상의 근거

- 설정 클래스 프록시가 `@Bean` 메서드 간 직접 호출을 가로채면 BeanFactory의 관리 객체를 반환한다고 판단했다.
- 프록시가 없으면 직접 호출은 일반 Java 호출이므로 메서드 본문의 `new`가 다시 실행된다고 판단했다.
- 메서드 매개변수는 BeanFactory가 후보 BeanDefinition을 검색하고 관리 객체를 전달하는 의존성 주입 지점이라고 판단했다.

## 실험

- `ProxiedConfig`: `proxyBeanMethods=true`에서 `team()`이 `member()`를 직접 호출했다.
- `PlainConfig`: `proxyBeanMethods=false`에서 같은 직접 호출을 실행했다.
- `ParameterConfig`: `proxyBeanMethods=false`에서 `team(Member member)`로 의존성을 전달받았다.
- 각 컨텍스트에서 조회한 `Member`와 `Team` 내부 `Member`의 동일성 및 `Member` 생성자 호출 횟수를 assertion으로 검증했다.

## 실제 결과

- `ProxiedConfig`: 동일 객체, `Member` 생성 1회.
- `PlainConfig`: 서로 다른 객체, `Member` 생성 2회.
- `ParameterConfig`: 동일 객체, `Member` 생성 1회.
- 2026-07-29 전체 Gradle 테스트 18개 성공, 실패·오류 0개.

## 예상과 달랐던 부분

- `proxyBeanMethods=false`여도 `@Bean` 분석, BeanDefinition 등록, BeanFactory의 의존성 검색과 객체 관리는 계속 동작한다.
- 설정 클래스 프록시는 직접 호출을 막는 것이 아니라 가로채서 BeanFactory의 관리 객체를 반환한다.
- 직접 호출 반례에서 `Team`이 2개가 아니라 `Member`가 2개 생성되고 `Team`은 1개 생성된다.

## 내가 다시 설명하기

- `proxyBeanMethods=true`이면 설정 클래스 프록시가 `@Bean` 메서드 간 직접 호출을 가로채 BeanFactory의 관리 객체를 반환한다.
- `proxyBeanMethods=false`이면 직접 호출은 일반 Java 호출이므로 대상 `@Bean` 메서드 본문의 객체 생성 코드가 다시 실행될 수 있다.
- `team(Member member)`처럼 메서드 매개변수로 의존성을 받으면 BeanFactory가 관리 객체를 찾아 전달하므로 설정 클래스 프록시가 없어도 된다.
- 프록시가 불필요한 조건은 설정 클래스 안에서 다른 `@Bean` 메서드를 직접 호출하지 않거나, 의존성을 메서드 매개변수로 전달받는 경우다.

## 남은 질문

- 새로 추가된 질문 없음.

## 회상 문제

1. `proxyBeanMethods=true`에서 `team()` 내부의 `member()` 호출은 어떤 경로로 처리되는가?
2. `proxyBeanMethods=false`의 직접 호출에서는 어떤 객체가 몇 개 생성되는가?
3. 메서드 매개변수 주입에서는 프록시 없이도 왜 관리 객체가 전달되는가?

## 면접 질문

1. `proxyBeanMethods=false`를 안전하게 사용할 수 있는 설정 클래스의 조건을 설명해보세요.
2. Singleton Bean이 의도치 않게 두 객체로 사용될 때 설정 클래스에서 어떤 코드를 우선 확인해야 하나요?

## 다음 복습일

2026-08-01
