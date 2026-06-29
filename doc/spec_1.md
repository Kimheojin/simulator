# Simulator 동시 요청 처리 구조

## 요약

현재 simulator 서버는 요청 IP, 클라이언트, 사용자 단위로 시뮬레이션 실행 상태를 분리하지 않는다.

`POST /v2/simulations/start` 요청이 들어오면 서버 인스턴스 전체에서 하나의 실행 상태만 사용한다. 따라서 서로 다른 IP에서 동시에 시작 요청이 들어와도 먼저 처리된 요청 하나만 시뮬레이션을 시작하고, 나머지 요청은 이미 실행 중인 시뮬레이션이 있다는 응답을 받는다.

## 대상 API

- `POST /v2/simulations/start`
  - 시뮬레이션 시작 요청
  - 시작 성공 시 `202 ACCEPTED` 반환
- `GET /v2/simulations/current`
  - 현재 시뮬레이션 실행 상태 조회
  - IP별 상태가 아니라 서버 인스턴스의 단일 실행 상태를 반환

## 동시 시작 요청 처리 방식

`SimulationService`는 내부에 단일 `currentRun` 상태를 가지고 있다.

시작 요청은 `start()` 메서드에서 `synchronized (lock)` 블록으로 보호된다. 이 구조 때문에 여러 HTTP 요청이 동시에 들어와도 `currentRun` 확인과 새 실행 생성 과정은 한 번에 하나의 스레드만 수행한다.

처리 흐름은 다음과 같다.

1. 시작 요청이 `SimulationController`로 들어온다.
2. 컨트롤러가 `SimulationService.start()`를 호출한다.
3. 서비스가 `synchronized (lock)` 안에서 현재 실행 상태를 확인한다.
4. 실행 중인 `currentRun`이 없으면 새 `SimulationRun`을 생성하고 스케줄링한다.
5. 이미 실행 중인 `currentRun`이 있으면 새 실행을 만들지 않고 오류를 반환한다.

즉, 동시에 여러 요청이 들어와도 시작 처리는 직렬화된다.

## 이미 실행 중인 경우 응답

시뮬레이션이 이미 실행 중이면 다음 애플리케이션 오류가 발생한다.

- HTTP 상태: `409 CONFLICT`
- 오류 코드: `SIMULATION_ALREADY_RUNNING`
- 메시지: `이미 실행 중인 시뮬레이션이 있습니다.`

이 응답은 요청 IP가 다른 경우에도 동일하게 적용된다.

## IP별 분리 여부

현재 구현은 요청 IP를 읽거나 저장하지 않는다.

또한 다음 단위의 실행 상태를 별도로 관리하지 않는다.

- 요청 IP
- 클라이언트 애플리케이션
- 사용자
- 세션
- API key

따라서 simulator 서버 프로세스 하나에는 동시에 하나의 시뮬레이션만 실행될 수 있다.

## Telemetry 전송 구조

시뮬레이션이 시작되면 `ThreadPoolTaskScheduler`를 통해 주기 작업이 등록된다.

- 스케줄러 스레드 풀 크기: `2`
- 작업 이름 prefix: `simulation-`
- 주기 작업: 설정된 interval마다 mock telemetry 생성 및 backend 서버로 전송
- 종료 작업: 설정된 duration 이후 현재 실행을 완료 상태로 변경

Telemetry 전송 중 일부 요청이 실패해도 전체 시뮬레이션은 계속 진행된다. 실패 횟수는 실행 상태의 `failureCount`에 누적된다.

## 운영상 의미

현재 구조는 단일 MVP 시뮬레이터 서버에서 하나의 공통 mock telemetry 흐름을 발생시키는 용도에 맞춰져 있다.

동시에 여러 IP나 여러 클라이언트가 독립적인 시뮬레이션을 실행해야 하는 요구사항이 생기면 현재 구조를 변경해야 한다. 예를 들어 IP, clientId, sessionId 같은 실행 식별자를 도입하고, `currentRun`을 단일 값이 아니라 실행 식별자별 상태 저장 구조로 바꿔야 한다.

