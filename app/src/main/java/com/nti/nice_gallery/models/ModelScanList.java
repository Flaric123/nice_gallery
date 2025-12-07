package com.nti.nice_gallery.models;

import java.util.List;

public class ModelScanList {

    public List<StoragePathsGroup> scanList;

    public static class StoragePathsGroup {
        public String storageName;
        public boolean scanAllStorage;
        public List<String> paths;
    }
}
