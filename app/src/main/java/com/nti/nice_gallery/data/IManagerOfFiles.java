package com.nti.nice_gallery.data;

import android.graphics.Bitmap;

import com.nti.nice_gallery.models.ModelMediaTreeItem;
import com.nti.nice_gallery.models.ModelStorage;

import java.util.List;

public interface IManagerOfFiles {
    List<ModelMediaTreeItem> getAllFiles();
    Bitmap getItemPreviewAsBitmap(ModelMediaTreeItem item);
    List<ModelStorage> getAllStorages();
}
