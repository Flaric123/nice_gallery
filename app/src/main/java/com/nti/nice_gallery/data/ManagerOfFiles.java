package com.nti.nice_gallery.data;

import android.content.Context;
import android.graphics.Bitmap;

import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelStorage;

import java.util.Collections;
import java.util.List;

public class ManagerOfFiles implements IManagerOfFiles {

    private final Context context;

    public ManagerOfFiles(Context context) {
        this.context = context;
    }

    @Override
    public List<ModelMediaFile> getAllFiles() {
        return Collections.emptyList();
    }

    @Override
    public Bitmap getFilePreview(ModelMediaFile item) {
        return null;
    }

    @Override
    public List<ModelStorage> getAllStorages() {
        return Collections.emptyList();
    }
}