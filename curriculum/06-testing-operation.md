# 7~8주차: 테스트와 운영

## 핵심 질문

- 순수 단위 테스트와 Spring 통합 테스트의 책임은 어떻게 다른가?
- 테스트가 통과해도 운영 DB에서 실패할 수 있는 이유는 무엇인가?
- readiness와 liveness는 어떤 실패를 구분하는가?
- 애플리케이션의 문제를 로그, 메트릭, 트레이스로 어떻게 좁혀 가는가?

## 최소 실험

1. 같은 기능을 순수 단위 테스트와 `@SpringBootTest`로 각각 검증한다.
2. Testcontainers로 실제 RDB 통합 테스트를 실행한다.
3. Actuator health와 metric endpoint를 관찰한다.
4. 종료 신호를 보내 graceful shutdown 동작을 확인한다.

## 완료 증거

- 테스트 종류 선택 근거
- CI에서 재현 가능한 테스트
- 장애 가설과 관찰 지표를 연결한 기록
