/*
 *  Copyright (C) 2016 Daniel H. Huson
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

/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.gui.utils;

import javafx.scene.control.ListView;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.*;
import javafx.scene.text.Text;
import jloda.util.Basic;
import splitstree5.undo.UndoManager;
import splitstree5.undo.UndoableChangeListViews2;

import java.util.ArrayList;
import java.util.List;

/**
 * drag and drop support between a pair of list views
 * Daniel Huson, 12/26/16.
 */
public class DragAndDropSupportListView2<T> {
    private ListView<T> fromList;

    /**
     * setup drag and drop support between a pair of list views
     *
     * @param listViewA
     * @param listViewB
     * @param <T>
     */
    public static <T> void setup(ListView<T> listViewA, ListView<T> listViewB, UndoManager undoManager, String changeName) {
        new DragAndDropSupportListView2<T>(listViewA, listViewB, undoManager, changeName);
    }

    /**
     * constructs the support object
     *
     * @param listViewA
     * @param listViewB
     */
    private DragAndDropSupportListView2(ListView<T> listViewA, ListView<T> listViewB, UndoManager undoManager, String changeName) {
        final DataFormat dataFormat = getDataFormat(listViewA);

        listViewA.setOnDragDetected(event -> {
            fromList = listViewA;
            final Dragboard dragBoard = listViewA.startDragAndDrop(TransferMode.MOVE);
            final ClipboardContent content = new ClipboardContent();
            content.put(dataFormat, new ArrayList<T>(listViewA.getSelectionModel().getSelectedItems()));
            content.putString(Basic.toString(listViewA.getSelectionModel().getSelectedItems(), "\n"));
            dragBoard.setContent(content);
        });

        listViewB.setOnDragDetected(event -> {
            fromList = listViewB;
            final Dragboard dragBoard = listViewA.startDragAndDrop(TransferMode.MOVE);
            final ClipboardContent content = new ClipboardContent();
            content.put(dataFormat, new ArrayList<T>(listViewB.getSelectionModel().getSelectedItems()));
            content.putString(Basic.toString(listViewB.getSelectionModel().getSelectedItems(), "\n"));

            dragBoard.setContent(content);
        });

        listViewA.setOnDragDone(dragEvent -> {
        });

        listViewB.setOnDragDone(dragEvent -> {
        });

        listViewA.setOnDragEntered(dragEvent -> {
            listViewA.setBlendMode(BlendMode.DARKEN);
        });

        listViewB.setOnDragEntered(dragEvent -> {
            listViewB.setBlendMode(BlendMode.DARKEN);
        });

        listViewA.setOnDragExited(dragEvent -> {
            listViewA.setBlendMode(null);
        });
        listViewB.setOnDragExited(dragEvent -> {
            listViewB.setBlendMode(null);
        });

        listViewA.setOnDragOver(dragEvent -> {
            dragEvent.acceptTransferModes(TransferMode.MOVE);
        });
        listViewB.setOnDragOver(dragEvent -> {
            dragEvent.acceptTransferModes(TransferMode.MOVE);
        });

        listViewA.setOnDragDropped(dragEvent -> {
            if (fromList != null) {
                ArrayList<T> prevA = null;
                ArrayList<T> prevB = null;

                if (undoManager != null) {
                    undoManager.setRecordChanges(false);
                    prevA = new ArrayList<T>(listViewA.getItems());
                    prevB = new ArrayList<T>(listViewB.getItems());
                }
                final List<T> list = (List<T>) dragEvent.getDragboard().getContent(dataFormat);
                fromList.getSelectionModel().clearSelection();
                fromList.getItems().removeAll(list);
                listViewA.getSelectionModel().clearSelection();
                int where = -1;
                {
                    final PickResult pickResult = dragEvent.getPickResult();
                    int count = 0;
                    if (pickResult != null && pickResult.getIntersectedNode() instanceof Text) {
                        String name = ((Text) pickResult.getIntersectedNode()).getText();
                        for (T item : listViewA.getItems()) {
                            if (item.toString().equals(name)) {
                                where = count;
                                break;
                            }
                            count++;

                        }
                    }
                }
                if (where == -1)
                    listViewA.getItems().addAll(list);
                else
                    listViewA.getItems().addAll(where, list);
                for (T item : list) {
                    listViewA.getSelectionModel().select(item);
                }
                fromList = null;
                if (undoManager != null) {
                    undoManager.setRecordChanges(true);
                    undoManager.add(new UndoableChangeListViews2<>(changeName, listViewA, prevA, listViewB, prevB));
                }
            }
            dragEvent.setDropCompleted(true);
        });

        listViewB.setOnDragDropped(dragEvent -> {
            if (fromList != null) {
                ArrayList<T> prevA = null;
                ArrayList<T> prevB = null;

                if (undoManager != null) {
                    undoManager.setRecordChanges(false);
                    prevA = new ArrayList<T>(listViewA.getItems());
                    prevB = new ArrayList<T>(listViewB.getItems());
                }

                final List<T> list = (List<T>) dragEvent.getDragboard().getContent(dataFormat);
                fromList.getSelectionModel().clearSelection();
                fromList.getItems().removeAll(list);
                listViewB.getSelectionModel().clearSelection();
                int where = -1;
                {
                    final PickResult pickResult = dragEvent.getPickResult();
                    int count = 0;
                    if (pickResult != null && pickResult.getIntersectedNode() instanceof Text) {
                        String name = ((Text) pickResult.getIntersectedNode()).getText();
                        for (T item : listViewB.getItems()) {
                            if (item.toString().equals(name)) {
                                where = count;
                                break;
                            }
                            count++;
                        }
                    }
                }
                if (where == -1)
                    listViewB.getItems().addAll(list);
                else
                    listViewB.getItems().addAll(where, list);
                for (T item : list) {
                    listViewB.getSelectionModel().select(item);
                }
                fromList = null;
                if (undoManager != null) {
                    undoManager.setRecordChanges(true);
                    undoManager.add(new UndoableChangeListViews2<>(changeName, listViewA, prevA, listViewB, prevB));

                }
                dragEvent.setDropCompleted(true);
            }
        });
    }

    private static <T> DataFormat getDataFormat(ListView<T> listView) {
        String dataFormatName = "ListOf" + listView.getClass().getSuperclass().toString();
        if (DataFormat.lookupMimeType(dataFormatName) != null)
            return DataFormat.lookupMimeType(dataFormatName);
        else
            return new DataFormat(dataFormatName);
    }

}
