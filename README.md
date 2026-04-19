# Chat Service

Spring Boot 기반의 WebSocket 실시간 채팅 서버입니다.  

---

## Tech Stack

- Java 17
- Spring Boot
- Spring WebSocket (STOMP)

---

## Installation

### 요구사항

- Java 17
- Gradle

### 1. 저장소 클론

```bash
git clone https://github.com/sunsik17/ChatService.git
cd ChatService
```

### 2. 빌드

```bash
./gradlew build
```

### 3. 서버 실행

```bash
./gradlew bootRun
```

서버가 정상적으로 기동되면 `http://localhost:8080` 에서 동작합니다.

---

## 테스트 (chat.html)

`chat.html` 파일을 브라우저에서 직접 열면 채팅 UI가 실행됩니다.

1. 위 과정을 통해 **Spring Boot 서버를 먼저 실행**합니다.
2. `chat.html` 파일을 브라우저에서 엽니다.
   * 단순히 웹소켓 엔드포인트에 접근시 잘 연결이 되는지 확인하기 위한 html파일 입니다.
   * 여러 브라우저 탭에서 동시에 열면 다중 유저 환경을 시뮬레이션할 수 있습니다.
3. 왼쪽 사이드바에서 채팅방을 클릭하면 자동으로 WebSocket에 연결됩니다.
4. 우측 상단의 **내 이름** 입력칸에서 발신자 이름을 지정할 수 있습니다.
   * 인증/인가를 구현하지 않아서 해당 방식을 사용합니다.
   * 인증/인가가 적용 되면 발신자를 사용자의 정보를 기반으로 동작하게 할 수 있습니다.

> **참고:** `chat.html`은 테스트 목적으로만 제공됩니다.  
> 실제 프론트엔드가 붙으면 이 파일은 사용하지 않습니다.

---

## WebSocket 연결 정보

| 항목 | 값 |
|---|---|
| WebSocket Endpoint | `ws://localhost:8080/ws` |
| 메시지 발행 prefix | `/pub` |
| 메시지 구독 prefix | `/sub` |

### 채팅방 구독 (Subscribe)

```
/sub/chatrooms/{chatRoomId}
```

해당 채팅방의 메시지를 수신하기 위해 해당 경로를 구독합니다.

### 메시지 발행 (Publish)

```
/pub/chatrooms
```

아래 형식의 JSON을 이 경로로 전송하게 됩니다.

```json
{
  "sender": "홍길동",
  "chatRoomId": "room-1",
  "content": "안녕하세요!"
}
```

---

## 동작 방식

### 전체 흐름

```
클라이언트                        서버 (Spring Boot)
    │                                    │
    │── WebSocket 연결 (/ws) ──────────► │
    │                                    │
    │── STOMP CONNECT ────────────────► │
    │◄─ STOMP CONNECTED ──────────────── │
    │                                    │
    │── SUBSCRIBE /sub/chatrooms/room-1 ►│  (채팅방 구독)
    │                                    │
    │── SEND /pub/chatrooms ──────────► │  (메시지 전송)
    │                        ┌──────────┘
    │                        │ ChatController가 수신
    │                        │ /sub/chatrooms/room-1 으로 브로드캐스트
    │                        └──────────┐
    │◄── MESSAGE /sub/chatrooms/room-1 ─ │  (같은 방 구독자 전원 수신)
```

### 핵심 컴포넌트 설명

**WebSocketConfiguration**

WebSocket 서버의 기본 설정을 담당합니다.

- `/ws` 엔드포인트로 WebSocket 및 SockJS 연결을 모두 허용합니다. SockJS는 WebSocket을 지원하지 않는 환경에서 자동으로 HTTP 롱폴링 등의 방식으로 폴백합니다.
- `/pub`을 애플리케이션 목적지 prefix로 설정합니다. 클라이언트가 `/pub/**` 경로로 메시지를 보내면 Spring이 해당 `@MessageMapping` 메서드로 라우팅합니다.
- `/sub`을 메시지 브로커 prefix로 설정합니다. 클라이언트는 `/sub/**` 경로를 구독하여 메시지를 수신합니다.

**ChatController**

`/pub/chatrooms` 경로로 수신된 메시지를 처리합니다.

- `@MessageMapping("/chatrooms")`는 실제 목적지 `/pub/chatrooms`에 매핑됩니다.
- 수신한 `Message` 객체를 `/sub/chatrooms/{chatRoomId}` 경로로 브로드캐스트합니다.
- 같은 채팅방을 구독한 모든 클라이언트가 동시에 메시지를 수신합니다.

**Message (DTO)**

채팅 메시지의 데이터 구조입니다.

```
sender     - 발신자 이름
chatRoomId - 채팅방 ID
content    - 메시지 내용
```

---

## 향후 개발 방향

현재 구현은 프로토타입 수준이며, 실제 서비스 전에 아래 사항들을 염두하고 있습니다.

### 메시지 브로커 확장

현재는 Spring 내장 Simple Message Broker를 사용합니다. 이는 단일 서버에서만 동작하며, 서버가 재시작되면 메시지가 유실됩니다. 여러 서버 인스턴스를 운영하거나 대용량 트래픽이 예상된다면 **Kafka** 또는 **RabbitMQ** 같은 외부 메시지 브로커로 사용하는 것을 생각하고 있습니다.

### 데이터베이스

메시지를 영속화하기위해 데이터 특성에 맞는 DB를 선택하고자 합니다.

- 텍스트 메시지만 지원하고 관계형 데이터(유저, 채팅방 멤버십 등)가 중요하다면 팀내에서 사용할 **PostgreSQL**을 따라서 사용하려고 합니다.
- 이미지, 파일 등 다양한 형식의 메시지를 지원하거나 메시지 스키마가 자주 변경될 가능성이 있다면 **MongoDB** 같은 Document DB가 유연합니다.

### 인증 / 인가

현재는 인증 없이 누구나 메시지를 보낼 수 있습니다. 실제 서비스에서는 WebSocket 연결 시 JWT 등의 토큰 검증을 추가해야 합니다. Spring Security와 WebSocket을 통합하면 연결 핸드셰이크 단계에서 인증을 처리할 수 있습니다.

### 채팅방 목록 API

현재 채팅방 목록은 `chat.html`에 하드코딩되어 있습니다. 실제 프론트엔드에서는 채팅방 목록 및 채팅방 생성/입장을 위한 REST API를 별도로 구현하고, 프론트엔드가 해당 API를 호출해 동적으로 채팅방을 구성하면 됩니다. WebSocket은 실시간 메시지 송수신에만 사용하는 구조가 일반적입니다.
