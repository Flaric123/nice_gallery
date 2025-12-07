package com.nti.nice_gallery.utils;

import android.content.Context;

public class ManagerOfDialogs {

    private final Context context;

    public ManagerOfDialogs(Context context) {
        this.context = context;
    }

    public void showInfo(String title, String message) {}

    public void showYesNo(String title, String message, Runnable onYes, Runnable onNo) {}

}
