package com.divarc.music365.entity;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.io.Serializable;

/**
 * Created by Nick on 23.10.2015.
 */

@Element(name="description")
public class Description implements Serializable{

    @Attribute(name = "imagesrc")
    String image;

   @Element(name = "text")
    String text;

    public String getImage() {
        return image;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Description{" +
                "image='" + image + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
