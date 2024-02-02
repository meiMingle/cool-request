package com.cool.request.view;

import com.cool.request.common.bean.BeanInvokeSetting;
import com.cool.request.common.constant.CoolRequestIdeaTopic;
import com.cool.request.lib.springmvc.RequestCache;
import com.cool.request.utils.ResourceBundleUtils;
import com.intellij.openapi.application.ApplicationManager;

import javax.swing.*;

public class ReflexSettingUIPanel {
    private JRadioButton sourceButton;
    private JRadioButton proxyButton;
    private JCheckBox interceptor;
    private JLabel interceptorDesc;
    private JPanel root;

    public ReflexSettingUIPanel() {
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(CoolRequestIdeaTopic.COOL_REQUEST_SETTING_CHANGE, (CoolRequestIdeaTopic.BaseListener) () -> loadText());
        loadText();
    }

    public JPanel getRoot() {
        return root;
    }

    private void loadText() {
        proxyButton.setText(ResourceBundleUtils.getString("proxy.object"));
        sourceButton.setText(ResourceBundleUtils.getString("source.object"));
        interceptor.setText(ResourceBundleUtils.getString("use.interceptor"));
        interceptorDesc.setText(ResourceBundleUtils.getString("use.interceptor.desc"));

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(proxyButton);
        buttonGroup.add(sourceButton);

        proxyButton.setSelected(true);
    }

    public BeanInvokeSetting getBeanInvokeSetting() {
        BeanInvokeSetting beanInvokeSetting = new BeanInvokeSetting();
        beanInvokeSetting.setUseInterceptor(interceptor.isSelected());
        beanInvokeSetting.setUseProxy(proxyButton.isSelected());
        return beanInvokeSetting;
    }

    public void setRequestInfo(RequestCache requestCache) {
        proxyButton.setSelected(!requestCache.isUseProxy());
        sourceButton.setSelected(requestCache.isUseInterceptor());
        interceptor.setSelected(requestCache.isUseInterceptor());
    }
}


