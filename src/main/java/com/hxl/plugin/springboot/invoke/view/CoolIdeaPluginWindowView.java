package com.hxl.plugin.springboot.invoke.view;

import com.hxl.plugin.springboot.invoke.Constant;
import com.hxl.plugin.springboot.invoke.IdeaTopic;
import com.hxl.plugin.springboot.invoke.action.ui.*;

import com.hxl.plugin.springboot.invoke.bean.EmptyEnvironment;
import com.hxl.plugin.springboot.invoke.bean.RequestEnvironment;
import com.hxl.plugin.springboot.invoke.model.ProjectStartupModel;
import com.hxl.plugin.springboot.invoke.net.PluginCommunication;
import com.hxl.plugin.springboot.invoke.net.RequestContextManager;
import com.hxl.plugin.springboot.invoke.state.CoolRequestEnvironmentPersistentComponent;
import com.hxl.plugin.springboot.invoke.state.project.ProjectConfigPersistentComponent;
import com.hxl.plugin.springboot.invoke.utils.*;
import com.hxl.plugin.springboot.invoke.view.dialog.SettingDialog;
import com.hxl.plugin.springboot.invoke.view.events.IToolBarViewEvents;
import com.hxl.plugin.springboot.invoke.view.main.MainBottomHTTPContainer;
import com.hxl.plugin.springboot.invoke.view.main.MainTopTreeView;
import com.intellij.openapi.actionSystem.*;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBSplitter;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Main View
 */
public class CoolIdeaPluginWindowView extends SimpleToolWindowPanel implements IToolBarViewEvents {
    private final MainTopTreeView mainTopTreeView;
    private final MainBottomHTTPContainer mainBottomHTTPContainer;
    private final UserProjectManager userProjectManager;
    private final JBSplitter jbSplitter = new JBSplitter(true, "", 0.35f);
    private final Project project;
    private final DefaultActionGroup menuGroup = new DefaultActionGroup();
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    private boolean showUpdateMenu = false;

