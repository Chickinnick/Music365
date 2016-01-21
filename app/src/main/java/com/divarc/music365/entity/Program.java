package com.divarc.music365.entity;

import org.simpleframework.xml.Element;

import java.io.Serializable;

@Element(name="programm")
public class Program implements Serializable {

    @Element(required=false)
    private String title;

    @Element(required=false)
    private String time;

    public Program(String name, String time) {
        this.title= name;
        this.time = time;
    }

    public Program() {
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Program{" +
                "title='" + title + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
