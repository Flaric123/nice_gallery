package com.nti.nice_gallery.data;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Size;

import com.nti.nice_gallery.models.ModelMediaTreeItem;
import com.nti.nice_gallery.models.ModelStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ManagerOfFiles implements IManagerOfFiles {

    private final Context context;
    private final ContentResolver contentResolver;

    public ManagerOfFiles(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    @Override
    public List<ModelMediaTreeItem> getAllFiles() {
        List<ModelMediaTreeItem> mediaList = new ArrayList<>();

        // Получаем изображения
        mediaList.addAll(getMediaFromMediaStore(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ModelMediaTreeItem.Type.Image));

        // Получаем видео
        mediaList.addAll(getMediaFromMediaStore(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                ModelMediaTreeItem.Type.Video));

        return mediaList;
    }

    @Override
    public Bitmap getItemPreviewAsBitmap(ModelMediaTreeItem item) {
        return null;
    }

    @Override
    public List<ModelStorage> getAllStorages() {
        return Collections.emptyList();
    }

    @SuppressLint("Range")
    private List<ModelMediaTreeItem> getMediaFromMediaStore(Uri contentUri, ModelMediaTreeItem.Type type) {
        List<ModelMediaTreeItem> mediaList = new ArrayList<>();

        // Колонки для запроса
        String[] projection;
        if (type == ModelMediaTreeItem.Type.Image) {
            projection = new String[] {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.DATE_MODIFIED,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT,
                    MediaStore.Images.Media.MIME_TYPE
            };
        } else {
            projection = new String[] {
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_ADDED,
                    MediaStore.Video.Media.DATE_MODIFIED,
                    MediaStore.Video.Media.WIDTH,
                    MediaStore.Video.Media.HEIGHT,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.MIME_TYPE
            };
        }

        // Сортировка по дате добавления (новые первыми)
        String sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";

        try (Cursor cursor = contentResolver.query(
                contentUri,
                projection,
                null,
                null,
                sortOrder
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
                int addedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED);
                int modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED);
                int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH);
                int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT);
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);

                int durationColumn = -1;
                if (type == ModelMediaTreeItem.Type.Video) {
                    durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                }

                do {
                    try {
                        String path = cursor.getString(pathColumn);
                        if (path == null) continue;

                        String name = cursor.getString(nameColumn);
                        Long size = cursor.getLong(sizeColumn);
                        Long dateAdded = cursor.getLong(addedColumn) * 1000L; // конвертируем в миллисекунды
                        Long dateModified = cursor.getLong(modifiedColumn) * 1000L;
                        Integer width = cursor.getInt(widthColumn);
                        Integer height = cursor.getInt(heightColumn);
                        String mimeType = cursor.getString(mimeTypeColumn);

                        // Получаем расширение файла
                        String extension = getExtensionFromMimeType(mimeType, name);

                        // Проверяем поддерживаемые форматы
                        if (!isSupportedFormat(extension, type)) {
                            continue;
                        }

                        // Создаем объект Resolution
                        Size resolution = (width > 0 && height > 0) ?
                                new Size(width, height) : null;

                        // Для видео получаем длительность
                        Integer duration = null;
                        if (type == ModelMediaTreeItem.Type.Video && durationColumn != -1) {
                            long durationMs = cursor.getLong(durationColumn);
                            duration = (int) (durationMs / 1000); // в секундах
                        }

                        ModelMediaTreeItem mediaItem = new ModelMediaTreeItem(
                                name,
                                path,
                                type,
                                size,
                                new Date(dateAdded),
                                new Date(dateModified),
                                resolution,
                                extension,
                                duration
                        );

                        mediaList.add(mediaItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mediaList;
    }

    private String getExtensionFromMimeType(String mimeType, String fileName) {
        if (mimeType != null) {
            if (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg")) {
                return "jpg";
            } else if (mimeType.equals("image/png")) {
                return "png";
            } else if (mimeType.equals("image/bmp")) {
                return "bmp";
            } else if (mimeType.equals("video/mp4")) {
                return "mp4";
            } else if (mimeType.equals("video/x-ms-wmv")) {
                return "wmv";
            }
        }

        // Если MIME type неизвестен, получаем из имени файла
        return getFileExtension(fileName);
    }

    private boolean isSupportedFormat(String extension, ModelMediaTreeItem.Type type) {
        extension = extension.toLowerCase();

        if (type == ModelMediaTreeItem.Type.Image) {
            for (String supported : ModelMediaTreeItem.supportedImageExtensions) {
                if (supported.equalsIgnoreCase(extension)) {
                    return true;
                }
            }
        } else if (type == ModelMediaTreeItem.Type.Video) {
            for (String supported : ModelMediaTreeItem.supportedVideoExtensions) {
                if (supported.equalsIgnoreCase(extension)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }


}
