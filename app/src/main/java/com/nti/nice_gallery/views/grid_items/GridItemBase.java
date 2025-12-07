package com.nti.nice_gallery.views.grid_items;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;

public class GridItemBase extends FrameLayout {

    public GridItemBase(@NonNull Context context) {
        super(context);
        init();
    }

    public GridItemBase(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridItemBase(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnClickListener(null);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        super.setOnClickListener(v -> {
            final Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_down_and_back);
            v.startAnimation(animation);
            if (listener != null) {
                listener.onClick(v);
            }
        });
    }
}
