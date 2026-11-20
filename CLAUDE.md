# 프로젝트 개요
- 스택: Java 17, Spring Boot 4.1.0, Spring Data JPA, MySQL(운영) / H2(로컬·테스트), Lombok, springdoc(Swagger), Gradle
- 구조: `org.sopt.sopkathon` 아래 `domain`(도메인별 패키지) + `global`
  - `domain/<도메인>`은 `controller` / `service` / `repository` / `entity` / `dto` / `code`(도메인 에러·성공 코드)로 구분
  - `global`은 `common`(`code`, `entity`) / `config` / `exception` / `response`

# 명령어
- 빌드: `./gradlew build`
- 테스트: `./gradlew test` (`test` 프로필, H2 인메모리)
- 로컬 실행: `./gradlew bootRun` (기본 `local` 프로필, H2 인메모리라 DB 설치 불필요)
  - 8080 포트가 사용 중이면 `./gradlew bootRun --args='--server.port=8081'`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

# 규칙
- 브랜치: `타입/#이슈번호/작업내용` (예: `feat/#1/common-response`), 작업내용은 영어 kebab-case, `main`에서 분기, `develop` 없음
- 커밋/PR 제목: `타입: 작업 내용` (feat, fix, refactor, docs, test, build, chore)
- 이슈 제목: `[타입] 작업 내용` (예: `[FEAT] 공통 응답 형식 및 전역 예외처리 추가`)
- 응답은 `global/response`의 `BaseResponse`, 예외는 `global/exception`의 `BaseException`으로 처리
  - 도메인 에러 코드는 `domain/<도메인>/code/XxxErrorCode` enum으로 `ErrorCode`를 구현
  - 코드 형식은 `도메인약어-E번호` / `도메인약어-S번호` (예: `PST-E001`), 약어는 도메인끼리 겹치지 않게
  - 200이 아닌 응답(201 등)은 `ResponseEntity`로 HTTP 상태를 지정
- 생성/수정 시각이 필요한 엔티티는 `BaseEntity` 상속 (`@SuperBuilder` 사용)
- 작업 후 `./gradlew test` 통과 확인

# 개발 원칙
## 성능
- 연관관계는 `@ManyToOne(fetch = LAZY)`로 선언, 목록 조회 시 N+1 여부 확인 (필요하면 fetch join / `@EntityGraph`)
- 조회 전용 서비스 메서드는 `@Transactional(readOnly = true)`
- 반복문 안에서 쿼리 호출 금지, 필요한 데이터는 한 번에 조회
- 자주 조회하는 조건 컬럼(FK, 상태값 등)은 인덱스 고려

## 계층·의존성
- 의존 방향은 `controller → service → repository`만 허용 (controller에서 repository 직접 호출 금지)
- 다른 도메인의 repository를 직접 주입하지 않기 (해당 도메인의 service를 통해 조회)
- 서비스 간 순환 의존 금지 (A ↔ B가 서로 주입하면 구조를 다시 나눌 것)
- `entity`는 `dto`, `controller`, `service`를 참조하지 않기
- DTO ↔ 엔티티 변환은 DTO 쪽(정적 팩토리 `from()` 등)에서 처리
- 여러 도메인이 함께 쓰는 코드만 `global`에 두고, 특정 도메인 로직은 `global`에 넣지 않기

## 보안
- 요청 DTO는 `@Valid` + Bean Validation(`@NotNull`, `@Size` 등)으로 검증
- 엔티티를 응답으로 직접 반환하지 않고 DTO로 변환
- 권한 확인은 서비스 계층에서 수행 (예: 작성자만 수정·삭제 가능)
- 쿼리는 JPA 메서드 / JPQL 파라미터 바인딩만 사용, 문자열 이어붙이기 금지
- 에러 응답에 스택트레이스·내부 정보 노출 금지 (클라이언트에는 공통 에러 코드·메시지만)
- 로그에 민감 정보(비밀번호, 토큰, 키 등) 노출 금지. 예상하지 못한 예외(500)는 원인 추적을 위해 스택트레이스를 로그에 남긴다

## 안정성
- 상태를 바꾸는 로직은 서비스 메서드에 `@Transactional`로 묶기
- 동시 요청 시 데이터가 깨질 수 있는 곳(중복 생성, 카운트 증감 등)은 유니크 제약 또는 락으로 방어
- 비즈니스 규칙 위반은 `BaseException` + 에러 코드로 처리, `null` 반환으로 넘기지 않기
- 새 기능에는 정상 케이스와 주요 실패 케이스 테스트를 함께 작성

# 절대 금지
- 운영 환경 DB·서버 접근 금지
- 비밀번호, 키 등 민감 정보 커밋 금지 (`application.yaml`에 실제 값 하드코딩 금지, 환경변수 사용 / 목록은 `.env.example`)
- `main` 브랜치에 직접 커밋/푸시 금지
- 요청하지 않은 의존성 추가, 패키지 구조 변경, 대규모 리팩토링 금지
- 예상과 다른 연결/에러 발생 시 즉시 중단하고, 그 전에 실행한 작업까지 보고
