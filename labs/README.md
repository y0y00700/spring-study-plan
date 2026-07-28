# Labs

Spring 개념을 격리해 검증하는 실험 코드를 둡니다.

책의 목차와 기초 진단 결과를 확인한 뒤 `spring-lab` 프로젝트를 생성합니다. 처음에는 프로젝트를 주제마다 나누지 않고 테스트 패키지로 구분합니다.

## 현재 환경

- Java 17
- Spring Boot 4.1.0
- Gradle Wrapper 9.5.1
- 테스트 실행: `labs/spring-lab`에서 `.\gradlew.bat test`

2026-07-29 기준 전체 테스트 18개가 성공했습니다.

- `SpringLabApplicationTests`: Spring 컨텍스트 로딩
- `SingletonSharedStateTest`: 메서드 인자로 같은 변경 가능한 리스트를 공유할 때 호출 결과가 간섭하는지 검증
- `ConstructorInjectionTest`: 생성자에 전달된 객체 참조 유지와 두 `PaymentProcessor` 구현체 교체 검증
- `SpringBeanSelectionTest`: 동일 타입 Bean 후보 충돌, `@Primary` 기본 선택, `@Qualifier` 명시 선택, `@Lazy` 후보의 생성 지연, `ApplicationContext`와 내부 `BeanFactory`의 동일 Singleton 조회 검증
- `AnnotationReflectionTest`: `RUNTIME`·`CLASS` 보존 정책에 따른 annotation 조회 결과와 `Method.invoke()`의 독립적인 실행 검증
- `SpringConstructorResolutionTest`: 생성자 매개변수 타입 조회와 `PaymentProcessor 생성 → OrderService 생성` 순서 검증
- `BeanLifecycleOrderTest`: 인스턴스화·Aware 콜백·초기화 콜백·BeanPostProcessor의 실행 순서와 후처리기가 반환한 프록시 공개 검증
- `ComponentScanExperimentTest`: scan 범위 안팎과 include·exclude filter에 따른 BeanDefinition 등록 결과, lazy Bean의 조회 전후 생성 횟수 검증
- `ConfigurationProxyExperimentTest`: `proxyBeanMethods` 설정과 `@Bean` 직접 호출·메서드 매개변수 주입에 따른 객체 동일성 및 생성 횟수 검증

```text
spring-lab/src/test/java/com/study/springlab/
├─ container/
├─ mvc/
├─ transaction/
└─ jpa/

spring-lab/src/test/java/study/constructorinjection/
├─ ConstructorInjectionTest.java
└─ SpringBeanSelectionTest.java

spring-lab/src/test/java/study/reflection/
├─ AnnotationReflectionTest.java
└─ SpringConstructorResolutionTest.java

spring-lab/src/test/java/study/lifecycle/
└─ BeanLifecycleOrderTest.java

spring-lab/src/test/java/study/componentscan/
├─ ComponentScanExperimentTest.java
├─ inside/InsideCandidates.java
└─ outside/OutsideCandidates.java

spring-lab/src/test/java/study/configuration/
└─ ConfigurationProxyExperimentTest.java
```

모든 실험에는 다음 내용을 남깁니다.

1. 실행 전 예상
2. 예상의 근거
3. 관찰할 대상
4. 실제 결과를 검증하는 assertion
5. 예상과 실제 결과가 달랐던 이유
