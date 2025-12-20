package com.nti.nice_gallery.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.util.Size;

import androidx.core.content.ContextCompat;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelFileFormat;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class ManagerOfFiles implements IManagerOfFiles {

    private static final String LOG_TAG = "ManagerOfFiles";
    private static Bitmap FOLDER_PLACEHOLDER;

    private final Context context;

    public ManagerOfFiles(Context context) {
        this.context = context;
        if (FOLDER_PLACEHOLDER == null) {
            FOLDER_PLACEHOLDER = ((BitmapDrawable)(ContextCompat.getDrawable(context, R.drawable.placeholder_960x960))).getBitmap();
        }
    }

    @Override
    public List<ModelMediaFile> getAllFiles() {

        class ImageContentInfo {
            public int width;
            public int height;
            public int rotation;
        }

        class VideoContentInfo {
            public int width;
            public int height;
            public int rotation;
            public int duration;
        }

        Function1<File, Date> getFileCreationTime = file -> {
            try {
                BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                return new Date(attrs.creationTime().toMillis());
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                return null;
            }
        };

        Function2<String, Boolean, ModelMediaFile.Type> getFileType = (fileName, isDirectory) -> {
            if (isDirectory) {
                return ModelMediaFile.Type.Folder;
            }

            fileName = fileName.toLowerCase();

            for (ModelFileFormat fileFormat : ModelMediaFile.supportedMediaFormats) {
                if (fileName.endsWith(fileFormat.fileExtension)) {
                    return fileFormat.type;
                }
            }

            return null;
        };

        Function1<String, ImageContentInfo> getImageContentInfo = path -> {
            ExifInterface exif;

            try {
                exif = new ExifInterface(path);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                return null;
            }

            int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
            int height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (rotation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90; break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270; break;
                default:
                    rotation = 0; break;
            }

            if (rotation == 90 || rotation == 270) {
                int tmp = width;
                width = height;
                height = tmp;
            }

            ImageContentInfo info = new ImageContentInfo();
            info.width = width;
            info.height = height;
            info.rotation = rotation;

            return info;
        };

        Function1<String, VideoContentInfo> getVideoContentInfo = path -> {
            String widthStr;
            String heightStr;
            String rotationStr;
            String durationStr;

            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(path);
                widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                rotationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                return null;
            }

            int width = 0;
            int height = 0;
            int rotation = 0;
            int duration = 0;

            if (widthStr != null) {
                width = Integer.parseInt(widthStr);
            }
            if (heightStr != null) {
                height = Integer.parseInt(heightStr);
            }
            if (rotationStr != null) {
                rotation = Integer.parseInt(rotationStr);
            }
            if (durationStr != null) {
                duration = Integer.parseInt(durationStr) / 1000; // millis to seconds
            }

            if (rotation == 90 || rotation == 270) {
                int tmp = width;
                width = height;
                height = tmp;
            }

            VideoContentInfo info = new VideoContentInfo();
            info.width = width;
            info.height = height;
            info.rotation = rotation;
            info.duration = duration;

            return info;
        };

        Function1<String, String> getFileExtension = fileName -> {
            int lastIndexOf = fileName.lastIndexOf(".");
            return lastIndexOf != -1 ? fileName.substring(lastIndexOf + 1) : null;
        };

        Function1<File, ModelMediaFile> getFileInfo = file -> {
            String name = file.getName();
            Boolean isDirectory = file.isDirectory();
            ModelMediaFile.Type type = getFileType.invoke(name, isDirectory);

            if (type == null) return null;

            String path = file.getAbsolutePath();
            Date createAt = getFileCreationTime.invoke(file);
            Date updatedAt = new Date(file.lastModified());

            if (isDirectory) {
                return new ModelMediaFile(
                        name,
                        path,
                        type,
                        createAt,
                        updatedAt,
                        null,
                        FOLDER_PLACEHOLDER.getWidth(),
                        FOLDER_PLACEHOLDER.getHeight(),
                        0,
                        null,
                        null
                );
            }

            Long weight = file.length();
            String extension = getFileExtension.invoke(name);

            int width = -1, height = -1, rotation = -1, duration = -1;

            if (type == ModelMediaFile.Type.Image) {
                ImageContentInfo info = getImageContentInfo.invoke(path);
                width = info.width;
                height = info.height;
                rotation = info.rotation;
            }

            if (type == ModelMediaFile.Type.Video) {
                VideoContentInfo info = getVideoContentInfo.invoke(path);
                width = info.width;
                height = info.height;
                rotation = info.rotation;
                duration = info.duration;
            }

            return new ModelMediaFile(
                    name,
                    path,
                    type,
                    createAt,
                    updatedAt,
                    weight,
                    width,
                    height,
                    rotation,
                    extension,
                    duration
            );
        };

        Function1<ModelStorage, List<ModelMediaFile>> getStorageFiles = storage -> {
            List<ModelMediaFile> files = new ArrayList<>();

            File root = new File(storage.path);
            File[] dirFiles = root.listFiles();

            if (dirFiles == null) {
                return files;
            }

            for (File file : dirFiles) {
                ModelMediaFile item = getFileInfo.invoke(file);
                if (item != null) {
                    files.add(item);
                }
            }

            return files;
        };

        List<ModelStorage> storages = getAllStorages();
        List<ModelMediaFile> files = new ArrayList<>();

        for (ModelStorage storage : storages) {
            List<ModelMediaFile> storageFiles = getStorageFiles.invoke(storage);
            files.addAll(storageFiles);
        }

        return files;
    }
    static int counter = 0;
    @Override
    public Bitmap getFilePreview(ModelMediaFile item) {

        final Size TARGET_PREVIEW_RESOLUTION = new Size(250, 250);
        final int VIDEO_PREVIEW_TIMING = 0;

        Function1<ModelMediaFile, Integer> calcInSampleSize = _item -> {

            final int reqWidth = TARGET_PREVIEW_RESOLUTION.getWidth();
            final int reqHeight = TARGET_PREVIEW_RESOLUTION.getHeight();
            int width = _item.width;
            int height = _item.height;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        };

        Function2<Bitmap, Integer, Bitmap> rotateBitmap = (source, angle) -> {
            if (source == null || angle == 0) return source;
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        };

        Function1<ModelMediaFile, Bitmap> getImagePreview = _item -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calcInSampleSize.invoke(_item);
            options.inPreferredConfig = android.graphics.Bitmap.Config.RGB_565;
            Bitmap result = BitmapFactory.decodeFile(item.path, options);
            return rotateBitmap.invoke(result, _item.rotation);
        };

        Function1<ModelMediaFile, Bitmap> getVideoPreview = _item -> {
            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(_item.path);
                return retriever.getScaledFrameAtTime(
                        VIDEO_PREVIEW_TIMING,
                        android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                        TARGET_PREVIEW_RESOLUTION.getWidth(),
                        TARGET_PREVIEW_RESOLUTION.getHeight());
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                return null;
            }
        };

        if (item.type == ModelMediaFile.Type.Image) {
            Bitmap bitmap = getImagePreview.invoke(item);
            Log.i(LOG_TAG, "Загрузка превью изображения " + (++counter) + " | " + bitmap.getWidth() + " | " + bitmap.getHeight() + " | " + item.name);
            return bitmap;
        }

        if (item.type == ModelMediaFile.Type.Video) {
            Bitmap bitmap = getVideoPreview.invoke(item);
            Log.i(LOG_TAG, "Загрузка превью видео " + (++counter) + " | " + bitmap.getWidth() + " | " + bitmap.getHeight() + " | " + item.name);
            return bitmap;
        }

        if (item.type == ModelMediaFile.Type.Folder) {
            return FOLDER_PLACEHOLDER;
        }

        return null;
    }

    @Override
    public List<ModelStorage> getAllStorages() {
        List<ModelStorage> storages = new ArrayList<>();

        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();

        Function1<StorageVolume, ModelStorage> getStorageInfo = volume -> {
            File storageDir = volume.getDirectory();
            String path = storageDir.getAbsolutePath();

            String name = String.format(context.getResources().getString(R.string.format_name_storage_name),
                    volume.getDescription(context),
                    path);

            ModelStorage.Type type =
                    volume.isPrimary()
                    ? ModelStorage.Type.Primary
                    : volume.isRemovable()
                    ? ModelStorage.Type.Removable
                    : ModelStorage.Type.Else;

            return new ModelStorage(
                    name,
                    path,
                    type
            );
        };

        for (StorageVolume volume : storageVolumes) {
            storages.add(getStorageInfo.invoke(volume));
        }

        return storages;
    }
}