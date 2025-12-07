package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.utils.ManagerOfNotifications;

public class ButtonChoiceSortVariant extends ButtonBase {

    public ButtonChoiceSortVariant(Context context) {
        super(context);
        init();
    }

    public ButtonChoiceSortVariant(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonChoiceSortVariant(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.baseline_sort_24);
        setOnClickListener(v -> onClick());
    }

    private void onClick() {}
}
