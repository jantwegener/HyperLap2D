/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;
import games.rednblack.h2d.common.MsgAPI;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogListener;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.data.manager.PreferencesManager;
import games.rednblack.editor.proxy.CommandManager;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SceneDataManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.view.stage.Sandbox;
import org.apache.commons.io.filefilter.SuffixFileFilter;

/**
 * Created by sargis on 3/25/15.
 */
public class HyperLap2DMenuBarMediator extends SimpleMediator<HyperLap2DMenuBar> {
    private static final String TAG = HyperLap2DMenuBarMediator.class.getCanonicalName();
    public static final String NAME = TAG;
    private ProjectManager projectManager;

    public HyperLap2DMenuBarMediator() {
        super(NAME, new HyperLap2DMenuBar());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        projectManager = facade.retrieveProxy(ProjectManager.NAME);
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                //FILE
                FileMenu.NEW_PROJECT,
                FileMenu.OPEN_PROJECT,
                FileMenu.SAVE_PROJECT,
                FileMenu.IMPORT_TO_LIBRARY,
                FileMenu.EXPORT,
                FileMenu.EXPORT_SETTINGS,
                FileMenu.RECENT_PROJECTS,
                FileMenu.CLEAR_RECENTS,
                FileMenu.EXIT,
                FileMenu.NEW_SCENE,
                FileMenu.SELECT_SCENE,
                FileMenu.DELETE_CURRENT_SCENE,
                //EDIT
                EditMenu.CUT,
                EditMenu.COPY,
                EditMenu.PASTE,
                EditMenu.UNDO,
                EditMenu.REDO,
                //General
                ProjectManager.PROJECT_OPENED,
                HyperLap2DMenuBar.RECENT_LIST_MODIFIED
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        String type = notification.getType();

        if (notification.getName().equals(HyperLap2DMenuBar.RECENT_LIST_MODIFIED)) {
            PreferencesManager prefs = PreferencesManager.getInstance();
            viewComponent.reInitRecent(prefs.getRecentHistory());
        }

        if (type == null) {
            handleGeneralNotification(notification);
            return;
        }
        switch (type) {
            case FileMenu.FILE_MENU:
                handleFileMenuNotification(notification);
                break;
            case EditMenu.EDIT_MENU:
                handleEditMenuNotification(notification);
                break;
            default:
                break;
        }
    }

    private void handleGeneralNotification(Notification notification) {
        switch (notification.getName()) {
            case ProjectManager.PROJECT_OPENED:
                onProjectOpened();
                break;
        }
    }

    private void handleEditMenuNotification(Notification notification) {
        Sandbox sandbox = Sandbox.getInstance();
        switch (notification.getName()) {
            case EditMenu.CUT:
                facade.sendNotification(MsgAPI.ACTION_CUT);
                break;
            case EditMenu.COPY:
                facade.sendNotification(MsgAPI.ACTION_COPY);
                break;
            case EditMenu.PASTE:
                facade.sendNotification(MsgAPI.ACTION_PASTE);
                break;
            case EditMenu.UNDO:
                CommandManager commandManager = facade.retrieveProxy(CommandManager.NAME);
                commandManager.undoCommand();
                break;
            case EditMenu.REDO:
                commandManager = facade.retrieveProxy(CommandManager.NAME);
                commandManager.redoCommand();
                break;
        }
    }

    private void handleFileMenuNotification(Notification notification) {
        Sandbox sandbox = Sandbox.getInstance();
        switch (notification.getName()) {
            case FileMenu.NEW_PROJECT:
                break;
            case FileMenu.OPEN_PROJECT:
                showOpenProject();
                break;
            case FileMenu.SAVE_PROJECT:
                SceneVO vo = sandbox.sceneVoFromItems();
                projectManager.saveCurrentProject(vo);
                break;
            case FileMenu.IMPORT_TO_LIBRARY:
                //showDialog("showImportDialog");
                break;
            case FileMenu.RECENT_PROJECTS:
                recentProjectItemClicked(notification.getBody());
                //showDialog("showImportDialog");
                break;
            case FileMenu.CLEAR_RECENTS:
                clearRecents();
                break;
            case FileMenu.EXPORT:
                facade.sendNotification(MsgAPI.ACTION_EXPORT_PROJECT);
                break;
            case FileMenu.EXPORT_SETTINGS:
                //showDialog("showExportDialog");
                break;
            case FileMenu.EXIT:
                facade.sendNotification(MsgAPI.APP_EXIT);
                break;
            case FileMenu.NEW_SCENE:
                Dialogs.showInputDialog(sandbox.getUIStage(), "Create New Scene", "Scene Name : ", new InputDialogListener() {
                    @Override
                    public void finished(String input) {
                        if (input == null || input.equals("")) {
                            return;
                        }
                        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
                        sceneDataManager.createNewScene(input);
                        sandbox.loadScene(input);
                        onScenesChanged();
                    }

                    @Override
                    public void canceled() {

                    }
                });
                break;
            case FileMenu.SELECT_SCENE:
                sceneMenuItemClicked(notification.getBody());
                break;
            case FileMenu.DELETE_CURRENT_SCENE:
                Dialogs.showConfirmDialog(sandbox.getUIStage(),
                        "Delete Scene", "Do you realy want to delete '" + projectManager.currentProjectVO.lastOpenScene + "' scene?",
                        new String[]{"Delete", "Cancel"}, new Integer[]{0, 1}, result -> {
                            if (result == 0) {
                                SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
                                sceneDataManager.deleteCurrentScene();
                                sandbox.loadScene("MainScene");
                                onScenesChanged();
                            }
                        });
                break;
        }
    }

    private void onScenesChanged() {
        viewComponent.reInitScenes(projectManager.currentProjectInfoVO.scenes);
    }

    private void onProjectOpened() {
        viewComponent.reInitScenes(projectManager.currentProjectInfoVO.scenes);
        viewComponent.setProjectOpen(true);
    }

    public void showOpenProject() {
        Sandbox sandbox = Sandbox.getInstance();
        //chooser creation
        FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);

        FileTypeFilter typeFilter = new FileTypeFilter(false);
        typeFilter.addRule("HyperLap2D Project (*.pit)", "pit");
        fileChooser.setFileTypeFilter(typeFilter);

        fileChooser.setMultiSelectionEnabled(false);

        FileHandle workspacePath = (projectManager.getWorkspacePath() == null || !projectManager.getWorkspacePath().exists()) ?
                Gdx.files.absolute(System.getProperty("user.home")) : projectManager.getWorkspacePath();
        fileChooser.setDirectory(workspacePath);

        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected(Array<FileHandle> files) {
                String path = files.first().file().getAbsolutePath();
                if (path.length() > 0) {
                    projectManager.openProjectFromPath(path);
                }
            }
        });
        sandbox.getUIStage().addActor(fileChooser.fadeIn());
    }

    public void recentProjectItemClicked(String path) {
        PreferencesManager prefs = PreferencesManager.getInstance();
        prefs.buildRecentHistory();
        prefs.pushHistory(path);
        Sandbox sandbox = Sandbox.getInstance();
        projectManager.openProjectFromPath(path);
    }

    public void clearRecents() {
        PreferencesManager prefs = PreferencesManager.getInstance();
        prefs.clearHistory();
        viewComponent.reInitRecent(prefs.getRecentHistory());
    }

    public void sceneMenuItemClicked(String sceneName) {
        Sandbox sandbox = Sandbox.getInstance();
        sandbox.loadScene(sceneName);
    }

    public void addMenuItem(String menu, String subMenuName, String notificationName) {
        viewComponent.addMenuItem(menu, subMenuName, notificationName);
    }
}