# 구현 메모

## 2026-06-25

- 로컬 개발 및 테스트 실행을 위해 기본 datasource를 H2 인메모리 DB로 설정했다.
- H2는 MySQL 호환 모드로 실행하며, 콘솔은 `/h2-console` 경로에서 활성화한다.
- JPA 스키마 생성은 임시 로컬 환경 기준으로 `ddl-auto: update`를 사용한다.
- Spring Security 의존성을 추가하고 MVP 초기 단계 기준으로 모든 요청을 허용하도록 설정했다.
- `https://control-front-navy.vercel.app` 출처의 CORS 요청을 허용했다.
- 클라이언트가 simulator 서버에 `POST /v2/simulations/start`를 호출하면 약 5분 동안 5초 간격으로 Mock telemetry를 생성해 backend 서버의 `POST /v1/internal/telemetry`로 전송하도록 구현했다.
- simulator 실행 상태 조회 API로 `GET /v2/simulations/current`를 추가했다.
- simulator는 `buses`, `routes`, `stops`, `route_stops` 기준 데이터를 읽기만 하며, `bus_locations`, `bus_events`에는 직접 접근하지 않는다.
- backend 호출은 Spring `RestClient`를 사용하고, `control-api.api-key`가 설정되어 있으면 `X-API-KEY` 헤더를 포함한다.
