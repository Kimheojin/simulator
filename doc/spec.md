# 구현 메모

## 2026-06-25

- 로컬 개발 및 테스트 실행을 위해 기본 datasource를 H2 인메모리 DB로 설정했다.
- H2는 MySQL 호환 모드로 실행하며, 콘솔은 `/h2-console` 경로에서 활성화한다.
- JPA 스키마 생성은 임시 로컬 환경 기준으로 `ddl-auto: update`를 사용한다.
- Spring Security 의존성을 추가하고 MVP 초기 단계 기준으로 모든 요청을 허용하도록 설정했다.
- `https://control-front-navy.vercel.app` 출처의 CORS 요청을 허용했다.
