package com.divarc.music365.entity;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.ArrayList;

@Element(name="channel")
public class Channel implements Serializable{


    @Element(name="description")
    Description description;

    @ElementList(name="days")
    ArrayList<Day> days;


@ElementList(name="programmdata")
    ArrayList<Programmsstats> programmsstatses;


    @Attribute(name="title",required=true)
    String title;

    @Attribute(name="stream",required=true)
    String stream;

    public ArrayList<Day> getDays() {
        return days;
    }

    public void setDays(ArrayList<Day> days) {
        this.days = days;
    }

    public String getTitle() {
        return title;
    }

    public String getStream() {
        return stream;
    }


    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public ArrayList<Programmsstats> getProgrammsstatses() {
        return programmsstatses;
    }

    public void setProgrammsstatses(ArrayList<Programmsstats> programmsstatses) {
        this.programmsstatses = programmsstatses;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "description=" + description +
                ", days=" + days +
                ", programmsstatses=" + programmsstatses +
                ", title='" + title + '\'' +
                ", stream='" + stream + '\'' +
                '}';
    }
}
