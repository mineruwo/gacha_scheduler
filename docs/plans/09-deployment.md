# 09. 배포 (DevOps)

상태: 준비물(운영 설정/systemd/nginx 템플릿) 완료 — **배포 대상을 네이버클라우드플랫폼(NCP)으로 결정(2026-07-16, 사용자 결정)**, 실제 프로비저닝은 아래 런북대로 사용자 진행 대기
선행 조건: NCP 계정 생성 + 결제수단 등록(사용자, 완료 — 신규가입 크레딧 발급받음)

## 배경

기능 파트(01~08)의 백엔드 구현은 끝났지만, 지금까지는 전부 로컬 개발 환경(H2 → 로컬 PostgreSQL)에서만 돌아갔다. 실제 서비스를 띄우려면 배포 인프라가 필요하다.

**클라우드 프로바이더 결정 경위**: 처음엔 AWS(파트 01에서 `PostgreSQL(AWS RDS)` 전제로 설계)를 쓰려 했으나 AWS 프리티어 신규 가입이 막혀 재검토했다. Oracle Cloud(영구 무료지만 ARM 재고 이슈)와 GCP(Always Free가 미국 리전 한정이라 한국 지연시간 불리)를 검토했고, 최종적으로 **서울 리전 + 신규가입 크레딧 + 결제 편의성**을 이유로 **네이버클라우드플랫폼(NCP)**을 선택했다. 이 프로젝트는 관리형 특화 서비스(서버리스, 프로바이더 전용 DB 등)를 쓰지 않고 순수 리눅스 VM + 표준 오픈소스(Postgres/systemd/nginx) 구성이라 프로바이더 종속이 거의 없고, 나중에 다른 곳으로 옮기더라도 서버 재설치 + `pg_dump`/`restore` 정도로 충분하다.

## 배포 아키텍처 결정

트래픽이 거의 없는 초기 단계라 관리 부담과 비용을 최소화하는 구성을 택함(AWS 기준으로 세웠던 원래 결정을 NCP 용어로 그대로 치환):

