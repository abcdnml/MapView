package com.aaa.lib.map3d;

import com.aaa.lib.map3d.model.Model;

public interface ModelListener {
    void onModelAdd(Model model);

    void onModelRemove(Model model);
}
