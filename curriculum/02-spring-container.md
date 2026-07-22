# 2주차: Spring Container와 DI

## 핵심 질문

- DI가 없다면 객체 생성과 테스트가 왜 어려워지는가?
- ApplicationContext는 객체를 어떻게 등록하고 찾는가?
- BeanDefinition은 실제 객체와 어떻게 다른가?
- `@Configuration` 클래스에 프록시가 필요한 이유는 무엇인가?

## 최소 실험

1. 순수 Java로 객체 의존성을 직접 조립한다.
2. 작은 DI Container를 직접 구현한다.
3. Spring Container로 교체해 Bean 동일성을 비교한다.
4. Bean 생성자, `@PostConstruct`, 소멸 콜백 순서를 확인한다.

## 완료 증거

- 객체 생성 책임을 외부로 옮긴 전후 코드 비교
- Singleton Bean 상태 공유 문제 재현
- `@Configuration` 사용 여부에 따른 결과 비교
