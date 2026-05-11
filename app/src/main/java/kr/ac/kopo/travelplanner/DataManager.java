package kr.ac.kopo.travelplanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DataManager {
    private static DataManager instance;
    private List<Trip> trips;
    private int nextTripId = 1;

    private DataManager() {
        trips = new ArrayList<Trip>();
        loadSampleData();
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private void loadSampleData() {
        Trip trip1 = new Trip(nextTripId++, "도쿄 벚꽃 여행", "일본 도쿄",
                "2024-03-25", "2024-03-30");
        trip1.addSchedule(new Schedule("2024-03-25", "14:00",
                "나리타 국제공항", "일본 千葉県成田市取香", "입국 심사 유의", "교통"));
        trip1.addSchedule(new Schedule("2024-03-25", "18:00",
                "신주쿠 그랜드 호텔", "일본 東京都新宿区西新宿", "체크인 16시부터", "숙소"));
        trip1.addSchedule(new Schedule("2024-03-26", "10:00",
                "우에노 공원", "일본 東京都台東区上野公園", "벚꽃 명소", "관광지"));
        trip1.addSchedule(new Schedule("2024-03-26", "13:00",
                "이치란 라멘 우에노점", "일본 東京都台東区上野6丁目", "1인 칸막이 식당", "식당"));

        Trip trip2 = new Trip(nextTripId++, "제주도 힐링 여행", "제주특별자치도",
                "2024-04-10", "2024-04-13");
        trip2.addSchedule(new Schedule("2024-04-10", "09:00",
                "제주국제공항", "제주 용담2동 2002", "렌터카 픽업", "교통"));
        trip2.addSchedule(new Schedule("2024-04-10", "14:00",
                "성산일출봉", "제주 서귀포시 성산읍 일출로 284-12", "세계자연유산", "관광지"));
        trip2.addSchedule(new Schedule("2024-04-11", "11:00",
                "흑돼지 거리", "제주 제주시 연동 312-1", "흑돼지 맛집 골목", "식당"));

        trips.add(trip1);
        trips.add(trip2);
    }

    public List<Trip> getTrips() { return trips; }

    public Trip getTripById(int id) {
        for (int i = 0; i < trips.size(); i++) {
            if (trips.get(i).getId() == id) {
                return trips.get(i);
            }
        }
        return null;
    }

    public void addTrip(String name, String destination, String startDate, String endDate) {
        trips.add(new Trip(nextTripId++, name, destination, startDate, endDate));
    }

    public void removeTrip(int id) {
        Iterator<Trip> iterator = trips.iterator();
        while (iterator.hasNext()) {
            Trip t = iterator.next();
            if (t.getId() == id) {
                iterator.remove();
                break;
            }
        }
    }

    public List<Trip> getSortedTrips(int sortType) {
        List<Trip> sorted = new ArrayList<Trip>(trips);
        switch (sortType) {
            case 0:
                Collections.sort(sorted, new Comparator<Trip>() {
                    @Override
                    public int compare(Trip a, Trip b) {
                        return b.getId() - a.getId();
                    }
                });
                break;
            case 1:
                Collections.sort(sorted, new Comparator<Trip>() {
                    @Override
                    public int compare(Trip a, Trip b) {
                        return a.getName().compareTo(b.getName());
                    }
                });
                break;
            case 2:
                Collections.sort(sorted, new Comparator<Trip>() {
                    @Override
                    public int compare(Trip a, Trip b) {
                        return a.getStartDate().compareTo(b.getStartDate());
                    }
                });
                break;
        }
        return sorted;
    }
}