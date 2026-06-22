# ✈️ Travel Planner

> 나만의 여행을 계획하고, 기록하고, 추억하는 Android 여행 플래너 앱(Android 기말고사 과제)

---

## 📖 프로젝트 소개

**Travel Planner**는 여행의 시작부터 끝까지 한 곳에서 관리할 수 있는 Android 앱입니다.  
여행 일정 등록, 사진 관리, 여행 일기 작성 기능을 제공하며, 직관적인 UI로 여행의 모든 순간을 간편하게 기록할 수 있습니다.

---

## 📦 패키지 정보

| 항목 | 내용 |
|------|------|
| 플랫폼 | Android |
| 언어 | Java |
| 최소 SDK | Android (AppCompat 기반) |

---

## 🗂️ 프로젝트 구조

```
kr.ac.kopo.travelplanner/
│
├── MainActivity.java            # 앱 진입점 - 여행 목록 화면
├── TripDetailActivity.java      # 여행 상세 - 일정 목록 및 관리
├── AddScheduleActivity.java     # 새 일정 추가 화면
├── DiaryActivity.java           # 여행 일기 작성 및 목록
├── PhotoGalleryActivity.java    # 사진첩 화면
│
├── Trip.java                    # 여행 데이터 모델
├── Schedule.java                # 일정 데이터 모델
├── DiaryEntry.java              # 일기 데이터 모델
│
├── DataManager.java             # 싱글톤 데이터 관리자
└── TripAdapter.java             # 여행 목록용 ListView 어댑터
```

---

## ✨ 주요 기능

### 🗺️ 여행 관리
- 새 여행 추가 (이름, 목적지, 시작일, 종료일)
- 여행 목록 정렬 (최신순 / 이름순 / 날짜순)
- 여행 삭제 (목록 롱클릭)
- 여행 목록이 비어있을 경우 안내 메시지 표시

### 📅 일정 관리
- 날짜, 시간, 장소명, 주소, 메모, 카테고리 입력
- 카테고리별 색상 구분 (숙소 / 식당 / 관광지 / 교통 / 쇼핑)
- 일정 완료 체크박스
- 일정 삭제 (테이블 행 롱클릭)

### 📸 사진첩
- 갤러리에서 사진 추가
- 그리드뷰 + 상단 갤러리뷰 동시 표시
- 사진 상세 보기 및 삭제

### 📝 여행 일기
- 제목, 날짜, 내용, 공개/비공개 설정
- 카드 형태의 2열 그리드 목록
- 일기 공유 (안드로이드 공유 인텐트)
- 일기 삭제 (카드 롱클릭)

---

## 🏗️ 아키텍처

```
[Activity Layer]
  MainActivity ──────────────────────────────────────┐
  TripDetailActivity ──> AddScheduleActivity         │
                    ──> PhotoGalleryActivity          │
                    ──> DiaryActivity                 │
                                                      │
[Data Layer]                                          │
  DataManager (Singleton) <─────────────────────────┘
       │
       ├── List<Trip>
       │     ├── List<Schedule>
       │     ├── List<String> (photoPaths)
       │     └── List<DiaryEntry>
```

- **DataManager**: 싱글톤 패턴으로 앱 전체 데이터를 메모리에서 관리
- **Activity 간 통신**: `Intent`를 통해 `trip_id` 전달
- **결과 반환**: `startActivityForResult` / `onActivityResult` 사용

---

## 🎨 카테고리 색상표

| 카테고리 | 색상 |
|---------|------|
| 숙소 | 🟢 Green `#4CAF50` |
| 식당 | 🔴 Deep Orange `#FF5722` |
| 관광지 | 🔵 Blue `#2196F3` |
| 교통 | 🟣 Purple `#9C27B0` |
| 쇼핑 | 🩷 Pink `#E91E63` |
| 기타 | ⬜ Blue Grey `#607D8B` |

---

## 🚀 실행 방법

1. 프로젝트를 Android Studio에서 열기
2. `Gradle Sync` 실행
3. 에뮬레이터 또는 실제 디바이스에서 실행

> **참고:** 사진첩 기능 사용 시 갤러리 접근 권한(`READ_EXTERNAL_STORAGE`)이 필요합니다.

---

## 📝 샘플 데이터

앱 최초 실행 시 아래 두 여행이 자동으로 로드됩니다.

> **예시**

| 여행명 | 목적지 | 기간 |
|--------|--------|------|
| 도쿄 벚꽃 여행 | 일본 도쿄 | 2024-03-25 ~ 2024-03-30 |
| 제주도 힐링 여행 | 제주특별자치도 | 2024-04-10 ~ 2024-04-13 |

---

## ⚠️ 주의사항

- 현재 데이터는 **메모리에만 저장**되며, 앱 종료 시 초기화됩니다.
- 추후 `SharedPreferences` 또는 `SQLite`, `Room DB` 연동을 통한 영구 저장 기능 추가를 권장합니다.

---

## 📄 라이선스

본 프로젝트는 학습 목적으로 제작되었습니다. (`kr.ac.kopo` - 한국폴리텍대학교 정수캠퍼스)