    private static class EnvironmentRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof com.hxl.plugin.springboot.invoke.bean.RequestEnvironment) {
                value = ((com.hxl.plugin.springboot.invoke.bean.RequestEnvironment) value).getEnvironmentName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private class EnvironmentJPanel extends JPanel {

        private final ComboBox<RequestEnvironment> environmentJComboBox = new ComboBox<>();
        private final EmptyEnvironment emptyEnvironment = new EmptyEnvironment();

        public EnvironmentJPanel() {
            ApplicationManager.getApplication().getMessageBus().connect().subscribe(IdeaTopic.ENVIRONMENT_ADDED, (IdeaTopic.BaseListener) this::loadEnvironmentData);
            DefaultActionGroup actionGroup = new DefaultActionGroup();
            actionGroup.add(new EnvironmentAnAction());
            add(environmentJComboBox);
            environmentJComboBox.setRenderer(new EnvironmentRenderer());
            ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("MyToolbar", actionGroup, false);
            ((ActionToolbar) toolbar.getComponent()).setOrientation(SwingConstants.HORIZONTAL);

            toolbar.setTargetComponent(this);
            add(toolbar.getComponent());
            loadEnvironmentData();

            environmentJComboBox.addItemListener(e -> {
                RequestEnvironment selectedItem = (RequestEnvironment) environmentJComboBox.getSelectedItem();
                ProjectConfigPersistentComponent.getInstance().projectEnvironmentMap.put(project.getName(), selectedItem.getId());

                project.getMessageBus().syncPublisher(IdeaTopic.ENVIRONMENT_CHANGE).event();
            });
            project.putUserData(Constant.MainViewDataProvideKey, () -> ((RequestEnvironment) environmentJComboBox.getSelectedItem()));
        }

        private void loadEnvironmentData() {
            List<RequestEnvironment> environments = CoolRequestEnvironmentPersistentComponent.getInstance().environments;
            RequestEnvironment[] array = environments.toArray(new RequestEnvironment[]{});
            ComboBoxModel<RequestEnvironment> comboBoxModel = new DefaultComboBoxModel<>(array);
            environmentJComboBox.setModel(comboBoxModel);
            String envId = ProjectConfigPersistentComponent.getInstance().projectEnvironmentMap.getOrDefault(project.getName(), null);
            int index = -1;
            if (envId != null) {
                for (int i = 0; i < environments.size(); i++) {
                    if (envId.equals(environments.get(i).getId())) index = i;
                }
            }
            environmentJComboBox.addItem(emptyEnvironment);
            if (index == -1) {
                environmentJComboBox.setSelectedItem(emptyEnvironment);
            } else {
                environmentJComboBox.setSelectedIndex(index);
            }

        }
    }

    public CoolIdeaPluginWindowView(Project project) {
        super(true);
        this.project = project;
        setLayout(new BorderLayout());
        userProjectManager = new UserProjectManager(project);
        project.putUserData(Constant.UserProjectManagerKey, userProjectManager);
        project.putUserData(Constant.RequestContextManagerKey, new RequestContextManager());

        this.mainTopTreeView = new MainTopTreeView(project, this);
        this.mainBottomHTTPContainer = new MainBottomHTTPContainer(project, this);

        initUI();
        initSocket(project);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(this::pullNewVersion, 0, 1, TimeUnit.HOURS);
    }

    private void initToolBar() {
        menuGroup.add(new RefreshAction(project, this));
        menuGroup.add(new CleanAction(project, this));
        menuGroup.add(new SettingAction(project, this));
        menuGroup.add(new FloatWindowsAnAction(project));
        menuGroup.add(new ChangeMainLayoutAnAction(project));
        menuGroup.add(new BugAction(project));
        menuGroup.add(new HelpAction(project, this));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("bar", menuGroup, false);

        JPanel topBarJPanel = new JPanel(new BorderLayout());
        toolbar.setTargetComponent(topBarJPanel);
        ((ActionToolbar) toolbar.getComponent()).setOrientation(myVertical ? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL);

        topBarJPanel.add(toolbar.getComponent(), BorderLayout.WEST);
        topBarJPanel.add(new EnvironmentJPanel(), BorderLayout.EAST);
        setToolbar(topBarJPanel);

    }

    private void pullNewVersion() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder().get().url("http://plugin.houxinlin.com/api/version");
        okHttpClient.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    String string = response.body().string();
                    if (new Version(Constant.VERSION).compareTo(new Version(string)) < 0) {
                        SwingUtilities.invokeLater(() -> showUpdateMenu());
                    }
                }
            }
        });
    }

    private void showUpdateMenu() {
        if (showUpdateMenu) return;
        showUpdateMenu = true;
        menuGroup.add(new UpdateAction(this));
    }

    private void initSocket(Project project) {
        try {
            int port = SocketUtils.getSocketUtils().getPort(project);
            PluginCommunication pluginCommunication = new PluginCommunication(project, new MessageHandlers(userProjectManager));
            pluginCommunication.startServer(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initUI() {
        initToolBar();
        project.getMessageBus().connect().subscribe(IdeaTopic.CHANGE_LAYOUT,
                (IdeaTopic.BaseListener) () -> {
                    boolean orientation = jbSplitter.getOrientation();
                    jbSplitter.setOrientation(!orientation);
                });

        jbSplitter.setFirstComponent(mainTopTreeView);
        jbSplitter.setSecondComponent(mainBottomHTTPContainer);
        this.add(jbSplitter, BorderLayout.CENTER);
    }

    @Override
    public void openSettingView() {
        SettingDialog.show(project);
    }

    @Override
    public void clearTree() {
        mainTopTreeView.clear();
        userProjectManager.clear();
        mainTopTreeView.getProject().getMessageBus().syncPublisher(IdeaTopic.DELETE_ALL_DATA).onDelete();

    }

    @Override
    public void pluginHelp() {
        try {
            Desktop.getDesktop().browse(URI.create("http://plugin.houxinlin.com"));
        } catch (IOException e) {
        }
    }

    @Override
    public void refreshTree() {
        List<ProjectStartupModel> springBootApplicationStartupModel = userProjectManager.getSpringBootApplicationStartupModel();
        //删除可以通信的端口
        Set<Integer> ports = new HashSet<>();
        for (ProjectStartupModel projectStartupModel : springBootApplicationStartupModel) {
            if (SocketUtils.canConnection(projectStartupModel.getPort())) {
                ports.add(projectStartupModel.getProjectPort());
            }
        }
        if (!ports.isEmpty()) {
            this.clearTree();
        }
        userProjectManager.projectEndpointRefresh();
    }

    public MainBottomHTTPContainer getMainBottomHTTPContainer() {
        return mainBottomHTTPContainer;
    }

    public MainTopTreeView getMainTopTreeView() {
        return mainTopTreeView;
    }


}
