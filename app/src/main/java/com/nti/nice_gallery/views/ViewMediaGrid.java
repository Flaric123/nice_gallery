package com.nti.nice_gallery.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ReadOnlyList;
import com.nti.nice_gallery.utils.Convert;
import com.nti.nice_gallery.views.buttons.ButtonChoiceGridVariant;
import com.nti.nice_gallery.views.grid_items.GridItemLine;
import com.nti.nice_gallery.views.grid_items.GridItemQuilt;
import com.nti.nice_gallery.views.grid_items.GridItemSquare;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import kotlin.jvm.functions.Function1;

public class ViewMediaGrid extends ScrollView {

    private static final String LOG_TAG = "ViewMediaGrid";

    public enum GridVariant { List, ThreeColumns, SixColumns, Quilt }

    private LinearLayout container;
    private ViewInfo viewInfo;

    private ReadOnlyList<ModelMediaFile> items;
    private GridVariant gridVariant;

    private IManagerOfSettings managerOfSettings;

    private int renderedItemsCount = 0;
    private boolean renderingInProgress = false;

    public ViewMediaGrid(Context context) {
        super(context);
        init();
    }

    public ViewMediaGrid(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewMediaGrid(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        managerOfSettings = Domain.getManagerOfSettings(getContext());
        gridVariant = managerOfSettings.getGridVariant();

        LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        setOnScrollChangeListener(this::onScrollChange);

        container = new LinearLayout(getContext());
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(containerParams);
        container.setOrientation(LinearLayout.VERTICAL);
        int containerPaddingPx = new Convert(getContext()).dpToPx(4);
        container.setPadding(containerPaddingPx, 0, containerPaddingPx, 0);
        addView(container);

        viewInfo = new ViewInfo(getContext());
        viewInfo.setIconVisibility(false);
        viewInfo.setMessage(R.string.message_loading_in_progress);
        viewInfo.setProgressBarVisibility(true);

        updateGrid();
    }

    public ReadOnlyList<ModelMediaFile> getItems() {
        return items;
    }

    public void setItems(ReadOnlyList<ModelMediaFile> items) {
        this.items = items;
        updateGrid();
    }

    public GridVariant getGridVariant() {
        return gridVariant;
    }

    public void setGridVariant(GridVariant gridVariant) {
        this.gridVariant = gridVariant;
        updateGrid();
    }

    private void updateGrid() {
        if (items == null || items.isEmpty()) {
            noItemsUpdate();
            return;
        }

        renderedItemsCount = 0;
        renderingInProgress = false;

        container.removeAllViews();
        renderNextItems();
    }

    private void noItemsUpdate() {
        container.removeAllViews();
        ViewInfo info = new ViewInfo(getContext());
        info.setIcon(R.drawable.baseline_image_search_24);
        info.setIconVisibility(true);
        info.setMessage(R.string.message_no_items);
        info.setProgressBarVisibility(false);
        container.addView(info);
    }

    private void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        final int SCROLL_END_THRESHOLD_PX = 200;

        View lastChild = getChildAt(getChildCount() - 1);
        int diff = (lastChild.getBottom() - (getHeight() + getScrollY()));
        if (diff <= SCROLL_END_THRESHOLD_PX) {
            renderNextItems();
        }
    }

