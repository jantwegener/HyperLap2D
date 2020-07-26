package games.rednblack.editor.view.ui.dialog;

import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.view.menu.FileMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.settings.GeneralSettings;
import games.rednblack.editor.view.ui.settings.SandboxSettings;

public class SettingsDialogMediator extends SimpleMediator<SettingsDialog> {

    private static final String TAG = SettingsDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public SettingsDialogMediator() {
        super(NAME, new SettingsDialog());
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                FileMenu.SETTINGS,
                SettingsDialog.ADD_SETTINGS
        };
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();

        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);

        GeneralSettings generalSettings = new GeneralSettings();
        generalSettings.setSettings(settingsManager.editorConfigVO);
        viewComponent.addSettingsNode(generalSettings);

        SandboxSettings sandboxSettings = new SandboxSettings();
        sandboxSettings.setSettings(settingsManager.editorConfigVO);
        viewComponent.addSettingsNode(sandboxSettings);
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case FileMenu.SETTINGS:
                viewComponent.show(uiStage);
                break;
            case SettingsDialog.ADD_SETTINGS:
                SettingsDialog.SettingsNodeValue<?> settings = notification.getBody();
                viewComponent.addSettingsNode(settings);
                break;
        }
    }
}