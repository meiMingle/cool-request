package com.cool.request.view.component;

import com.cool.request.action.actions.*;
import com.cool.request.common.constant.CoolRequestConfigConstant;
import com.cool.request.common.constant.CoolRequestIdeaTopic;
import com.cool.request.common.service.ProjectViewSingleton;
import com.cool.request.common.state.SettingPersistentState;
import com.cool.request.common.state.SettingsState;
import com.cool.request.utils.NavigationUtils;
import com.cool.request.utils.WebBrowseUtils;
import com.cool.request.view.ToolComponentPage;
import com.cool.request.view.dialog.SettingDialog;
import com.cool.request.view.events.IToolBarViewEvents;
import com.cool.request.view.main.MainTopTreeView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBSplitter;

import javax.swing.*;
import java.awt.*;


/**
 * Main View
 */
public class ApiToolPage extends SimpleToolWindowPanel implements IToolBarViewEvents, ToolComponentPage {
    public static final String PAGE_NAME = "Api";
    private final DefaultActionGroup menuGroup = new DefaultActionGroup();
    private final JBSplitter jbSplitter = new JBSplitter(true, "", 0.5f);
    private final MainTopTreeView mainTopTreeView;
    private MainBottomHTTPContainer mainBottomHTTPContainer;
    private final Project project;
    private boolean showUpdateMenu = false;
    private boolean createMainBottomHTTPContainer;

    public static ApiToolPage getInstance(Project project) {
        return ProjectViewSingleton.getInstance(project).createAndApiToolPage();
    }

    public ApiToolPage(Project project) {
        super(true);
        this.project = project;
        this.createMainBottomHTTPContainer = createMainBottomHTTPContainer;
        this.project.getUserData(CoolRequestConfigConstant.CoolRequestKey).attachWindowView(this);
        setLayout(new BorderLayout());
        this.mainTopTreeView = new MainTopTreeView(project);

        SettingsState state = SettingPersistentState.getInstance().getState();
        if (state.mergeApiAndRequest) {
            this.mainBottomHTTPContainer = ProjectViewSingleton.getInstance(project).createAndGetMainBottomHTTPContainer();
        }

        ApplicationManager.getApplication().getMessageBus()
                .connect().subscribe(CoolRequestIdeaTopic.COOL_REQUEST_SETTING_CHANGE, (CoolRequestIdeaTopic.BaseListener) () -> {
                    SettingsState state1 = SettingPersistentState.getInstance().getState();
                    if (state1.mergeApiAndRequest && jbSplitter.getSecondComponent() == null) {
                        if (mainBottomHTTPContainer == null) {
                            mainBottomHTTPContainer = ProjectViewSingleton.getInstance(project).createAndGetMainBottomHTTPContainer();
                        }
                        jbSplitter.setSecondComponent(mainBottomHTTPContainer);
                    }
                    if (!state1.mergeApiAndRequest) {
                        jbSplitter.setSecondComponent(null);
                    }
                });
        initUI();
        // 刷新视图
        DumbService.getInstance(project).smartInvokeLater(() -> NavigationUtils.staticRefreshView(project));

    }

    public void initUI() {
        initToolBar();
        project.getMessageBus().connect().subscribe(CoolRequestIdeaTopic.CHANGE_LAYOUT,
                (CoolRequestIdeaTopic.BaseListener) () -> {
                    boolean orientation = jbSplitter.getOrientation();
                    jbSplitter.setOrientation(!orientation);
                });

        jbSplitter.setFirstComponent(mainTopTreeView);
        if (mainBottomHTTPContainer != null) {
            jbSplitter.setSecondComponent(mainBottomHTTPContainer);
        }
        this.add(jbSplitter, BorderLayout.CENTER);
    }

