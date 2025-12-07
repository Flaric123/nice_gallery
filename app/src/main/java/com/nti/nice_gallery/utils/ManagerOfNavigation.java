package com.nti.nice_gallery.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class ManagerOfNavigation {

    private final Context context;

    public ManagerOfNavigation(Context context) {
        this.context = context;
    }

    public void navigate(Activity from, Class to, boolean replace) {
        Intent intent = new Intent(from, to);
        from.startActivity(intent);
        if (replace) {
            from.finish();
        }
    }

    public void navigate(Activity from, Class to) {
        navigate(from, to, false);
    }
}
