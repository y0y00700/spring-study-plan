# Labs

Spring 개념을 격리해 검증하는 실험 코드를 둡니다.

책의 목차와 기초 진단 결과를 확인한 뒤 `spring-lab` 프로젝트를 생성합니다. 처음에는 프로젝트를 주제마다 나누지 않고 테스트 패키지로 구분합니다.

## 현재 환경

- Java 17
- Spring Boot 4.1.0
- Gradle Wrapper 9.5.1
- 테스트 실행: `labs/spring-lab`에서 `.\gradlew.bat test`

2026-07-24 기준 전체 테스트 2개가 성공했습니다.

- `SpringLabApplicationTests`: Spring 컨텍스트 로딩
- `SingletonSharedStateTest`: 메서드 인자로 같은 변경 가능한 리스트를 공유할 때 호출 결과가 간섭하는지 검증

생성자 주입 구현체 교체와 annotation·Reflection을 직접 검증하는 테스트는 아직 추가되지 않았습니다.

```text
spring-lab/src/test/java/com/study/springlab/
├─ container/
├─ mvc/
├─ transaction/
└─ jpa/
```

모든 실험에는 다음 내용을 남깁니다.

1. 실행 전 예상
2. 예상의 근거
3. 관찰할 대상
4. 실제 결과를 검증하는 assertion
5. 예상과 실제 결과가 달랐던 이유
