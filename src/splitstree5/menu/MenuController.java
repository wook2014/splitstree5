/*
 *  MenuController.java Copyright (C) 2019 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.menu;

import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.IMainWindow;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.SplashScreen;
import jloda.util.ProgramProperties;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.io.nexus.workflow.WorkflowNexusInput;
import splitstree5.main.MainWindow;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class MenuController {

    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu fileMenu;

    @FXML
    private MenuItem newMenuItem;

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private Menu openRecentMenu;

    @FXML
    private MenuItem inputEditorMenuItem;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem saveAsMenuItem;

    @FXML
    private MenuItem importMenuItem;

    @FXML
    private MenuItem importMultipleTreeFilesMenuItem;

    @FXML
    private MenuItem GroupIdenticalHaplotypesFilesMenuItem;

    @FXML
    private MenuItem exportMenuItem;

    @FXML
    private MenuItem exportImageMenuItem;

    @FXML
    private MenuItem replaceDataMenuItem;

    @FXML
    private Menu toolsMenu;

    @FXML
    private MenuItem exportWorkflowMenuItem;

    @FXML
    private MenuItem pageSetupMenuItem;

    @FXML
    private MenuItem printMenuitem;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private MenuItem quitMenuItem;

    @FXML
    private Menu editMenu;

    @FXML
    private MenuItem undoMenuItem;

    @FXML
    private MenuItem redoMenuItem;

    @FXML
    private MenuItem cutMenuItem;

    @FXML
    private MenuItem copyMenuItem;

    @FXML
    private MenuItem copyImageMenuItem;

    @FXML
    private MenuItem pasteMenuItem;

    @FXML
    private MenuItem duplicateMenuItem;

    @FXML
    private MenuItem deleteMenuItem;

    @FXML
    private MenuItem findMenuItem;

    @FXML
    private MenuItem findAgainMenuItem;

    @FXML
    private MenuItem replaceMenuItem;

    @FXML
    private MenuItem gotoLineMenuItem;

    @FXML
    private MenuItem preferencesMenuItem;

    @FXML
    private MenuItem selectAllMenuItem;

    @FXML
    private MenuItem selectNoneMenuItem;

    @FXML
    private MenuItem selectAllNodesMenuItem;

    @FXML
    private MenuItem selectAllLabeledNodesMenuItem;

    @FXML
    private MenuItem selectAllBelowMenuItem;

    @FXML
    private MenuItem selectBracketsMenuItem;

    @FXML
    private MenuItem invertNodeSelectionMenuItem;

    @FXML
    private MenuItem deselectAllNodesMenuItem;

    @FXML
    private MenuItem selectAllEdgesMenuItem;

    @FXML
    private MenuItem selectAllLabeledEdgesMenuItem;

    @FXML
    private MenuItem selectAllEdgesBelowMenuItem;

    @FXML
    private MenuItem invertEdgeSelectionMenuItem;

    @FXML
    private MenuItem deselectEdgesMenuItem;

    @FXML
    private MenuItem selectFromPreviousMenuItem;

    @FXML
    private MenuItem increaseFontSizeMenuItem;

    @FXML
    private MenuItem decreaseFontSizeMenuItem;

    @FXML
    private MenuItem zoomInMenuItem;

    @FXML
    private MenuItem zoomOutMenuItem;

    @FXML
    private MenuItem resetMenuItem;

    @FXML
    private MenuItem rotateLeftMenuItem;

    @FXML
    private MenuItem rotateRightMenuItem;

    @FXML
    private MenuItem flipMenuItem;

    @FXML
    private CheckMenuItem wrapTextMenuItem;


    @FXML
    private MenuItem formatNodesMenuItem;

    @FXML
    private MenuItem layoutLabelsMenuItem;

    @FXML
    private CheckMenuItem sparseLabelsCheckMenuItem;

    @FXML
    private CheckMenuItem showScaleBarMenuItem;

    @FXML
    private MenuItem fullScreenMenuItem;

    @FXML
    private MenuItem filterTaxaMenuItem;

    @FXML
    private MenuItem filterCharactersMenuItem;

    @FXML
    private MenuItem filterTreesMenuItem;

    @FXML
    private MenuItem filterSplitsMenuItem;

    @FXML
    private MenuItem traitsMenuItem;

    @FXML
    private MenuItem uncorrectedPMenuItem;

    @FXML
    private MenuItem logDetMenuItem;

    @FXML
    private MenuItem hky85MenuItem;

    @FXML
    private MenuItem jukesCantorMenuItem;

    @FXML
    private MenuItem k2pMenuItem;

    @FXML
    private MenuItem k3stMenuItem;

    @FXML
    private MenuItem f81MenuItem;

    @FXML
    private MenuItem f84MenuItem;

    @FXML
    private MenuItem proteinMLDistanceMenuItem;

    @FXML
    private MenuItem geneContentDistanceMenuItem;

    @FXML
    private MenuItem njMenuItem;

    @FXML
    private MenuItem bioNJMenuItem;

    @FXML
    private MenuItem upgmaMenuItem;

    @FXML
    private MenuItem bunemanTreeMenuItem;

    @FXML
    private MenuItem selectTreeMenuItem;

    @FXML
    private MenuItem consensusTreeMenuItem;

    @FXML
    private MenuItem rootByOutgroupMenuItem;

    @FXML
    private MenuItem rootByMidpointMenuItem;


    @FXML
    private MenuItem treeViewMenuItem;

    @FXML
    private MenuItem treeGridMenuItem;

    @FXML
    private MenuItem tanglegramMenuItem;

    @FXML
    private MenuItem neighborNetMenuItem;

    @FXML
    private MenuItem splitDecompositionMenuItem;

    @FXML
    private MenuItem parsimonySplitsMenuItem;

    @FXML
    private MenuItem consensusNetworkMenuItem;

    @FXML
    private MenuItem filteredSuperNetworkMenuItem;

    @FXML
    private MenuItem medianNetworkMenuItem;

    @FXML
    private MenuItem medianJoiningMenuItem;

    @FXML
    private MenuItem minSpanningNetworkMenuItem;

    @FXML
    private MenuItem consensusClusterNetworkMenuItem;

    @FXML
    private MenuItem hybridizationNetworkMenuItem;

    @FXML
    private MenuItem splitsNetworkViewMenuItem;

    @FXML
    private MenuItem haplotypeNetworkViewMenuItem;


    @FXML
    private MenuItem show3DViewerMenuItem;

    @FXML
    private MenuItem relaxMenuItem;

    @FXML
    private MenuItem pcoaMenuItem;

    @FXML
    private MenuItem brayCurtisMenuItem;

    @FXML
    private MenuItem jsdMenuItem;

    @FXML
    private MenuItem bootstrappingMenuItem;

    @FXML
    private MenuItem showBootStrapNetworkMenuItem;

    @FXML
    private MenuItem estimateInvariableSitesMenuItem;

    @FXML
    private MenuItem computePhylogeneticDiversityMenuItem;

    @FXML
    private MenuItem computeDeltaScoreMenuItem;

    @FXML
    private MenuItem showWorkflowMenuItem;

    @FXML
    private Menu windowMenu;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private MenuItem showMessageWindowMenuItem;

    @FXML
    private MenuItem communityWebsiteMenuItem;

    @FXML
    private MenuItem checkForUpdatesMenuItem;

    private MainWindow mainWindow;

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Menu getFileMenu() {
        return fileMenu;
    }

    public MenuItem getNewMenuItem() {
        return newMenuItem;
    }

    public MenuItem getOpenMenuItem() {
        return openMenuItem;
    }

    public Menu getOpenRecentMenu() {
        return openRecentMenu;
    }

    public MenuItem getInputEditorMenuItem() { //todo : delete after testing + in menu.fxml
        return inputEditorMenuItem;
    }

    public MenuItem getSaveMenuItem() {
        return saveMenuItem;
    }

    public MenuItem getSaveAsMenuItem() {
        return saveAsMenuItem;
    }

    public MenuItem getImportMenuItem() {
        return importMenuItem;
    }

    public MenuItem getImportMultipleTreeFilesMenuItem() {
        return importMultipleTreeFilesMenuItem;
    }

    public MenuItem getGroupIdenticalHaplotypesFilesMenuItem() {
        return GroupIdenticalHaplotypesFilesMenuItem;
    }

    public MenuItem getExportMenuItem() {
        return exportMenuItem;
    }

    public MenuItem getExportImageMenuItem() {
        return exportImageMenuItem;
    }

    public MenuItem getReplaceDataMenuItem() {
        return replaceDataMenuItem;
    }

    public MenuItem getExportWorkflowMenuItem() {
        return exportWorkflowMenuItem;
    }

    public MenuItem getPageSetupMenuItem() {
        return pageSetupMenuItem;
    }

    public MenuItem getPrintMenuitem() {
        return printMenuitem;
    }

    public MenuItem getCloseMenuItem() {
        return closeMenuItem;
    }

    public MenuItem getQuitMenuItem() {
        return quitMenuItem;
    }

    public Menu getEditMenu() {
        return editMenu;
    }

    public MenuItem getUndoMenuItem() {
        return undoMenuItem;
    }

    public MenuItem getRedoMenuItem() {
        return redoMenuItem;
    }

    public MenuItem getCutMenuItem() {
        return cutMenuItem;
    }

    public MenuItem getCopyMenuItem() {
        return copyMenuItem;
    }

    public MenuItem getCopyImageMenuItem() {
        return copyImageMenuItem;
    }

    public MenuItem getPasteMenuItem() {
        return pasteMenuItem;
    }

    public MenuItem getDuplicateMenuItem() {
        return duplicateMenuItem;
    }

    public MenuItem getDeleteMenuItem() {
        return deleteMenuItem;
    }

    public MenuItem getFindMenuItem() {
        return findMenuItem;
    }

    public MenuItem getFindAgainMenuItem() {
        return findAgainMenuItem;
    }

    public MenuItem getReplaceMenuItem() {
        return replaceMenuItem;
    }

    public MenuItem getGotoLineMenuItem() {
        return gotoLineMenuItem;
    }

    public MenuItem getPreferencesMenuItem() {
        return preferencesMenuItem;
    }

    public MenuItem getSelectAllMenuItem() {
        return selectAllMenuItem;
    }

    public MenuItem getSelectNoneMenuItem() {
        return selectNoneMenuItem;
    }

    public MenuItem getSelectAllNodesMenuItem() {
        return selectAllNodesMenuItem;
    }

    public MenuItem getSelectAllLabeledNodesMenuItem() {
        return selectAllLabeledNodesMenuItem;
    }

    public MenuItem getSelectAllBelowMenuItem() {
        return selectAllBelowMenuItem;
    }

    public MenuItem getSelectBracketsMenuItem() {
        return selectBracketsMenuItem;
    }

    public MenuItem getInvertNodeSelectionMenuItem() {
        return invertNodeSelectionMenuItem;
    }

    public MenuItem getDeselectAllNodesMenuItem() {
        return deselectAllNodesMenuItem;
    }

    public MenuItem getSelectAllEdgesMenuItem() {
        return selectAllEdgesMenuItem;
    }

    public MenuItem getSelectAllLabeledEdgesMenuItem() {
        return selectAllLabeledEdgesMenuItem;
    }

    public MenuItem getSelectAllEdgesBelowMenuItem() {
        return selectAllEdgesBelowMenuItem;
    }

    public MenuItem getInvertEdgeSelectionMenuItem() {
        return invertEdgeSelectionMenuItem;
    }

    public MenuItem getDeselectEdgesMenuItem() {
        return deselectEdgesMenuItem;
    }

    public MenuItem getSelectFromPreviousMenuItem() {
        return selectFromPreviousMenuItem;
    }

    public MenuItem getIncreaseFontSizeMenuItem() {
        return increaseFontSizeMenuItem;
    }

    public MenuItem getDecreaseFontSizeMenuItem() {
        return decreaseFontSizeMenuItem;
    }

    public MenuItem getZoomInMenuItem() {
        return zoomInMenuItem;
    }

    public MenuItem getZoomOutMenuItem() {
        return zoomOutMenuItem;
    }

    public MenuItem getResetMenuItem() {
        return resetMenuItem;
    }

    public MenuItem getRotateLeftMenuItem() {
        return rotateLeftMenuItem;
    }

    public MenuItem getRotateRightMenuItem() {
        return rotateRightMenuItem;
    }

    public MenuItem getFlipMenuItem() {
        return flipMenuItem;
    }

    public CheckMenuItem getWrapTextMenuItem() {
        return wrapTextMenuItem;
    }

    public MenuItem getFormatNodesMenuItem() {
        return formatNodesMenuItem;
    }

    public MenuItem getLayoutLabelsMenuItem() {
        return layoutLabelsMenuItem;
    }

    public CheckMenuItem getSparseLabelsCheckMenuItem() {
        return sparseLabelsCheckMenuItem;
    }

    public CheckMenuItem getShowScaleBarMenuItem() {
        return showScaleBarMenuItem;
    }

    public MenuItem getFullScreenMenuItem() {
        return fullScreenMenuItem;
    }

    public MenuItem getFilterTaxaMenuItem() {
        return filterTaxaMenuItem;
    }

    public MenuItem getFilterCharactersMenuItem() {
        return filterCharactersMenuItem;
    }

    public MenuItem getFilterTreesMenuItem() {
        return filterTreesMenuItem;
    }

    public MenuItem getFilterSplitsMenuItem() {
        return filterSplitsMenuItem;
    }

    public MenuItem getTraitsMenuItem() {
        return traitsMenuItem;
    }

    public MenuItem getUncorrectedPMenuItem() {
        return uncorrectedPMenuItem;
    }

    public MenuItem getLogDetMenuItem() {
        return logDetMenuItem;
    }

    public MenuItem getHky85MenuItem() {
        return hky85MenuItem;
    }

    public MenuItem getJukesCantorMenuItem() {
        return jukesCantorMenuItem;
    }

    public MenuItem getK2pMenuItem() {
        return k2pMenuItem;
    }

    public MenuItem getK3stMenuItem() {
        return k3stMenuItem;
    }

    public MenuItem getF81MenuItem() {
        return f81MenuItem;
    }

    public MenuItem getF84MenuItem() {
        return f84MenuItem;
    }

    public MenuItem getProteinMLDistanceMenuItem() {
        return proteinMLDistanceMenuItem;
    }

    public MenuItem getGeneContentDistanceMenuItem() {
        return geneContentDistanceMenuItem;
    }

    public MenuItem getNjMenuItem() {
        return njMenuItem;
    }

    public MenuItem getBioNJMenuItem() {
        return bioNJMenuItem;
    }

    public MenuItem getUpgmaMenuItem() {
        return upgmaMenuItem;
    }

    public MenuItem getBunemanTreeMenuItem() {
        return bunemanTreeMenuItem;
    }

    public MenuItem getSelectTreeMenuItem() {
        return selectTreeMenuItem;
    }

    public MenuItem getConsensusTreeMenuItem() {
        return consensusTreeMenuItem;
    }

    public MenuItem getRootByOutgroupMenuItem() {
        return rootByOutgroupMenuItem;
    }

    public MenuItem getRootByMidpointMenuItem() {
        return rootByMidpointMenuItem;
    }

    public MenuItem getTreeViewMenuItem() {
        return treeViewMenuItem;
    }

    public MenuItem getTreeGridMenuItem() {
        return treeGridMenuItem;
    }

    public MenuItem getTanglegramMenuItem() {
        return tanglegramMenuItem;
    }

    public MenuItem getNeighborNetMenuItem() {
        return neighborNetMenuItem;
    }

    public MenuItem getSplitDecompositionMenuItem() {
        return splitDecompositionMenuItem;
    }

    public MenuItem getParsimonySplitsMenuItem() {
        return parsimonySplitsMenuItem;
    }

    public MenuItem getConsensusNetworkMenuItem() {
        return consensusNetworkMenuItem;
    }

    public MenuItem getFilteredSuperNetworkMenuItem() {
        return filteredSuperNetworkMenuItem;
    }

    public MenuItem getMedianNetworkMenuItem() {
        return medianNetworkMenuItem;
    }

    public MenuItem getMedianJoiningMenuItem() {
        return medianJoiningMenuItem;
    }

    public MenuItem getMinSpanningNetworkMenuItem() {
        return minSpanningNetworkMenuItem;
    }

    public MenuItem getConsensusClusterNetworkMenuItem() {
        return consensusClusterNetworkMenuItem;
    }

    public MenuItem getHybridizationNetworkMenuItem() {
        return hybridizationNetworkMenuItem;
    }

    public MenuItem getSplitsNetworkViewMenuItem() {
        return splitsNetworkViewMenuItem;
    }

    public MenuItem getHaplotypeNetworkViewMenuItem() {
        return haplotypeNetworkViewMenuItem;
    }

    public MenuItem getShow3DViewerMenuItem() {
        return show3DViewerMenuItem;
    }

    public MenuItem getRelaxMenuItem() {
        return relaxMenuItem;
    }

    public MenuItem getPcoaMenuItem() {
        return pcoaMenuItem;
    }

    public MenuItem getBrayCurtisMenuItem() {
        return brayCurtisMenuItem;
    }

    public MenuItem getJsdMenuItem() {
        return jsdMenuItem;
    }

    public MenuItem getBootstrappingMenuItem() {
        return bootstrappingMenuItem;
    }

    public MenuItem getShowBootStrapNetworkMenuItem() {
        return showBootStrapNetworkMenuItem;
    }

    public MenuItem getEstimateInvariableSitesMenuItem() {
        return estimateInvariableSitesMenuItem;
    }

    public MenuItem getComputePhylogeneticDiversityMenuItem() {
        return computePhylogeneticDiversityMenuItem;
    }

    public MenuItem getComputeDeltaScoreMenuItem() {
        return computeDeltaScoreMenuItem;
    }

    public MenuItem getShowWorkflowMenuItem() {
        return showWorkflowMenuItem;
    }

    public Menu getWindowMenu() {
        return windowMenu;
    }

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public MenuItem getShowMessageWindowMenuItem() {
        return showMessageWindowMenuItem;
    }

    public MenuItem getCommunityWebsiteMenuItem() {
        return communityWebsiteMenuItem;
    }

    public MenuItem getCheckForUpdatesMenuItem() {
        return checkForUpdatesMenuItem;
    }

    private final Set<MenuItem> alwaysOnMenuItems = new HashSet<>();


    @FXML
    void initialize() {
        // if we are running on MacOS, put the specific menu items in the right places
        if (ProgramProperties.isMacOS()) {
            getMenuBar().setUseSystemMenuBar(true);
            fileMenu.getItems().remove(getQuitMenuItem());
            windowMenu.getItems().remove(getAboutMenuItem());
            editMenu.getItems().remove(getPreferencesMenuItem());
        } else {
            getAboutMenuItem().setOnAction((e) -> SplashScreen.getInstance().showSplash(Duration.ofMinutes(1)));
        }

        increaseFontSizeMenuItem.setAccelerator(new KeyCharacterCombination("+", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_ANY));
        decreaseFontSizeMenuItem.setAccelerator(new KeyCharacterCombination("-", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_ANY));


        final InvalidationListener invalidationListener = observable -> {
            windowMenu.getItems().clear();
            windowMenu.getItems().add(getShowMessageWindowMenuItem());
            int count = 0;
            for (IMainWindow mainWindow : MainWindowManager.getInstance().getMainWindows()) {
                if (count == 0)
                    windowMenu.getItems().add(new SeparatorMenuItem());
                if (mainWindow.getStage() != null) {
                    final MenuItem menuItem = new MenuItem(mainWindow.getStage().getTitle().replaceAll("- " + ProgramProperties.getProgramName(), ""));
                    menuItem.setOnAction((e) -> mainWindow.getStage().toFront());
                    menuItem.setAccelerator(new KeyCharacterCombination("" + (++count), KeyCombination.SHORTCUT_DOWN));
                    windowMenu.getItems().add(menuItem);
                }
                if (MainWindowManager.getInstance().getAuxiliaryWindows(mainWindow) != null) {
                    for (Stage auxStage : MainWindowManager.getInstance().getAuxiliaryWindows(mainWindow)) {
                        final MenuItem menuItem = new MenuItem(auxStage.getTitle().replaceAll("- " + ProgramProperties.getProgramName(), ""));
                        menuItem.setOnAction((e) -> auxStage.toFront());
                        windowMenu.getItems().add(menuItem);
                    }
                }
            }
        };
        MainWindowManager.getInstance().changedProperty().addListener(invalidationListener);
        invalidationListener.invalidated(null);

        alwaysOnMenuItems.add(fullScreenMenuItem);
        alwaysOnMenuItems.add(quitMenuItem);
        alwaysOnMenuItems.add(openRecentMenu);
        alwaysOnMenuItems.add(showWorkflowMenuItem);
        alwaysOnMenuItems.add(toolsMenu);
    }

    /**
     * unbinds and disables all menu items
     */
    public void unbindAndDisableAllMenuItems() {
        for (Menu menu : menuBar.getMenus()) {
            if (menu != windowMenu) { // don't disable window menu
                for (MenuItem menuItem : menu.getItems()) {
                    if (!alwaysOnMenuItems.contains(menuItem)) {
                        menuItem.setOnAction(null);
                        menuItem.disableProperty().unbind();
                        menuItem.setDisable(true);
                    }
                }
            }
        }
        if (undoMenuItem.textProperty().isBound()) {
            undoMenuItem.textProperty().unbind();
            undoMenuItem.setText("Undo");
        }
        if (redoMenuItem.textProperty().isBound()) {
            redoMenuItem.textProperty().unbind();
            redoMenuItem.setText("Redo");
        }

        RecentFilesManager.getInstance().setFileOpener((fileName) -> {
            if (WorkflowNexusInput.isApplicable(fileName))
                WorkflowNexusInput.open(mainWindow, fileName);
            else
                FileOpener.open(false, mainWindow, fileName, null);
        });

        wrapTextMenuItem.selectedProperty().unbind();

        getOpenRecentMenu().disableProperty().unbind();
        getOpenRecentMenu().setDisable(false);
    }

    /**
     * enables all menu items whose disable property is not bound and that have an action
     */
    public void enableAllUnboundActionMenuItems() {
        if (getOpenRecentMenu().getItems().size() == 0) // can't do this in init because mainWindow not available there
            RecentFilesManager.getInstance().setupMenu(getOpenRecentMenu());

        for (Menu menu : menuBar.getMenus()) {
            for (MenuItem menuItem : menu.getItems()) {
                if (!alwaysOnMenuItems.contains(menuItem)) {
                    if (!menuItem.disableProperty().isBound() && menuItem.getOnAction() != null)
                        menuItem.setDisable(false);
                }
            }
        }

        if (mainWindow != null) // need to refresh the icon for unknown reasons...
            mainWindow.getStage().getIcons().setAll(ProgramProperties.getProgramIconsFX());

    }

    /**
     * adds full screen support
     *
     * @param stage
     */
    public void setupFullScreenMenuSupport(Stage stage) {
        stage.fullScreenProperty().addListener((c, o, n) -> {
            fullScreenMenuItem.setText(n ? "Exit Full Screen" : "Enter Full Screen");
        });
        fullScreenMenuItem.setOnAction((e) -> {
            stage.setFullScreen(!stage.isFullScreen());
        });
        fullScreenMenuItem.setDisable(false);
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }
}
