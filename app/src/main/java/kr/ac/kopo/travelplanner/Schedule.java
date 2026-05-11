package kr.ac.kopo.travelplanner;

import java.io.Serializable;

public class Schedule implements Serializable {
    private String date;
    private String time;
    private String placeName;
    private String address;
    private String memo;
    private String category;
    private boolean completed;

    public Schedule(String date, String time, String placeName,
                    String address, String memo, String category) {
        this.date = date;
        this.time = time;
        this.placeName = placeName;
        this.address = address;
        this.memo = memo;
        this.category = category;
        this.completed = false;
    }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getPlaceName() { return placeName; }
    public String getAddress() { return address; }
    public String getMemo() { return memo; }
    public String getCategory() { return category; }
    public boolean isCompleted() { return completed; }

    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setPlaceName(String name) { this.placeName = name; }
    public void setAddress(String address) { this.address = address; }
    public void setMemo(String memo) { this.memo = memo; }
    public void setCategory(String category) { this.category = category; }

    public int getCategoryColor() {
        switch (category) {
            case "숙소": return 0xFF4CAF50;
            case "식당": return 0xFFFF5722;
            case "관광지": return 0xFF2196F3;
            case "교통": return 0xFF9C27B0;
            case "쇼핑": return 0xFFE91E63;
            default: return 0xFF607D8B;
        }
    }
}