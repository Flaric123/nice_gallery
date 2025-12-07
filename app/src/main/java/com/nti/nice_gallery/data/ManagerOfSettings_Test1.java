package com.nti.nice_gallery.data;

import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelScanList;

import java.util.List;

public class ManagerOfSettings_Test1 implements IManagerOfSettings{

    private List<ModelScanList>  scanList;
    private ModelFilters filters;

    @Override
    public List<ModelScanList> getScanList() {
        return scanList;
    }

    @Override
    public void saveScanList(List<ModelScanList> scanList) {
        this.scanList = scanList;
    }

    @Override
    public ModelFilters getFilters() {
        return filters;
    }

    @Override
    public void saveFilters(ModelFilters filters) {
        this.filters = filters;
    }
}
