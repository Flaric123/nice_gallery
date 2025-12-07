package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;
import com.nti.nice_gallery.R;
import com.nti.nice_gallery.views.ViewMediaGrid;

import java.util.function.Consumer;

public class ButtonChoiceGridVariant extends ButtonBase {

    private final ViewMediaGrid.GridVariant[] variants = new ViewMediaGrid.GridVariant[] {
            ViewMediaGrid.GridVariant.List,
            ViewMediaGrid.GridVariant.ThreeColumns,
            ViewMediaGrid.GridVariant.SixColumns,
            ViewMediaGrid.GridVariant.Quilt
    };

    private final int[] icons = new int[] {
            R.drawable.baseline_view_list_24,
            R.drawable.baseline_view_module_24,
            R.drawable.baseline_view_compact_24,
            R.drawable.baseline_view_quilt_24
    };

    private int selectedVariant = 1;
    private Consumer<ViewMediaGrid.GridVariant> variantChangeListener;

    public ButtonChoiceGridVariant(Context context) {
        super(context);
        init();
    }

    public ButtonChoiceGridVariant(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonChoiceGridVariant(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(icons[selectedVariant]);
        setOnClickListener(v -> onClick());
    }

    public void setVariantChangeListener(Consumer<ViewMediaGrid.GridVariant> l) {
        variantChangeListener = l;
    }

    private void onClick() {
        selectedVariant++;

        if (selectedVariant > 3) {
            selectedVariant = 0;
        }

        setImageResource(icons[selectedVariant]);

        if (variantChangeListener != null) {
            variantChangeListener.accept(variants[selectedVariant]);
        }
    }
}