- **NCP Server(소형, 2vCPU/2~4GB RAM 권장) 1대**에서 jar를 **systemd**로 직접 실행 (컨테이너/오케스트레이션 없음 — 지금 규모에 과함, 로드밸런서 상시 비용도 없앰). OS는 Ubuntu 22.04 LTS 권장(자료가 제일 많음)
- **nginx**를 앞단에 둬서 TLS 종료(Let's Encrypt/certbot) + 리버스 프록시만 담당, 앱 자체는 8080에서 평문 HTTP로만 리슨
- DB는 **Cloud DB for PostgreSQL**(관리형) 또는 **같은 Server에 Postgres 직접 설치**(더 저렴, 지금 로컬 개발과 동일 구성) 둘 다 가능 — 크레딧 소진 속도가 걱정되면 후자로 시작해도 무방(로컬 개발에서 이미 검증된 구성 그대로)
- 보안: AWS의 "보안 그룹"에 해당하는 NCP **ACG(Access Control Group)**로 인바운드 제어. Server용 ACG에는 22(SSH, 본인 IP만)/80/443만 열고 8080은 열지 않음(nginx를 통해서만 접근). Cloud DB를 쓰는 경우 DB용 ACG에는 Server의 ACG 이름만 소스로 등록해 5432를 허용(퍼블릭 접근 차단)
- 이유: 컨테이너화가 나중에 필요해지면(오토스케일링 등) 그때 마이그레이션해도 늦지 않음 — 지금 단계에서 과설계하지 않는다

## 준비된 것 (2026-07-16, 코드/템플릿)

- `backend/backend_gacha/src/main/resources/application-prod.properties` — `SPRING_PROFILES_ACTIVE=prod`로 활성화. 에러 응답에서 스택트레이스/내부 메시지 숨김, `spring.jpa.open-in-view=false`(이 프로젝트는 애초에 지연 연관관계를 안 쓰는 컨벤션이라 꺼도 안전), SQL 로그 끔, actuator는 `/actuator/health`만 노출
- `build.gradle`에 `spring-boot-starter-actuator` 추가, `SecurityConfig`에 `/actuator/health` permitAll 추가(로드밸런서/모니터링이 토큰 없이 헬스체크할 수 있어야 함) — `SecurityConfigTest`에 케이스 추가, 전체 테스트 78개 통과
- `backend/backend_gacha/deploy/gacha-scheduler-backend.service` — jar를 전용 유저(`gacha`)로 실행하는 systemd 유닛. 힙은 `-Xmx512m`으로 제한(1GB RAM 인스턴스 기준), 실패 시 자동 재시작
- `backend/backend_gacha/deploy/backend.env.example` — 운영 환경변수 템플릿(`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`/`GOOGLE_CLIENT_ID`/`JWT_SECRET`/`CORS_ALLOWED_ORIGINS`). 실제 값이 채워진 `backend.env`는 서버의 `/etc/gacha-scheduler/backend.env`에만 두고 절대 커밋하지 않음
- `backend/backend_gacha/deploy/nginx-gacha-scheduler.conf.example` — nginx 리버스 프록시 + certbot으로 TLS 붙이는 템플릿

## 프로비저닝 런북 (NCP 콘솔 기준)

새 계정은 기본적으로 **VPC 환경**으로 생성된다(예전 Classic 환경 가이드 글이 검색에 많이 나오는데, 포트포워딩 방식이라 지금은 다름 — VPC 환경은 아래처럼 공인 IP를 서버에 직접 연결). 콘솔 UI 세부 버튼 위치는 바뀔 수 있으니 아래는 리소스/순서 기준으로 참고.

### 0. 공통 준비

1. Console → **VPC** 메뉴에서 VPC 1개, Subnet 1개(Public) 생성 — 서버/DB 둘 다 여기 소속시킴
2. **ACG(Access Control Group)** 2개 생성: `gacha-server-acg`(서버용), `gacha-db-acg`(DB용, Cloud DB 쓸 경우만)

### 1. DB — Cloud DB for PostgreSQL(관리형) 또는 Server에 직접 설치

**A안(관리형, 편하지만 크레딧 소진 빠름)**: Console → Database → **Cloud DB for PostgreSQL** → 인스턴스 생성(가장 작은 사양) → `gacha-db-acg`에 접근 소스로 `gacha-server-acg` 이름을 등록해 5432 허용(퍼블릭 액세스는 끔) → 생성 완료 후 도메인/엔드포인트 확인

**B안(Server에 직접 설치, 로컬 개발과 동일 구성, 더 저렴)**: 아래 Server 준비가 끝난 뒤 `sudo apt install -y postgresql`로 설치, 로컬 개발 때와 동일하게 전용 DB/롤 생성

어느 쪽이든 `backend.env`의 `DB_URL=jdbc:postgresql://<db-endpoint>:5432/gacha_scheduler`에 반영. 스키마는 `ddl-auto=update`가 첫 기동 시 자동 생성, `seed.sql`은 필요시 `psql`로 수동 적용.

### 2. Server(컴퓨트 인스턴스)

1. Console → Compute → **Server** → 서버 생성: OS **Ubuntu 22.04 LTS**, 가장 작은 사양(2vCPU/2GB 전후, 트래픽 거의 없으므로 충분), 위에서 만든 Subnet + `gacha-server-acg` 선택
2. 인증키(로그인용 키페어)는 서버 생성 중 신규 발급 → `.pem` 파일 안전하게 보관(분실 시 서버 접속 불가)
3. `gacha-server-acg` 인바운드 규칙: 22(가능하면 본인 IP만), 80, 443만 허용 — **8080은 열지 않음**(nginx를 통해서만 접근)
4. Console → Network → **공인 IP(Public IP)** → 신청 후 이 서버에 연결(VPC 환경은 포트포워딩이 아니라 서버에 공인 IP를 직접 붙이는 방식)
5. SSH 접속: `ssh -i <keyfile>.pem ubuntu@<공인IP>`
6. Java 21 설치: `sudo apt update && sudo apt install -y openjdk-21-jdk`
7. nginx 설치: `sudo apt install -y nginx`, `deploy/nginx-gacha-scheduler.conf.example`을 `/etc/nginx/sites-available/gacha-scheduler`로 배치(도메인 교체) 후 `sudo ln -s /etc/nginx/sites-available/gacha-scheduler /etc/nginx/sites-enabled/` → `sudo systemctl enable --now nginx`
8. `sudo useradd -r -s /usr/sbin/nologin gacha`, `sudo mkdir -p /opt/gacha-scheduler /etc/gacha-scheduler`
9. 로컬에서 `./gradlew bootJar` 빌드 후 생성된 jar를 `scp -i <keyfile>.pem`으로 `/opt/gacha-scheduler/backend_gacha.jar`에 업로드
10. `backend.env.example`을 참고해 `/etc/gacha-scheduler/backend.env` 작성(권한 `chmod 600`, 소유자 `gacha`)
11. `deploy/gacha-scheduler-backend.service`를 `/etc/systemd/system/`에 복사 → `sudo systemctl daemon-reload && sudo systemctl enable --now gacha-scheduler-backend`
12. `curl http://localhost:8080/actuator/health`로 로컬 확인 → 도메인의 A레코드를 위 공인 IP로 연결 → `sudo certbot --nginx -d <api-domain>`으로 TLS 발급 → `https://<api-domain>/actuator/health`로 외부 확인

### 3. 프론트/모바일 쪽 반영 (참고, 실제 작업은 프론트 세션)

- `frontend/.env`의 `VITE_API_BASE_URL`을 배포된 API 도메인으로 변경 후 재빌드/재배포
- `backend.env`의 `CORS_ALLOWED_ORIGINS`에 프론트가 실제 배포된 도메인 추가
- `mobile_shell`을 원격 URL 모드로 전환하려면 `capacitor.config.json`의 `server.url`을 배포된 프론트 도메인으로 설정(README 참고)

## 실제 프로비저닝 기록 (2026-07-16)

- Server: `gacha-backend` (mi1-g3, RAM 1GB, Ubuntu 24.04), 공인 IP `101.79.26.52`
- SSH: 비밀번호 로그인 비활성화, 전용 키(`~/.ssh/gacha-scheduler/deploy_key`, 로컬 macOS에만 존재)로만 접속. `~/.ssh/config`에 `gacha-server` 별칭 등록됨(`ssh gacha-server`)
- **RAM 1GB가 JVM+Postgres 동시 구동엔 빡빡함**: 2GB 스왑 파일 추가(`vm.swappiness=10`), JVM 힙 `-Xmx384m`(원래 템플릿 512m에서 하향), Postgres `max_connections=20`(기본 100에서 하향)으로 안정화. 여유 메모리가 100MB 남짓이라 트래픽 늘면 재검토 필요
- nginx: `listen [::]:80`(IPv6) 때문에 기본 설치 직후 기동 실패 — 이 서버는 IPv6 미지원이라 기본 사이트(`/etc/nginx/sites-enabled/default`) 제거하고 `gacha-scheduler` 사이트만 사용
- DB: Cloud DB for PostgreSQL(관리형) 대신 Server에 직접 설치 — 크레딧 절약 목적, 로컬 개발과 동일 구성
- seed.sql은 게임/캐릭터/배너/공지 데이터만 반영하고 **테스트 유저(`admin@example.com`/`user@example.com`)는 배포 직후 삭제함** — 아래 "발견한 보안 이슈" 참고
- 아직 도메인이 없어 TLS 미적용, 현재는 `http://101.79.26.52`로만 접근 가능. 도메인 준비되면 certbot으로 이어서 진행

### 발견한 보안 이슈 (수정 필요, 이번 작업 범위 밖이라 코드는 안 건드림)

`UserService`의 회원가입 로직 — "같은 이메일로 구글 가입 후 비밀번호 가입을 시도하면 거부하지 않고 그 계정에 비밀번호만 추가해서 통합"(2026-07-16 이메일/비밀번호 로그인 추가 시 결정된 동작, SYNC.md 참고) — 이 **이메일 소유권 검증 없이 계정을 탈취할 수 있는 구조**임을 배포 중 발견했다. 구글 계정으로 이미 존재하는 이메일(`google_id`만 있고 `password_hash`는 null인 계정)을 공격자가 알고 있다면, `POST /api/auth/signup`에 그 이메일 + 자기가 정한 비밀번호로 가입 요청을 보내면 그대로 통과되어 **기존 계정에 비밀번호가 설정되고 그 계정으로 로그인 가능**해진다. 이메일 인증(코드 발송 등) 없이는 진짜 소유자인지 확인할 방법이 없다.

- 이번 배포에서는 정확히 이 패턴에 해당하는 시드 테스트 계정(`admin@example.com`, MAIN_ADMIN 권한)을 배포 직후 삭제해서 즉각적인 위험은 제거했다.
- 실제 유저가 구글로 가입하기 시작하면 이메일만 알면 그 계정을 비밀번호 가입으로 탈취당할 수 있는 구조적 문제가 남아있다 — **이메일 인증 없이 계정을 병합하지 않도록 회원가입 로직을 다시 봐야 함**(예: 이미 `google_id`가 있는 이메일로 signup 요청이 오면 병합 대신 거부하거나, 이메일 인증 링크를 요구).

## 다음에 재검토할 것 (지금 범위 밖)

- `spring.jpa.hibernate.ddl-auto=update`를 계속 쓸지, 실사용자 데이터가 쌓이기 전에 Flyway/Liquibase 마이그레이션으로 전환할지 — 지금은 프로젝트 전체 컨벤션(00-overview.md)을 따라 그대로 둠
- CI(GitHub Actions 등)로 테스트 자동 실행 + 배포 자동화 — 지금은 로컬에서 수동으로 빌드/scp
- 공개 API(가챠 뽑기, iCal 구독 등) rate limiting — 실사용자 트래픽이 생기기 전까지는 급하지 않음

## DoD

- [x] 클라우드 프로바이더 결정 — NCP, 계정 생성 + 신규가입 크레딧 확인 완료 (사용자)
- [x] VPC/Subnet/ACG 생성 (`gacha-scheduler` VPC 10.0.0.0/16, `gacha-subnet` 10.0.0.0/24 Public, `gacha-scheduler-default-acg`에 22/80/443 인바운드)
- [x] Server 생성 (mi1-g3 Micro, 1년 무료, RAM 1GB) + 공인 IP 연결(`101.79.26.52`) + Postgres 직접 설치(관리형 DB 대신 비용 절감) + systemd 서비스로 백엔드 기동 — 2026-07-16
- [x] nginx로 `http://101.79.26.52/actuator/health` 외부 접근 확인 (TLS는 도메인 준비되면 certbot으로 추가 예정, 아직 IP만)
- [ ] 도메인 연결 + certbot TLS 발급
- [ ] 프론트 `VITE_API_BASE_URL`을 배포 도메인으로 전환 + CORS 반영
