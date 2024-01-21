package com.hxl.plugin.springboot.invoke.tool;

import com.hxl.plugin.springboot.invoke.utils.AnActionCallback;

import javax.swing.*;
import java.util.Objects;
import java.util.function.Supplier;

public class MainToolWindowsAction {
    private String name;
    private Icon icon;
    private ViewFactory viewFactory;

    private AnActionCallback callback;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MainToolWindowsAction that = (MainToolWindowsAction) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public MainToolWindowsAction(String name, Icon icon, ViewFactory viewFactory) {
        this.name = name;
        this.icon = icon;
        this.viewFactory = viewFactory;
    }
    public MainToolWindowsAction(String name, Icon icon, AnActionCallback callback) {
        this.name = name;
        this.icon = icon;
        this.callback = callback;
    }
    public AnActionCallback getCallback() {
        return callback;
    }

    public void setCallback(AnActionCallback callback) {
        this.callback = callback;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public void setViewFactory(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    interface ViewFactory extends Supplier<JComponent> {
    }
}
