package app.musicplayer.model;

import app.musicplayer.util.Resources;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Pane;

public class PlayingTableCell<S, T> extends TableCell<S, T> {
	
	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null || !((Boolean) item).booleanValue()) {
			setText(null);
			setGraphic(null);
		} else {
			try {
				String fileName = Resources.FXML + "PlayingIcon.fxml";
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fileName));
                Pane pane = (Pane) loader.load();
                setGraphic(pane);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
		}
	}
}