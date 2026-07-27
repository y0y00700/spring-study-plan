# Spring Deep Dive

Java/Spring 실무 경험을 Spring의 내부 원리와 백엔드 설계 역량으로 연결하기 위한 학습 저장소입니다.

## 현재 단계

Spring 학습 준비 및 기초 진단

## 이번 주 목표

- HTTP 요청이 Controller에 도착하는 흐름을 설명한다.
- Servlet, Servlet Container, Spring MVC의 관계를 구분한다.
- 객체 의존성을 직접 조립해 보고 DI가 해결하는 문제를 설명한다.

## 바로가기

- [현재 학습 컨텍스트](CURRENT.md)
- [8주 학습 로드맵](ROADMAP.md)
- [세션별 상세 로드맵](ROADMAP_DETAIL.md)
- [기초 진단](curriculum/00-diagnostic.md)
- [도서 학습 계획](book/spring-textbook/reading-plan.md)
- [해결되지 않은 질문](questions/unresolved.md)
- [확인된 오개념](questions/misconceptions.md)

## 학습 원칙

각 주제는 다음 순서로 학습합니다.

1. 알고 있다고 생각하는 내용을 먼저 설명한다.
2. 코드 실행 결과를 실행 전에 예측한다.
3. 최소 재현 실험을 작성한다.
4. 예상과 실제 결과의 차이를 분석한다.
5. 책이나 코드를 보지 않고 다시 설명한다.
6. 일정 간격을 두고 회상 복습한다.

## 진척도

- [ ] 웹과 Servlet 기반 지식
- [ ] Spring Container와 DI
- [ ] Spring MVC
- [ ] AOP와 트랜잭션
- [ ] JPA와 Hibernate
- [ ] 테스트
- [ ] 운영 환경

## 다른 컴퓨터에서 이어서 학습하기

학습 시작 전에 원격 변경사항을 반영합니다.

```powershell
git pull --rebase
```

Codex에는 다음과 같이 요청합니다.

```text
AGENTS.md, CURRENT.md, 최근 sessions 기록과 이번 주 curriculum 문서를 읽고
지난 학습을 이어서 진행해 줘. 오늘은 진단 질문부터 시작하고 정답을 먼저 말하지 마.
```

학습을 마치면 세션 기록과 `CURRENT.md`를 갱신하고 커밋합니다.
