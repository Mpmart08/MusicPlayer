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
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Optional;
import javafx.scene.layout.Region;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class MainController implements Initializable {

    private boolean isSideBarExpanded = true;
    private double expandedWidth = 250;
    private double collapsedWidth = 50;

    @FXML private BorderPane mainWindow;
    @FXML private VBox sideBar;
    @FXML private ImageView sideBarSlideButton;
    @FXML private ImageView playPauseButton;
    @FXML private Region nowPlayingArtwork;
    @FXML private Label nowPlayingTitle;
    @FXML private Label nowPlayingArtist;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }

    @FXML
    private void selectView(Event e) {

        HBox eventSource = ((HBox)e.getSource());

        Optional<Node> previous = sideBar.getChildren().stream()
            .filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();

        if (previous.isPresent()) {
            HBox previousItem = (HBox)previous.get();
            previousItem.getStyleClass().setAll("sideBarItem");
        }

        ObservableList<String> styles = eventSource.getStyleClass();
        
        if (styles.get(0).equals("sideBarItem")) {
            styles.setAll("sideBarItemSelected");
            loadView(eventSource);
        } else if (styles.get(0).equals("bottomBarItem")) {
            loadView(eventSource);
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

        Image icon;

        if (MusicPlayer.isPlaying()) {

            MusicPlayer.pause();
            icon = new Image(this.getClass().getResource(Resources.IMG + "playIcon.png").toString());
            playPauseButton.setImage(icon);

        } else {

            MusicPlayer.play();
            icon = new Image(this.getClass().getResource(Resources.IMG + "pauseIcon.png").toString());
            playPauseButton.setImage(icon);

            Song song = MusicPlayer.getNowPlayingStack().getFirst();

            nowPlayingTitle.setText(song.getTitle());
            nowPlayingArtist.setText(song.getArtist());

            Image artwork = song.getArtwork();
            if (artwork == null) {
                artwork = new Image(this.getClass().getResource(Resources.IMG + "albumsIcon.png").toString());
            }
            BackgroundImage image = new BackgroundImage(artwork, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);

            nowPlayingArtwork.setBackground(new Background(image));
        }
    }

    private void loadView(HBox eventSource) {

        try {

            String fileName = Resources.FXML + eventSource.getId() + ".fxml";
            Node view = (Node)FXMLLoader.load(this.getClass().getResource(fileName));
            mainWindow.setCenter(view);

        } catch (Exception ex) {

            ex.printStackTrace();
        }
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
        sideBarSlideButton.setImage(new Image(this.getClass().getResource(Resources.IMG
            + (isSideBarExpanded
            ? "leftArrowIcon.png"
            : "rightArrowIcon.png")
        ).toString()));
    }
}