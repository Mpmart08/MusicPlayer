package app.musicplayer.view;

import java.net.URL;
import java.util.ResourceBundle;

import app.musicplayer.model.Library;
import app.musicplayer.util.Refreshable;
import javafx.fxml.Initializable;

public class SettingsController implements Initializable, Refreshable {

    public static void main(String[] args) throws Exception {
        Library.importMusic("C:\\Users\\Mpmar\\Music");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    @Override
    public void refresh() {}

}
