package com.example.test.qrcode_to_database.tools;

public class Event {

    private int img;
    private String title;
    private String description;

    public Event() {
        super();
    }

    public Event(int img, String title, String description) {
        super();
        this.img = img;
        this.title = title;
        this.description = description;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
