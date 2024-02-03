package com.example.demo;

public class TodoData {

    private int id;

    private String titleStat;


    private long date;

    private TodoStatus status;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getTitleStat() {
        return titleStat;
    }

    public long getDate() {
        return date;
    }


    public TodoStatus getStatus() {
        return status;
    }

    public void setStatus(TodoStatus status) {
        this.status = status;
    }
    public enum TodoStatus {
        PENDING, LATE, DONE
    }

}
