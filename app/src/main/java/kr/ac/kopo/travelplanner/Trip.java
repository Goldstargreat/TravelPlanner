package kr.ac.kopo.travelplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Trip implements Serializable
{
    private int id;
    private String name;
    private String destination;
    private String startDate;
    private String endDate;
    private String coverImagePath;
    private List<Schedule> schedules;
    private List<String> photoPaths;
    private List<DiaryEntry> diaryEntries;

    public Trip(int id, String name, String destination, String startDate, String endDate) {
        this.id = id;
        this.name = name;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.coverImagePath = "";
        this.schedules = new ArrayList<Schedule>();
        this.photoPaths = new ArrayList<String>();
        this.diaryEntries = new ArrayList<DiaryEntry>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDestination() { return destination; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getCoverImagePath() { return coverImagePath; }
    public List<Schedule> getSchedules() { return schedules; }
    public List<String> getPhotoPaths() { return photoPaths; }
    public List<DiaryEntry> getDiaryEntries() { return diaryEntries; }

    public void setName(String name) { this.name = name; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setCoverImagePath(String path) { this.coverImagePath = path; }

    public void addSchedule(Schedule s) { schedules.add(s); }
    public void addPhoto(String path) { photoPaths.add(path); }
    public void addDiary(DiaryEntry d) { diaryEntries.add(d); }

    public void removeSchedule(int index) {
        if (index >= 0 && index < schedules.size()) {
            schedules.remove(index);
        }
    }
    public void removePhoto(int index) {
        if (index >= 0 && index < photoPaths.size()) {
            photoPaths.remove(index);
        }
    }
    public void removeDiary(int index) {
        if (index >= 0 && index < diaryEntries.size()) {
            diaryEntries.remove(index);
        }
    }

    public String getDateRange() {
        return startDate + " ~ " + endDate;
    }

    public int getScheduleCount() {
        return schedules.size();
    }
}