    private void renderNextItems() {
        final int RENDERING_STEP_ITEMS_COUNT = 25;

        if (this.renderingInProgress || this.renderedItemsCount == items.size()) {
            return;
        }

        this.renderingInProgress = true;
        container.addView(viewInfo);

        Supplier<LinearLayout> renderNextForListVariant = () -> {
            int from = renderedItemsCount;
            int to = Math.min(renderedItemsCount + RENDERING_STEP_ITEMS_COUNT, items.size());

            LinearLayout pageContainer = new LinearLayout(getContext());
            pageContainer.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            pageContainer.setOrientation(LinearLayout.VERTICAL);

            for (int i = from; i < to; i++) {
                ModelMediaFile item = items.get(i);
                GridItemLine itemView = new GridItemLine(getContext());
                itemView.setModel(item);
                pageContainer.addView(itemView);
            }

            this.renderedItemsCount = to;

            return pageContainer;
        };

        Function1<Integer, LinearLayout> renderNextForColumnsVariant = columnsCount -> {
            final int HIDE_ITEM_DATA_IF_COLUMNS_COUNT_MORE_THAN = 3;

            int from = renderedItemsCount;
            int to = Math.min(renderedItemsCount + RENDERING_STEP_ITEMS_COUNT, items.size());

            LinearLayout pageContainer = new LinearLayout(getContext());
            pageContainer.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            pageContainer.setOrientation(LinearLayout.VERTICAL);

            int itemsCount = 0;
            LinearLayout row = null;

            for (int i = from; i < to || (row != null && i < items.size()); i++) {
                if (row == null) {
                    row = new LinearLayout(getContext());
                    row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    row.setOrientation(LinearLayout.HORIZONTAL);
                }

                ModelMediaFile item = items.get(i);
                GridItemSquare itemView = new GridItemSquare(getContext());
                itemView.setIsInfoHidden(columnsCount > HIDE_ITEM_DATA_IF_COLUMNS_COUNT_MORE_THAN);
                itemView.setModel(item);
                row.addView(itemView);
                itemsCount++;

                if (row.getChildCount() == columnsCount) {
                    pageContainer.addView(row);
                    row = null;
                }
            }

            if (row != null) {
                pageContainer.addView(row);
            }

            this.renderedItemsCount += itemsCount;

            return pageContainer;
        };

        Supplier<LinearLayout> renderNextForQuiltVariant = () -> {
            final Size NO_SIZE_ITEM_RESOLUTION = new Size(960, 960);
            final int MIN_IMAGES_ROW_WIDTH_PX = 1920;
            final int CONTAINER_HORIZONTAL_PADDING_DP = 4;
            final int ITEM_MARGIN_DP = 4;
            final float MIN_ROW_WIDTH_TO_HEIGHT_RATIO = 1.5f;

            int from = renderedItemsCount;
            int to = Math.min(renderedItemsCount + RENDERING_STEP_ITEMS_COUNT, items.size());

            LinearLayout pageContainer = new LinearLayout(getContext());
            pageContainer.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            pageContainer.setOrientation(LinearLayout.VERTICAL);

            int displayWidth = getResources().getDisplayMetrics().widthPixels;
            Convert convert = new Convert(getContext());
            LinearLayout rowLayout = null;
            ArrayList<ModelMediaFile> rowItems = null;
            ArrayList<Float> rowWidths = null;
            ArrayList<Float> rowHeights = null;
            int itemsCount = 0;

            for (int j = from; j < to || (rowLayout != null && j < items.size()); j++) {
                if (rowLayout == null) {
                    rowLayout = new LinearLayout(getContext());
                    rowLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                    rowItems = new ArrayList<>();
                    rowWidths = new ArrayList<>();
                    rowHeights = new ArrayList<>();
                }

                ModelMediaFile item = items.get(j);

                rowItems.add(item);
                itemsCount++;

                int itemWidth, itemHeight;

                if (item.width != null && item.width > 0 && item.height != null && item.height > 0) {
                    itemWidth = item.width;
                    itemHeight = item.height;
                } else {
                    itemWidth = NO_SIZE_ITEM_RESOLUTION.getWidth();
                    itemHeight = NO_SIZE_ITEM_RESOLUTION.getHeight();
                }

                rowWidths.add((float)itemWidth);
                rowHeights.add((float)itemHeight);

                float sumWidth = rowWidths.stream().reduce(0f, Float::sum);
                float maxHeight = rowHeights.stream().max(Float::compareTo).get();
                float avgHeight = rowHeights.stream().reduce(0f, Float::sum) / rowHeights.size();
                boolean isItemLast = items.indexOf(item) == items.size() - 1;

                if ((sumWidth < MIN_IMAGES_ROW_WIDTH_PX || sumWidth / avgHeight < MIN_ROW_WIDTH_TO_HEIGHT_RATIO) && !isItemLast) {
                    continue;
                }

                for (int i = 0; i < rowWidths.size(); i++) {
                    rowWidths.set(i, rowWidths.get(i) * maxHeight / rowHeights.get(i));
                }

                int displayWidthWithoutPaddings = displayWidth - convert.dpToPx(2 * CONTAINER_HORIZONTAL_PADDING_DP + 2 * rowItems.size() * ITEM_MARGIN_DP);
                sumWidth = rowWidths.stream().reduce(0f, Float::sum);
                int itemMarginsPx = convert.dpToPx(2 * ITEM_MARGIN_DP);

                for (int i = 0; i < rowWidths.size(); i++) {
                    rowWidths.set(i, rowWidths.get(i) * displayWidthWithoutPaddings / sumWidth + itemMarginsPx);
                }

                for (int i = 0; i < rowWidths.size(); i++) {
                    GridItemQuilt itemView = new GridItemQuilt(getContext(), rowWidths.get(i));
                    itemView.setModel(rowItems.get(i));
                    rowLayout.addView(itemView);
                }

                pageContainer.addView(rowLayout);

                rowLayout = null;
                rowItems = null;
                rowWidths = null;
                rowHeights = null;
            }

            this.renderedItemsCount += itemsCount;

            return pageContainer;
        };

        Runnable checkIsContainerFullAndLoadNextIfNot = () -> {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (!canScrollVertically(1)) {
                        renderNextItems();
                    }
                }
            });
        };

        Runnable loadOnePage = () -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                LinearLayout pageContainer = null;

                switch (gridVariant) {
                    case List: pageContainer = renderNextForListVariant.get(); break;
                    case ThreeColumns: pageContainer = renderNextForColumnsVariant.invoke(3); break;
                    case SixColumns: pageContainer = renderNextForColumnsVariant.invoke(6); break;
                    case Quilt: pageContainer = renderNextForQuiltVariant.get(); break;
                }

                final LinearLayout pageContainerFinal = pageContainer;

                post(() -> {
                    container.removeView(viewInfo);
                    container.addView(pageContainerFinal);
                    this.renderingInProgress = false;
                    checkIsContainerFullAndLoadNextIfNot.run();
                    executor.shutdown();
                });
            });
        };

        loadOnePage.run();
    }
}
