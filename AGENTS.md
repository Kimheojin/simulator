## 프로젝트 컨텍스트

- 프로젝트: simulator
- 기본 패키지: heojin.simulator
- 런타임: Java 21
- 프레임워크: Spring Boot 4.1.0
- 빌드 도구: Gradle
- 주요 의존성: Spring Web MVC, Spring Data JPA, Lombok, JUnit Platform


이 저장소는 서울 버스 관제 시스템 MVP의 백엔드 서버로 더미 데이터를 전송하는 서버

## 참고 문서

- `doc/overview.md`: 프로젝트 목표, MVP 범위, 공통 정책
- `doc/backend.md`: 백엔드 도메인, 영속성, 서비스 책임
- `doc/erd.md`: ERD, 테이블 관계, FK/인덱스, 영속성 설계 원칙
- `doc/endpoint.md`: REST API 엔드포인트, 요청/응답 형태, 오류 응답
- `doc/front.md`: 프론트엔드 연동 기대사항
- `doc/simul.md`: 버스 시뮬레이터 연동 기대사항
- `doc/spec.md`: 구현 메모, 완료된 작업, 중요한 변경 이력

상세 API 예시나 제품 명세를 이 파일에 중복해서 적지 않는다. 대신 위의 원본 문서를 수정

## 패키지 구조

애플리케이션 코드는 도메인 우선으로 구성한다. 각 도메인은 자신의 controller, service, repository, DTO, entity, mapper 패키지를 소유

권장 구조:

```text
src/main/java/heojin/simulator
├── bus
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   ├── entity
│   └── mapper
├── route
├── stop
├── telemetry
└── global
    ├── config
    ├── exception
    ├── response
    └── security
```

테스트도 `src/test/java/heojin/control_api` 아래에서 같은 구조를 사용

## 계층 책임

- `controller`: HTTP 요청/응답을 처리하고, 요청 DTO를 검증하며, 비즈니스 작업을 서비스에 위임한다.
- `service`: 유스케이스와 비즈니스 규칙을 구현하고, 트랜잭션 경계를 관리하며, repository나 외부 호출을 조율한다.
- `repository`: 데이터 접근만 담당한다. 비즈니스 규칙을 두지 않는다.
- `dto`: API 요청/응답 객체를 정의한다. JPA 엔티티를 API로 직접 노출하지 않는다.
- `entity`: 도메인 상태와 의미 있는 도메인 행위를 표현한다. setter 중심 설계보다 의도가 드러나는 상태 변경 메서드를 선호한다.
- `mapper`: 엔티티와 DTO를 변환한다. 복잡한 비즈니스 규칙을 포함하지 않는다.
- `global`: 공통 설정, 예외 처리, 응답 헬퍼, 보안 관련 요소를 둔다.

도메인 간 직접 결합을 최소화한다. 도메인 간 협력이 필요하면 명시적인 서비스 메서드나 이벤트 스타일 연동을 선호한다.

## API 및 영속성 규칙

- 공개 API 엔드포인트는 `/v2` 사용
- 컨트롤러는 엔티티가 아니라 응답 DTO를 반환해야 함
- 요청 검증은 컨트롤러/요청 DTO 경계에서 처리
- API 응답 형태나 오류 코드가 변경되면 `doc/endpoint.md`에 반영해야 한다.
- 애플리케이션 오류를 추가할 때는 `global.exception` 아래의 공통 예외 처리 방식을 사용한다.
- 현재 프로젝트는 로컬 개발 및 테스트용 임시 H2 datasource 설정을 사용. 스키마 작업은 이후 MySQL 마이그레이션과 호환되게 유지
- 엔티티 관계, cascade, fetch 전략 변경은 영속성 및 직렬화 부작용을 검토해야 한다.
- DB에는 FK를 두되 JPA 엔티티 객체 연관관계는 기본적으로 사용하지 않고 FK ID 컬럼으로 참조

## 테스트 규칙

- 비즈니스 로직이 변경되면 테스트를 추가하거나 수정
- 서비스 테스트는 핵심 성공 경로와 중요한 실패 경로를 포함
- 컨트롤러 테스트는 요청 검증, HTTP 상태 코드, 응답 형태를 검증
- 복잡한 조회 조건이나 커스텀 쿼리를 추가할 때는 repository 테스트가 필요
- 버그 수정은 실용적으로 가능하면 수정 전에 실패하는 재현 테스트를 포함
- 구현 세부사항 검증보다 비즈니스 결과 검증을 선호

권장 이름:

- Service: `{Domain}ServiceTest`
- Controller: `{Domain}ControllerTest`
- Repository: `{Domain}RepositoryTest`

## 검증 명령

- 테스트 실행: `./gradlew test`
- 빌드: `./gradlew build`
- 로컬 애플리케이션 실행: `./gradlew bootRun`

가장 좁고 유용한 검증을 먼저 실행한 뒤, 변경이 공통 동작이나 공개 API 계약에 영향을 주면 더 넓게 검증

## 문서화 규칙

- 주요 구현 결정과 완료된 작업은 `doc/spec.md`에 기록
    - 앞선 작업에 중복 내용 혹은 수정사항이 있으면 수정
- `doc/spec.md`는 반드시 한국어로 작성
- 이 파일은 프로젝트 전반의 에이전트 지침, 구조, 라이브러리, 검증 명령이 변경될 때만 업데이트

## 변경 규율

- 새 패턴을 도입하기 전에 기존 이름 규칙과 스타일을 따름
- 변경은 요청된 동작 범위로 제한
- 요청된 변경에 꼭 필요하지 않다면 광범위한 리팩터링을 하지 않음
- 새 라이브러리를 추가할 때는 목적, 버전, 사용 위치를 문서화
- 명시적으로 교체를 요청받지 않은 이상 작업 트리의 사용자 또는 팀원 변경을 보존
