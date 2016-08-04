package com.skystreamtv.element_ez_stream.updater.model;

import java.io.Serializable;
import java.util.List;

public class Skins implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Skin> skins;

    public Skins() {
    }

    public Skins(List<Skin> skins) {
        this.skins = skins;
    }

    public List<Skin> getSkins() {
        return skins;
    }

    public void setSkins(List<Skin> skins) {
        this.skins = skins;
    }
}
