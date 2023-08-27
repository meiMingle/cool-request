package com.hxl.plugin.springboot.invoke.utils.file;

import com.hxl.plugin.springboot.invoke.Constant;
import com.hxl.plugin.springboot.invoke.utils.ClassResourceUtils;

import java.net.URL;

public class WindowFileChooser  extends BasicFileChooser{
    @Override
    public String getFile() {
        loadSo();
        return NativeWindowDialogUtils.openFileSelectDialog();
    }
    private void loadSo(){
        URL resource = NativeWindowDialogUtils.class.getResource(Constant.CLASSPATH_WINDOW_SO_LIB_PATH);
        ClassResourceUtils.copyTo(resource, Constant.CONFIG_SO_PATH.toString());
        System.load(Constant.CONFIG_SO_PATH.toString());
    }
}