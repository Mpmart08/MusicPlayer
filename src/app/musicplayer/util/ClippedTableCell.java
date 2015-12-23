package app.musicplayer.util;

import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;

public class ClippedTableCell<S, T> extends TableCell<S, T> {

    public ClippedTableCell() {

        setTextOverrun(OverrunStyle.CLIP);
    }

    @Override
    protected void updateItem(T item, boolean empty) {

         super.updateItem(item, empty);

         if (empty || item == null) {
             setText(null);
             setGraphic(null);
         } else {
             setText(item.toString());
         }
     }
}