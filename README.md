# 🧥 옷장을 부탁해
[![Codecov](https://codecov.io/gh/SB01-Team09/sb01-otboo-team09/branch/develop/graph/badge.svg)](https://codecov.io/gh/SB01-Team09/sb01-otboo-team09)
## 📝 프로젝트 소개
- 날씨 데이터와 사용자의 위치 데이터를 기반으로 의상 조합을 추천하고,
OOTD 피드, 팔로우, 메시지 기능을 제공하는 소셜 기반 패션 추천 플랫폼입니다.
- 개발 기간: 2025.06 - 2025.07
- 팀원: 백엔드 5명
<img width="2516" height="1352" alt="image" src="https://github.com/user-attachments/assets/084f2a42-dc41-469f-a6d3-80e487d0e05c" />

## 👩🏻‍🦰 역할
- GitHub Actions 기반 CI/CD 파이프라인 구축
- 시스템 아키텍처 설계 및 AWS 배포
- 팔로우, 조회 기능 백엔드 구현
    - 팔로우 요약 정보 조회 시 Redis 캐시 설계 및 도입 (응답 시간 80% 이상 개선)

## 🛠️ 기술 스택
### 📍 Backend
- Spring Boot
- Spring Data JPA
- Spring Security
- Spring Batch
### 💾 Database, Infrastructure
- PostgreSQL
- AWS
- Docker
- Github Actions
### 📢 Message, Caching
- Redis
- Kafka

## 🖼️ 배포 다이어그램
<img width="1540" height="1069" alt="image" src="https://github.com/user-attachments/assets/4b1411d4-23e9-42df-b947-ddcc19ad7c7d" />

## ♾️ CI/CD 파이프라인 및 배포 결과
<img width="1883" height="852" alt="image" src="https://github.com/user-attachments/assets/57192397-06d9-4d31-b37d-8c72a517fd8c" />
<img width="1645" height="771" alt="image" src="https://github.com/user-attachments/assets/1243debd-ee22-47c6-b4ef-f23a550f0803" />

- GitHub Actions를 활용하여 코드 Push → 테스트 실행 → Docker 이미지 빌드 → ECR Push → ECS 배포까지 자동화된 CI/CD 파이프라인을 구축했습니다.
- OIDC 기반 인증을 적용하여 GitHub에 AWS Access Key를 저장하지 않고 안전하게 AWS 리소스에 접근하도록 설계했습니다.


## 📁 폴더 구조
- 도메인 주도 개발로 비즈니스 요구사항을 도메인 별로 분리
<img width="299" height="568" alt="스크린샷 2025-07-29 152952" src="https://github.com/user-attachments/assets/183e3899-411d-4202-a1d0-1f6aa8796707" />

## ⚙️ ERD
<details>
<summary>ERD 확인</summary>
<img width="4680" height="1832" alt="otboo" src="https://github.com/user-attachments/assets/332c118e-fcc6-4338-91cc-0c6983f593b3" />
</details>
<br/>

## 🙌🏻 협업과정
<img width="995" height="705" alt="image" src="https://github.com/user-attachments/assets/0c4acb9f-9f49-4b31-a921-9f73125e529e" />
<img width="927" height="770" alt="image" src="https://github.com/user-attachments/assets/6dc74cb9-26f0-48f2-8712-815fa55c8fe7" />
<img width="1501" height="940" alt="image" src="https://github.com/user-attachments/assets/27f6bebb-dc88-4d60-9883-dafc1025a4b8" />

- GitHub Issue와 PR 템플릿을 활용해 작업 흐름을 관리하고 `main / dev / feature` 브랜치 전략을 적용했습니다.  
- 팀원 간 코드 리뷰를 통해 코드 품질을 개선하고 리포지토리 및 서비스 테스트 코드 작성을 통해 테스트 커버리지를 75%까지 올렸습니다.

