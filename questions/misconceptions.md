# 확인된 오개념

틀린 답을 감추지 않고, 처음 생각과 수정된 이해를 함께 기록합니다.

| 날짜 | 처음 생각 | 반례 또는 실험 | 수정된 이해 | 복습일 |
|---|---|---|---|---|
| 2026-07-23 | 사용하는 쪽에 `OrderService`를 주입하면 `OrderService` 내부 의존 객체의 생성 책임도 분리된다고 생각함 | `OrderService` 내부에 `new KakaoPaymentProcessor()`가 그대로 남아 있으면 구현체 변경 시 `OrderService`를 수정해야 함 | 분리하려는 의존 객체인 `PaymentProcessor`를 `OrderService` 생성자 매개변수로 받아야 함 | 2026-07-26 |
| 2026-07-23 | 생성자 주입 문제를 `@Autowired` 필드 주입으로 풀거나, 필드에서 구현체를 만든 뒤 생성자에서 덮어써도 된다고 생각함 | 필드가 구체 구현체를 직접 생성하면 외부에서 의존성을 전달해도 생성 책임과 구체 구현체 의존이 남음 | 구체 구현체 생성은 외부 조립 코드가 담당하고, 사용하는 객체는 생성자로 받은 의존성을 한 번 저장함 | 2026-07-26 |
| 2026-07-23 | 두 구현체 검증 중 카카오 객체를 호출하면서도 `"NAVER"`가 반환될 것으로 예측함 | 생성자에 카카오 객체 참조를 전달한 뒤 원래 지역변수를 네이버 객체로 재대입해도 `OrderService`는 계속 카카오 객체를 호출함 | 변수 재대입은 그 변수의 참조만 바꾸며, 이미 생성자에 전달되어 필드에 저장된 참조까지 바꾸지 않는다. 검증문에서는 실제 객체 연결 관계를 추적해야 함 | 2026-07-26 |
| 2026-07-23 | Reflection으로 annotation 존재를 확인하면 annotation의 동작과 대상 메서드도 함께 실행된다고 생각함 | `isAnnotationPresent()`는 존재 여부만 반환하며 메서드를 호출하지 않음 | annotation은 조사 대상인 메타데이터이고, Reflection은 조사 수단이며, 실제 동작에는 별도의 처리·호출 주체가 필요함 | 2026-07-24 |
| 2026-07-26 | `ApplicationContext.getBean()`은 설정 클래스 분석 결과를 바탕으로 별도 객체를 반환하고, 내부 `BeanFactory.getBean()`은 자신이 만든 다른 객체를 반환한다고 생각함 | 두 경로로 조회한 `OrderService`를 `assertSame`으로 비교하자 같은 Singleton 참조가 반환됨 | 설정 분석 결과는 Bean 정의이며 `getBean()`의 결과는 Bean 객체다. `ApplicationContext.getBean()`은 내부 `BeanFactory`에 조회를 위임하고 같은 Singleton 저장소의 객체를 반환한다. | 2026-07-27 |
| 2026-07-26 | 주입 메서드의 매개변수 타입만 확인하면 동일 타입 후보가 두 개라는 사실까지 알 수 있다고 설명함 | `orderService(PaymentProcessor)`의 매개변수 타입과 `kakaoProcessor()`·`naverProcessor()`의 반환 타입을 분리해 추적함 | 주입 지점의 매개변수 타입은 검색 조건이고, 각 Bean 정의의 타입 정보가 후보를 판별하는 근거다. | 2026-07-27 |
| 2026-07-27 | `method.isAnnotationPresent()`가 클래스에 annotation이 있는지 확인한다고 설명함 | `Method` 객체로 `pay()`의 `@Tracked`를 조회하고 클래스 조회와 구분함 | `method.isAnnotationPresent()`는 해당 `Method`가 나타내는 메서드를 확인한다. 조회 대상은 호출 주체인 `AnnotatedElement`에 따라 달라진다. | 2026-07-30 |
| 2026-07-27 | `OrderService` 생성자가 호출되는 시점에 `PaymentProcessor` 후보 객체를 확인한다고 설명함 | 지연 등록한 `PaymentProcessor`와 `OrderService`의 생성 이벤트를 검증하자 `PaymentProcessor 생성 → OrderService 생성` 순서였음 | BeanFactory는 생성자 호출 전에 매개변수 타입을 확인하고 후보 Bean 정의를 선택한다. 선택된 객체를 생성하거나 조회한 뒤 그 참조를 전달하며 생성자를 호출한다. | 2026-07-30 |
| 2026-07-28 | `setBeanName()`까지 인스턴스화 과정이며, `this`를 사용할 수 있으면 인스턴스화가 끝났다고 생각함 | 생성자 안에서도 `this`를 사용할 수 있고 `setBeanName()`은 생성자 실행 뒤 이미 만들어진 객체에 호출됨 | 인스턴스화는 생성자 호출로 객체를 만드는 단계이며, `setBeanName()`은 이후 컨테이너 정보를 전달하는 Aware 콜백이다. | 2026-07-31 |
| 2026-07-28 | 필드 주입 의존성을 생성자에서 사용할 수 있다고 생각함 | 객체 할당 시 필드 값은 기본적으로 `null`이고 필드 주입은 생성자 실행 뒤 진행됨 | 생성자 실행 시 의존성이 필요하면 생성자 주입으로 먼저 확보된 참조를 인자로 받아야 한다. | 2026-07-31 |
| 2026-07-28 | 명시적인 `@ComponentScan`이 없으면 스캔 여부를 알 수 없다고 생각함 | `@SpringBootApplication`의 합성 annotation 구성을 확인함 | `@SpringBootApplication`은 `@ComponentScan`을 포함하며, 별도 지정이 없으면 애플리케이션 클래스의 패키지와 하위 패키지가 기본 범위다. | 2026-07-31 |
| 2026-07-28 | scan 범위 밖의 `@Component`도 등록되거나 include filter로 포함될 수 있다고 생각함 | base package 밖의 `@Component`와 include 조건 일치 클래스 모두 BeanDefinition으로 등록되지 않음을 검증함 | filter는 base package로 정한 탐색 범위 안에서만 적용되며 include filter도 탐색 범위를 확장하지 않는다. | 2026-07-31 |
| 2026-07-29 | `proxyBeanMethods=false`이면 메서드 매개변수로 의존성을 받아도 별도 객체가 생성되고, 객체 생성과는 관계없는 옵션이라고 생각함 | 직접 `member()`를 호출한 설정과 `team(Member member)`로 주입받은 설정의 동일성·생성 횟수를 비교함 | 이 옵션은 `@Bean` 메서드 간 직접 호출의 프록시 개입 여부를 정한다. 매개변수 의존성은 BeanFactory가 해결하므로 `false`여도 관리 객체가 전달된다. | 2026-08-01 |
| 2026-07-29 | `proxyBeanMethods=false`의 직접 호출에서 `Team` 생성자가 2회 실행된다고 설명함 | 컨테이너의 `Member`와 `team()` 내부 직접 호출로 만든 `Member`를 분리해 생성 횟수를 검증함 | `Member`가 2개 생성되고 `Team`은 1개 생성된다. 어떤 메서드의 `new`가 다시 실행되는지 호출 경로로 추적해야 한다. | 2026-08-01 |
