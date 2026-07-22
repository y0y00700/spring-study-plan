# 확인된 오개념

틀린 답을 감추지 않고, 처음 생각과 수정된 이해를 함께 기록합니다.

| 날짜 | 처음 생각 | 반례 또는 실험 | 수정된 이해 | 복습일 |
|---|---|---|---|---|
| 2026-07-23 | 사용하는 쪽에 `OrderService`를 주입하면 `OrderService` 내부 의존 객체의 생성 책임도 분리된다고 생각함 | `OrderService` 내부에 `new KakaoPaymentProcessor()`가 그대로 남아 있으면 구현체 변경 시 `OrderService`를 수정해야 함 | 분리하려는 의존 객체인 `PaymentProcessor`를 `OrderService` 생성자 매개변수로 받아야 함 | 2026-07-24 |