    private void initToolBar() {

        menuGroup.add(new RefreshAction(project, this));
        menuGroup.add(new CleanAction(project, this));
        menuGroup.addSeparator();

        menuGroup.add(new CollapseAction(project));
        menuGroup.add(new ExpandAction(project));
        menuGroup.add(new FindAction(project));
        menuGroup.addSeparator();

        menuGroup.add(new FloatWindowsAnAction(project));
        if (createMainBottomHTTPContainer) {
            menuGroup.add(new ChangeMainLayoutAnAction(project));
        }
        menuGroup.addSeparator();

        menuGroup.add(new BugAction(project));
        menuGroup.add(new HelpAction(project, this));
        menuGroup.add(new ContactAnAction(project));
        menuGroup.addSeparator();

        menuGroup.add(new AboutAnAction(project));
        menuGroup.addSeparator();
        menuGroup.add(new PayAnAction(project));
//        menuGroup.addSeparator();
//        menuGroup.add(new SettingAction(project, this));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("bar", menuGroup, false);
        toolbar.setTargetComponent(this);
//
//        JPanel topBarJPanel = new JPanel(new BorderLayout());
//        toolbar.setTargetComponent(topBarJPanel);
//        ((ActionToolbar) toolbar.getComponent()).setOrientation(myVertical ? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL);
//
//        topBarJPanel.add(toolbar.getComponent(), BorderLayout.WEST);
//        topBarJPanel.add(new EnvironmentJPanel(), BorderLayout.EAST);
        setToolbar(toolbar.getComponent());

    }

    public void removeAllDynamicAnActions() {
        AnAction[] childActionsOrStubs = menuGroup.getChildActionsOrStubs();
        for (AnAction childActionsOrStub : childActionsOrStubs) {
            if (childActionsOrStub instanceof DynamicUrlAnAction) {
                menuGroup.remove(childActionsOrStub);
            }
        }
    }

    public void addNewDynamicAnAction(String title, String url, ImageIcon imageIcon) {
        AnAction[] childActionsOrStubs = menuGroup.getChildActionsOrStubs();
        for (AnAction childActionsOrStub : childActionsOrStubs) {
            if (childActionsOrStub.getTemplatePresentation().getText().equalsIgnoreCase(title)) {
                return;
            }
        }
        SwingUtilities.invokeLater(() -> menuGroup.add(new DynamicUrlAnAction(title, imageIcon, url)));
    }

    public void showUpdateMenu() {
        if (showUpdateMenu) {
            return;
        }
        showUpdateMenu = true;
        menuGroup.add(new UpdateAction(this));
    }


    @Override
    public void openSettingView() {
        SettingDialog.show(project);
    }

    @Override
    public void clearAllData() {
        mainTopTreeView.getProject().getMessageBus().syncPublisher(CoolRequestIdeaTopic.DELETE_ALL_DATA).onDelete();

    }

    @Override
    public void pluginHelp() {
        WebBrowseUtils.browse("https://plugin.houxinlin.com");
    }

    @Override
    public void refreshTree() {
//        List<ProjectStartupModel> springBootApplicationStartupModel = userProjectManager.getSpringBootApplicationStartupModel();
//        //删除可以通信的端口
//        Set<Integer> ports = new HashSet<>();
//        for (ProjectStartupModel projectStartupModel : springBootApplicationStartupModel) {
//            if (SocketUtils.canConnection(projectStartupModel.getPort())) {
//                ports.add(projectStartupModel.getProjectPort());
//            }
//        }
//        if (!ports.isEmpty()) {
//            this.clearTree();
//        }
//        userProjectManager.projectEndpointRefresh();
    }

    public MainBottomHTTPContainer getMainBottomHTTPContainer() {
        return mainBottomHTTPContainer;
    }

    public MainTopTreeView getMainTopTreeView() {
        return mainTopTreeView;
    }


    @Override
    public void setAttachData(Object object) {
        if (object == null) return;

    }

    @Override
    public String getPageId() {
        return PAGE_NAME;
    }
}