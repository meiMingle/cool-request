package com.hxl.plugin.springboot.invoke.view;

import com.hxl.plugin.springboot.invoke.action.actions.BaseAnAction;
import com.hxl.plugin.springboot.invoke.utils.ResourceBundleUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class BaseTableParamWithToolbar extends SimpleToolWindowPanel {
    private final DefaultActionGroup menuGroup = new DefaultActionGroup();

    public void addRow() {
    }

    public void removeRow() {
    }

    public void copyRow() {
    }

    public BaseTableParamWithToolbar(boolean showBar) {
        super(true);
    }

    protected void showToolBar() {
        menuGroup.add(new AddRowAnAction());
        menuGroup.add(new RemoveRowAnAction());
        menuGroup.add(new CopyRowAnAction());
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("bar", menuGroup, false);

        toolbar.setTargetComponent(this);
        ((ActionToolbar) toolbar.getComponent()).setOrientation(myVertical ? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL);
        setToolbar(toolbar.getComponent());
    }

    class AddRowAnAction extends BaseAnAction {
        public AddRowAnAction() {
            super(null, () -> "Add New Row", AllIcons.General.Add);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            addRow();
        }
    }

    class RemoveRowAnAction extends BaseAnAction {

        public RemoveRowAnAction() {
            super(null, () -> "Remove New Row", AllIcons.General.Remove);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            removeRow();
        }
    }

    class CopyRowAnAction extends BaseAnAction {
        public CopyRowAnAction() {
            // TODO: 2024/1/12 这个ui找不到 
            super(null, () -> "Copy Row", AllIcons.Actions.DynamicUsages);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            copyRow();
        }
    }
}