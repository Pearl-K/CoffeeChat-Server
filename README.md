# CoffeeChat-Server
## Motivation
- 기존에 오프라인 중심이었던 개발자 커피챗을 온라인에서도 편하게 이어갈 수 있도록 만듭니다.
- 서비스가 제공하는 **핵심 가치** 중 하나는, 실시간 온라인 커피챗을 기술적으로 원활히 제공하는 것입니다.


## Overview & My Role
- 이 레포지토리는 CoffeeChat Service의 서버 중 **실시간 채팅** 파트를 분리하여 재구성한 서버입니다.
- 저는 이 프로젝트에서 아래와 같은 도메인의 설계와 개발을 담당했습니다.

1. 실시간 채팅 서비스
2. User On/Offline Tracking
3. 인기 게시글 / 인기 순위 / 피드 API 및 캐싱 전략

---
## Refactoring Journey (V1 → V3) at a Glance

이 프로젝트는 MVP PoC에 초점을 맞춘 **V1**에서 시작하여 
   → 메시지 신뢰성과 성능을 끌어올린 **V2** 
   → 확장성을 고려한 **V3**로 발전했습니다. 


각 버전의 목표와 주요 변경점은 다음과 같습니다.


| Version | 핵심 목표 (Goal) | 주요 기술/설계 변경점 | 해결하고자 한 문제 |
| :--- | :--- | :--- | :--- |
| **V1** | **최소 기능 구현 (MVP PoC)** | <ul><li>WebSocket + Redis Pub/Sub</li><li>MongoDB</li></ul> | <ul><li>동료들이 직접 기능 검증 및 피드백</li><li> 50명 이상의 동시 접속자 두고 채팅 시연</li></ul> |
| **V2** | **메시지 신뢰성 보장, 성능 개선** | <ul><li>메시지 브로커 교체 (RabbitMQ)</li><li>세션 객체 경량화, GC 튜닝</li></ul> | <ul><li>Redis Pub/Sub의 메시지 유실 가능성 해결</li><li>모놀리틱 환경에서 성능 한계 돌파</li></ul> |
| **V3** | **수평 확장(Scale-out)이 가능한 구조 구현** | <ul><li>세션 공유 저장소 도입 (Redis)</li><li>MQ를 통한 서버간 느슨한 결합</li><li>모니터링 환경 구축 (Prometheus)</li></ul> | <ul><li>상태 의존(Stateful)으로 인한 확장 불가 문제 해결</li><li>트래픽 급증에 유연하게 대응</li></ul> |



---
## Architecture Evolution
- 시스템 구조도 (버전 별로 넣고 설명하기)


---
## Challenges & Solutions
- 버전별 챌린지와 솔루션 언급


---
## Technical Decisions & Trade-offs
- RabbitMQ, Kafka 비교 (메시지 브로커 비교)



---
## Retrospective
- 프로젝트 회고
