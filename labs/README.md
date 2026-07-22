# Labs

Spring 개념을 격리해 검증하는 실험 코드를 둡니다.

책의 목차와 기초 진단 결과를 확인한 뒤 `spring-lab` 프로젝트를 생성합니다. 처음에는 프로젝트를 주제마다 나누지 않고 테스트 패키지로 구분합니다.

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
