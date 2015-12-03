package musicplayer;

import javafx.scene.control.TableCell;
import javafx.scene.shape.SVGPath;
import javafx.scene.layout.Pane;
import javafx.geometry.Insets;
import javafx.fxml.FXMLLoader;

public class PlayingTableCell<S, T> extends TableCell<S, T> {

        @Override
        protected void updateItem(T item, boolean empty) {

             super.updateItem(item, empty);

             if (empty || item == null || !((Boolean) item).booleanValue()) {
                setText(null);
                setGraphic(null);
             } else {
                try {
                    String fileName = Resources.FXML + "playingIcon.fxml";
                    FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fileName));
                    Pane pane = (Pane) loader.load();
                    setGraphic(pane);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
             }
         }
    }