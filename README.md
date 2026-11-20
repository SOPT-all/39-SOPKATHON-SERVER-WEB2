# 39th SOPT SOPKATHON WEB2 Server

## 🛠 Tech Stack
- Java 21, Spring Boot 4.1.0
- Spring Data JPA, MySQL (로컬·운영) / H2 (테스트), Docker Compose (로컬 MySQL)
- Lombok, springdoc-openapi (Swagger), Gradle

## 🚀 실행 방법
```bash
cp .env.example .env   # DB 비밀번호 등 값 설정
docker compose up -d   # 로컬 MySQL 실행
./gradlew bootRun      # local 프로필, .env를 읽어 로컬 MySQL에 접속
./gradlew test      # 테스트
./gradlew build     # 빌드
```
- Swagger: http://localhost:8080/swagger-ui/index.html
- 로컬 환경 변수는 `.env.example`, 운영 환경 변수(`ENV_PROD` 시크릿)는 `.env.prod.example` 참고

## 📁 패키지 구조
```
org.sopt.sopkathon
├── domain
│   └── <도메인>
│       ├── controller
│       ├── service
│       ├── repository
│       ├── entity
│       ├── dto
│       └── code        # 도메인 에러·성공 코드
└── global
    ├── common          # 공통 코드, BaseEntity
    ├── config
    ├── exception
    └── response
```

## 🌿 Branch 전략
- `main`: 배포 브랜치
- `develop`: 개발 통합 브랜치 (동작 테스트 후 `main`으로 머지)
- 작업 브랜치: `타입/#이슈번호/작업내용` (예: `feat/#1/common-response`), `develop`에서 분기

## 📝 Convention
- 커밋 / PR 제목: `타입: 작업 내용`
- 이슈 제목: `[타입] 작업 내용`

| 타입 | 설명 |
|---|---|
| feat | 새로운 기능 |
| fix | 버그 수정 |
| refactor | 리팩토링 |
| docs | 문서 수정 |
| test | 테스트 코드 |
| build | 빌드, 의존성 |
| chore | 기타 설정 |
