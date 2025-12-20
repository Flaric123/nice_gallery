package com.nti.nice_gallery.models;

public class ModelStorage {

    public enum Type { Primary, Removable, Else}

    public final String name;
    public final String path;
    public final Type type;

    public ModelStorage(String name, String path, Type type) {
        this.name = name;
        this.path = path;
        this.type = type;
    }
}
