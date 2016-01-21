package com.divarc.music365.entity;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.io.Serializable;

/**
 * Created by Nick on 23.10.2015.
 */
@Element(name = "message")
public class Message implements Serializable{

    @Attribute(name = "id")
    int id;

    @Attribute(name = "imagesrc")
    String image;

    @Element(name = "text")
    String text;

    public int getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getText() {
        return text;
    }


    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", image='" + image + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
