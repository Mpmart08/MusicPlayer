package musicplayer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.Node;

public class SongsController implements Initializable {

    @FXML
    private TableView tableView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        for (Object item : tableView.getColumns()) {
            TableColumn column = (TableColumn) item;
            column.prefWidthProperty().bind(tableView.widthProperty().multiply(0.2));
        }

    }

}