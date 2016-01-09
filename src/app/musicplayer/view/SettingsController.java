package app.musicplayer.view;

import java.net.URL;
import java.util.ResourceBundle;

import app.musicplayer.model.Library;
import javafx.fxml.Initializable;

public class SettingsController implements Initializable {

    public static void main(String[] args) throws Exception {
        Library.importMusic("C:\\Users\\Mpmar\\Music");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

}
