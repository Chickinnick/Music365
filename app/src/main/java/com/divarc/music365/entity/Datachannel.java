package com.divarc.music365.entity;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Root(name="datachannel")
public class Datachannel implements Serializable {


    @Element(name="message")
    Message message;



    @ElementList(name="channels")
    List<Channel> channels;


    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<Channel> channels) {
        this.channels = channels;
    }

    @Override
    public String toString() {
        return "Datachannel{" +
                "message=" + message +
                ", channels=" + channels +
                '}';
    }

}
