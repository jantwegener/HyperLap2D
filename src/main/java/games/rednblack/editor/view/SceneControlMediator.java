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

package games.rednblack.editor.view;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.SceneVO;

/**
 * Mediates scene communication between editor and current runtime
 *
 * @author azakhary
 */
public class SceneControlMediator {

	private final HyperLap2DFacade facade;
	private final ProjectManager projectManager;
	/**
	 * main holder of the scene
	 */
	public SceneLoader sceneLoader;

	/**
	 * runtime essentials
	 */
	// private Essentials essentials;

	/**
	 * current scene tools
	 */
	private SceneVO currentSceneVo;

	/**
	 * tools object of the root element of the scene
	 */
	private CompositeItemVO rootSceneVO;

	/**
	 * current scene rendering item
	 */
	private Entity currentScene;

	public SceneControlMediator(SceneLoader sceneLoader) {
		this.sceneLoader = sceneLoader;
		// this.essentials = essentials;
		facade = HyperLap2DFacade.getInstance();
		projectManager = facade.retrieveProxy(ProjectManager.NAME);
	}

	public ProjectInfoVO getProjectInfoVO() {
		return sceneLoader.getRm().getProjectVO();
	}

	public void initScene(String sceneName) {
		ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
		ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);

		ScreenViewport viewport = new ScreenViewport();
		// Yey to whoever made this method
		viewport.setUnitsPerPixel(1f/resourceManager.getProjectVO().pixelToWorld);

		currentSceneVo = sceneLoader.loadScene(sceneName, viewport);
		// TODO: this is now in sceneLoaader but probably will be changed
		// essentials.world = new World(new
		// Vector2(currentSceneVo.physicsPropertiesVO.gravityX,
		// currentSceneVo.physicsPropertiesVO.gravityY), true);
		// essentials.rayHandler.setWorld(essentials.world);

		rootSceneVO = new CompositeItemVO(currentSceneVo.composite);
	}

	public void updateAmbientLights() {
		sceneLoader.setAmbientInfo(sceneLoader.getSceneVO());
	}

	public CompositeItemVO getRootSceneVO() {
		return rootSceneVO;
	}

	public Entity getCurrentScene() {
		return currentScene;
	}

	public SceneVO getCurrentSceneVO() {
		return currentSceneVo;
	}
	
	public Entity getRootEntity() {
		return sceneLoader.rootEntity;
	}

//	public CompositeItem getCompositeElement(CompositeItemVO vo) {
//		return sceneLoader.getCompositeElement(vo);
//	}

}