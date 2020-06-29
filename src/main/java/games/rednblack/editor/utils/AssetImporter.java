package games.rednblack.editor.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.dialog.ImportDialog;
import games.rednblack.editor.view.ui.dialog.ImportDialogMediator;
import games.rednblack.editor.view.ui.widget.ProgressHandler;

import java.io.File;

public class AssetImporter {

    private static AssetImporter sInstance;
    private ImportDialogMediator.AssetsImportProgressHandler progressHandler;
    private ImportDialog viewComponent;

    public static AssetImporter getInstance() {
        if (sInstance == null) {
            sInstance = new AssetImporter();
        }
        return sInstance;
    }

    private AssetImporter() {

    }

    public void setProgressHandler(ImportDialogMediator.AssetsImportProgressHandler handler) {
        progressHandler = handler;
    }

    public void setViewComponent(ImportDialog component) {
        viewComponent = component;
    }

    public void postPathObtainAction(String[] paths) {
        int type = ImportUtils.getImportType(paths);

        if (type <= 0) {
            // error
            viewComponent.showError(type);
        } else {
            Array<FileHandle> files = getFilesFromPaths(paths);
            if (ImportUtils.getInstance().checkAssetExistence(type, files)) {
                Dialogs.showConfirmDialog(Sandbox.getInstance().getUIStage(),
                        "Duplicate file", "You have already an asset with this name, would you like to overwrite?",
                        new String[]{"Overwrite", "Cancel"}, new Integer[]{0, 1}, result -> {
                            if (result == 0) {
                                initImport(type, paths, false);
                            }
                        });
            } else {
                initImport(type, paths, false);
            }
        }
    }

    private void initImport(int type, String[] paths, boolean skipRepack) {
        boolean isMultiple = paths.length > 1 && type != ImportUtils.TYPE_ANIMATION_PNG_SEQUENCE;

        viewComponent.setImportingView(type, isMultiple);

        startImport(type, skipRepack, progressHandler, paths);
    }

    public void startImport(int importType, boolean skipRepack, ProgressHandler progressHandler, String... paths) {
        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);

        Array<FileHandle> files = getFilesFromPaths(paths);
        switch (importType) {
            case ImportUtils.TYPE_IMAGE:
                projectManager.importImagesIntoProject(files, progressHandler, skipRepack);
                break;
            case ImportUtils.TYPE_TEXTURE_ATLAS:
                projectManager.importAtlasesIntoProject(files, progressHandler);
                break;
            case ImportUtils.TYPE_PARTICLE_EFFECT:
                projectManager.importParticlesIntoProject(files, progressHandler, skipRepack);
                break;
            case ImportUtils.TYPE_SPRITER_ANIMATION:
                projectManager.importSpineAnimationsIntoProject(files, progressHandler);
                break;
            case ImportUtils.TYPE_SPINE_ANIMATION:
                projectManager.importSpineAnimationsIntoProject(files, progressHandler);
                break;
            case ImportUtils.TYPE_SPRITE_ANIMATION_ATLAS:
                projectManager.importSpriteAnimationsIntoProject(files, progressHandler);
                break;
            case ImportUtils.TYPE_ANIMATION_PNG_SEQUENCE:
                projectManager.importSpriteAnimationsIntoProject(files, progressHandler);
                break;
            case ImportUtils.TYPE_SHADER:
                projectManager.importShaderIntoProject(files, progressHandler);
                break;
            case ImportUtils.TYPE_HYPERLAP2D_INTERNAL_LIBRARY:
                projectManager.importItemLibraryIntoProject(files, progressHandler);
                break;
            case ImportUtils.TYPE_HYPERLAP2D_LIBRARY:
                projectManager.importHyperLapLibraryIntoProject(files, progressHandler);
                break;
        }

        // save before importing
        SceneVO vo = Sandbox.getInstance().sceneVoFromItems();
        projectManager.saveCurrentProject(vo);
        projectManager.setLastImportedPath(files.get(0).parent().path());
    }

    private  Array<FileHandle> getFilesFromPaths(String[] paths) {
        Array<FileHandle> files = new Array<>();
        for (String path : paths) {
            files.add(new FileHandle(new File(path)));
        }

        return files;
    }
}
