package kr.ac.kopo.travelplanner;

import java.io.Serializable;

public class DiaryEntry implements Serializable {
    private String title;
    private String date;
    private String content;
    private boolean isPublic;

    public DiaryEntry(String title, String date, String content, boolean isPublic) {
        this.title = title;
        this.date = date;
        this.content = content;
        this.isPublic = isPublic;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getContent() { return content; }
    public boolean isPublic() { return isPublic; }

    public void setTitle(String title) { this.title = title; }
    public void setDate(String date) { this.date = date; }
    public void setContent(String content) { this.content = content; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public String getPreview() {
        if (content.length() > 50) {
            return content.substring(0, 50) + "...";
        }
        return content;
    }
}