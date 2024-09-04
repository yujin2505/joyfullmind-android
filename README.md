# 💡조이풀마인드 프로젝트

조이풀마인드는 AI 챗봇을 활용한 심리상담 서비스입니다. 사용자의 감정을 분석하고, 기분 전환에 도움이 되는 노래나 산책 코스를 추천하는 등 다양한 기능을 제공합니다.

![355060430-c4e4cf7a-733d-4ae6-a4bb-682efc51f0a5](https://github.com/user-attachments/assets/96e912e7-f7ef-4d3d-9966-d298850d1d30)


## 🗂목차
1. [프로젝트 개요](#프로젝트-개요)
2. [개발 환경](#개발-환경)
3. [프로젝트 설계](#프로젝트-설계)
4. [사용한 오픈 API](#사용한-오픈-API)
5. [프로젝트 개발](#프로젝트-개발)
6. [프로젝트 문제 해결](#프로젝트-문제-해결)
7. [참고 자료](#참고-자료)

## 프로젝트 개요
### 🔔기획 의도
- 정부의 정신건강 및 자살률 문제 해결을 위한 심리 상담 서비스 제공
- AI 기술을 활용한 접근성 높은 정신 건강 관리 서비스
![기획 의도2](https://github.com/user-attachments/assets/ec73dcff-572b-4fd1-83a5-bd441e594450)



### 📈시장 분석
- AI 챗봇 시장의 연평균 복합 성장률(CAGR) 성장 전망
- 미국의 정신 건강 관리 챗봇 WOEBOT의 임상적 검증 사례
![시장 분석2](https://github.com/user-attachments/assets/623fa64d-b882-4087-b6ea-ab244e504d3c)



## 🛠️개발 환경
- **Database**: MySQL, Firestore
- **CI/CD**: GitHub Actions
- **백엔드 서버**: Flask, Docker, AWS Lambda
- **협업 툴**: GitHub, Slack
- **Machine Learning**: SBERT, KoBERT

## 🚩서버 아키텍쳐

![스크린샷 2024-08-06 093626](https://github.com/user-attachments/assets/44d7e9e5-8294-4231-a306-6e51c6eec651)


## 📌핵심 기능
#### 1. 일상 대화 챗봇
<img width="202" alt="5sh6sh" src="https://github.com/user-attachments/assets/f1584b7a-a36a-44c2-92b2-668970fa2325">

- AI 심리 상담: 사용자가 입력한 메시지에 대해 AI 챗봇이 가장 적절한 답변을 제공합니다.
- 실시간 채팅: 사용자는 실시간으로 챗봇과 대화할 수 있으며, 모든 대화는 데이터베이스에 저장됩니다.

#### 2. 감정 분석
![KakaoTalk_20240806_101714416_01](https://github.com/user-attachments/assets/9af6dc4b-001d-429c-8033-56f160fc3d2b)

- 감정 레이블링: 사용자가 입력한 문장을 AI 모델이 감정 레이블로 분류합니다.
- 감정 분석 결과 제공: 분석된 감정 결과를 시각적으로 제공하고, 사용자의 감정 변화를 추적합니다.
- 감정 기반 피드백: 분석된 감정 결과를 바탕으로 맞춤형 피드백과 추천 서비스를 제공합니다.

#### 3. 산책로 추천
<img width="202" alt="ddddddd" src="https://github.com/user-attachments/assets/f600367f-be65-4c3a-a31c-131c2516c746"> <img width="202" alt="KakaoTalk_20240806_101714416_03" src="https://github.com/user-attachments/assets/8fd30188-a1ed-438f-9e6c-b35afeac1d90"> 

- 산책로 정보 제공: GPS 위치 기반으로 산책로 정보를 제공합니다. 소요 시간, 거리, 위치 등의 상세 정보를 포함합니다.

#### 4. 감정 분석 결과에 따른 노래 추천
![KakaoTalk_20240806_101714416_02](https://github.com/user-attachments/assets/897584f7-0077-47eb-a2e0-bd84807348d9)

- 감정 기반 노래 추천: 사용자의 감정 분석 결과를 바탕으로 기분 전환에 도움이 될 만한 노래를 추천합니다.
- 추천 트랙: 특정 감정에 맞는 아티스트와 곡을 시드로 하여 유사한 스타일의 음악을 추천합니다.


## 프로젝트 설계
### 🤖AI 심리 상담 챗봇
- SBERT 모델을 사용한 문장 임베딩 및 유사도 계산

### 🔎감정 분석 기능
- KoBERT 모델을 사용한 감정 분류

### 📌데이터베이스
- RDBMS: 사용자 회원 정보와 일기 정보 저장
- NoSQL: 채팅 메시지 데이터 및 사용자 프로필 이미지 저장

![image](https://github.com/user-attachments/assets/4d792c28-8794-4a1c-a035-82c1bd005bbc)


## 🕶사용한 오픈 API
- **네이버 로그인 API**: 네이버 아이디 연동
- **Google 주변 지역 검색 API**: 산책로 추천 기능 구현
- **TMAP 보행자 경로 안내 API**: 보행자 경로 안내
- **Spotify Web API**: 노래 추천 기능 구현

## 프로젝트 개발
### 🤗협업 도구
- GitHub: 프로젝트 버전 관리
- Slack: 이슈 및 공지 사항 공유

### 챗봇 데이터셋 가공
- AI-HUB 웰니스 상담 데이터 사용
- https://aihub.or.kr/aihubdata/data/view.do?currMenu=120&topMenu=100&aihubDataSe=extrldata&dataSetSn=267
- 텍스트 전처리 및 임베딩 데이터 생성

  
![image](https://github.com/user-attachments/assets/1e54ab0e-7c36-4788-96ed-429efdcf5a7e)


### 🤖감정 분류 모델
- pandas를 사용한 데이터 호출 및 전처리
- KoBERT 모델을 사용한 감정 분류

## 프로젝트 문제 해결
### 서버리스 배포 이슈
- AWS CLI를 이용한 도커 이미지 배포

### 일기 카테고리 오류
- 날짜 범위에 따른 일기 필터링 기능 구현

## 참고 자료
- [GitHub Repository](https://github.com/GoEnding/joyfulmind-android)
- [시연 영상](https://www.youtube.com/watch?v=LqMbSQpaxGA)
- [Figma 디자인](https://www.figma.com/design/WSZV6yOy6JAbywovAEw5yk/yhPJ?node-id=0-1&t=baVEw9J1hHSVU6A8-0)
- [ERD Diagram](https://dbdiagram.io/d/6684a0bd9939893daee0eec9)
- [API Specification](https://documenter.getpostman.com/view/35043994/2sA3e5d81u)


