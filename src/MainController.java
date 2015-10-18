package musicplayer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.fxml.FXMLLoader;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.util.Duration;
import javafx.event.EventHandler;
import javafx.event.Event;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.nio.file.Paths;

public class MainController {

    private boolean isSideBarExpanded = true;
    private double expandedWidth = 250;
    private double collapsedWidth = 50;
    private boolean isPaused = true;
    private MediaPlayer mediaPlayer;

    private Animation collapseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> setSlideDirection());
        }
        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (1.0 - frac);
            slide(curWidth);
        }
    };

    private Animation expandAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> {setVisibility(true); setSlideDirection();});
        }
        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (frac);
            slide(curWidth);
        }
    };

    @FXML
    private BorderPane mainWindow;

    @FXML
    private VBox sideBar;

    @FXML
    private ImageView sideBarSlideButton;

    @FXML
    private ImageView playPauseButton;

    @FXML
    private void changeView(Event e) {

        try {

            String fileName = ((Node)e.getSource()).getId();
            Node view = (Node)FXMLLoader.load(MainView.class.getResource("res/fxml/" + fileName + ".fxml"));
            mainWindow.setCenter(view);

        } catch (Exception ex) {

            System.out.println(ex.toString());
        }
    }

    @FXML
    private void slideSideBar() {

        if (isSideBarExpanded) {
            collapseSideBar();
        } else {
            expandSideBar();
        }
    }

    @FXML
    private void playPause() {

        if (isPaused) {
            String path = "C:\\Users\\Mpmar\\Music\\Nobuo Uematsu\\Final Fantasy X Original Soundtrack Disc\\02 To Zanarkand.mp3";
            Media file = new Media(Paths.get(path).toUri().toString());
            if (mediaPlayer == null) mediaPlayer = new MediaPlayer(file);
            mediaPlayer.play();
        } else {
            mediaPlayer.pause();
        }
        isPaused = !isPaused;
        playPauseButton.setImage(new Image(isPaused
            ? "musicplayer/res/img/PlayIcon.png"
            : "musicplayer/res/img/PauseIcon.png"));
    }

    private void collapseSideBar() {

        if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
            && collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {

                setVisibility(false);
                collapseAnimation.play();
        }
    }

    private void expandSideBar() {

        if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
            && collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {

                expandAnimation.play();
        }
    }

    private void slide(double curWidth) {

        sideBar.setPrefWidth(curWidth);
        for (Node n : sideBar.getChildren()) {
            if (n instanceof HBox) {
                for (Node m : ((HBox)n).getChildren()) {
                    if (m instanceof Label) {
                        m.setTranslateX(-expandedWidth + curWidth);
                    }
                }
            }
        }
    }

    private void setVisibility(boolean isVisible) {

        for (Node n : sideBar.getChildren()) {
            if (n instanceof HBox) {
                for (Node m : ((HBox)n).getChildren()) {
                    if (m instanceof Label) {
                        m.setOpacity(isVisible ? 1 : 0);
                    }
                }
            }
        }
    }

    private void setSlideDirection() {

        isSideBarExpanded = !isSideBarExpanded;
        sideBarSlideButton.setImage(new Image(isSideBarExpanded
            ? "musicplayer/res/img/LeftArrowIcon.png"
            : "musicplayer/res/img/RightArrowIcon.png"));
    }

}