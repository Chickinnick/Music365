package com.divarc.music365.entity;

import org.simpleframework.xml.Element;

import java.io.Serializable;

/**
 * Created by Nick on 25.11.2015.
 */
@Element(name = "programmsstats")
public class Programmsstats  implements Serializable{

    @Element(name = "pdescription")
    String pdescription;

    @Element(name = "ptext")
    String ptext;

    @Element(name = "pimagesrc")
    String pimagesrc;

    @Override
    public String toString() {
        return "Programmsstats{" +
                "pdescription='" + pdescription + '\'' +
                ", ptext='" + ptext + '\'' +
                ", pimagesrc='" + pimagesrc + '\'' +
                '}';
    }

    public String getPdescription() {
        return pdescription;
    }

    public void setPdescription(String pdescription) {
        this.pdescription = pdescription;
    }

    public String getPtext() {
        return ptext;
    }

    public void setPtext(String ptext) {
        this.ptext = ptext;
    }

    public String getPimagesrc() {
        return pimagesrc;
    }

    public void setPimagesrc(String pimagesrc) {
        this.pimagesrc = pimagesrc;
    }
}

