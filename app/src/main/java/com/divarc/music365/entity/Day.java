package com.divarc.music365.entity;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Nick on 24.09.2015.
 */

@Element(name="day")
public class Day implements Serializable{
    @Attribute(name="name")
    String name;

    @ElementList(name="programms")
    ArrayList<Program> programs;

    public String getName() {
        return name;
    }



    public ArrayList<Program> getPrograms() {
        return programs;
    }

    @Override
    public String toString() {
        return "Day{" +
                "name='" + name + '\'' +
                ", programs=" + programs +
                '}';
    }
}
