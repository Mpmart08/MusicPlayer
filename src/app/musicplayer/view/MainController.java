package app.musicplayer.view;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Song;
import app.musicplayer.util.Resources;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainController implements Initializable {

	private boolean isSideBarExpanded = true;
    private double expandedWidth = 250;
    private double collapsedWidth = 50;
    private double expandedHeight = 50;
    private double collapsedHeight = 0;
    private Initializable subViewController;

    @FXML private BorderPane mainWindow;
    @FXML private ScrollPane subViewRoot;
    @FXML private VBox sideBar;
    @FXML private ImageView sideBarSlideButton;
    @FXML private ImageView playPauseButton;
    @FXML private ImageView nowPlayingArtwork;
    @FXML private Label nowPlayingTitle;
    @FXML private Label nowPlayingArtist;
    @FXML private Slider timeSlider;
    @FXML private Slider invisibleSlider;
    @FXML private Label timePassed;
    @FXML private Label timeRemaining;
    @FXML private ImageView backButton;
    @FXML private ImageView skipButton;
    
    /**
     * Creates a MainController Object.
     * Constructor is called before the initialize() method.
     */
    public MainController() {}
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        timeSlider.valueChangingProperty().addListener(
            (slider, wasChanging, isChanging) -> {

                if (wasChanging) {

                    int seconds = (int) Math.round(timeSlider.getValue() / 4.0);
                    timeSlider.setValue(seconds * 4);
                    invisibleSlider.setValue(seconds * 4);
                    MusicPlayer.seek(seconds);
                }
            }
        );

        timeSlider.valueProperty().addListener(
            (slider, oldValue, newValue) -> {

                double previous = oldValue.doubleValue();
                double current = newValue.doubleValue();
                if (!timeSlider.isValueChanging() && current != previous + 1) {

                    int seconds = (int) Math.round(current / 4.0);
                    invisibleSlider.setValue(seconds * 4);
                    timeSlider.setValue(seconds * 4);
                    MusicPlayer.seek(seconds);
                }
            }
        );

        invisibleSlider.valueProperty().addListener(
            (slider, oldValue, newValue) -> {

                double previous = oldValue.doubleValue();
                double current = newValue.doubleValue();
                if (!invisibleSlider.isValueChanging() && current != previous + 1) {

                    int seconds = (int) Math.round(current / 4.0);
                    invisibleSlider.setValue(seconds * 4);
                    timeSlider.setValue(seconds * 4);
                    MusicPlayer.seek(seconds);
                }
            }
        );
        // Loads the default view: artists.
        loadView("artists");
    } // End initialize()

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
            loadView(eventSource.getId());
        } else if (styles.get(0).equals("bottomBarItem")) {
            loadView(eventSource.getId());
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
    public void playPause() {

        if (MusicPlayer.isPlaying()) {
            MusicPlayer.pause();
        } else {
            MusicPlayer.play();
        }
    }

    @FXML
    private void back() {

        MusicPlayer.back();
    }

    @FXML
    private void skip() {

        MusicPlayer.skip();
    }

    public Initializable loadView(String viewName) {

        try {
        	
            String fileName = viewName.substring(0, 1).toUpperCase() + viewName.substring(1) + ".fxml";
            
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fileName));
            Node view = (Node) loader.load();
            subViewRoot.setContent(view);
            if (loadViewAnimation.statusProperty().get() == Animation.Status.RUNNING) {
                loadViewAnimation.stop();
            }
            loadViewAnimation.play();
            subViewController = loader.getController();
            return subViewController;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void updatePlayPauseIcon() {

        Image icon;

        if (MusicPlayer.isPlaying()) {

            icon = new Image(this.getClass().getResource(Resources.IMG + "PlayIcon.png").toString());
            playPauseButton.setImage(icon);

        } else {

            icon = new Image(this.getClass().getResource(Resources.IMG + "PauseIcon.png").toString());
            playPauseButton.setImage(icon);
        }
    }

    public void updateNowPlayingButton() {

        Song song = MusicPlayer.getNowPlaying();
        if (song != null) {
            nowPlayingTitle.setText(song.getTitle());
            nowPlayingArtist.setText(song.getArtist());
            nowPlayingArtwork.setImage(song.getArtwork());
        } else {
            nowPlayingTitle.setText("");
            nowPlayingArtist.setText("");
            nowPlayingArtwork.setImage(null);
        }
    }

    public void initializeTimeSlider() {

        Song song = MusicPlayer.getNowPlaying();
        if (song != null) {
            timeSlider.setMin(0);
            timeSlider.setMax(song.getLengthInSeconds() * 4);
            timeSlider.setValue(0);
            timeSlider.setBlockIncrement(1);
            invisibleSlider.setMin(0);
            invisibleSlider.setMax(song.getLengthInSeconds() * 4);
            invisibleSlider.setValue(0);
            invisibleSlider.setBlockIncrement(1);
        } else {
            timeSlider.setMin(0);
            timeSlider.setMax(1);
            timeSlider.setValue(0);
            timeSlider.setBlockIncrement(1);
            invisibleSlider.setMin(0);
            invisibleSlider.setMax(1);
            invisibleSlider.setValue(0);
            invisibleSlider.setBlockIncrement(1);
        }
    }

    public void updateTimeSlider() {

        invisibleSlider.increment();
        timeSlider.increment();
    }

    public void initializeTimeLabels() {

        Song song = MusicPlayer.getNowPlaying();
        if (song != null) {
            timePassed.setText("0:00");
            timeRemaining.setText(song.getLength());
        } else {
            timePassed.setText("");
            timeRemaining.setText("");
        }
    }

    public void updateTimeLabels() {

        timePassed.setText(MusicPlayer.getTimePassed());
        timeRemaining.setText(MusicPlayer.getTimeRemaining());
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

    private void setVisibility(boolean isVisible) {
        for (Node n : sideBar.getChildren()) {
            if (n instanceof HBox) {
                for (Node m : ((HBox)n).getChildren()) {
                    if (m instanceof Label) {
                        m.setVisible(isVisible);
                    }
                }
            }
        }
    }

    private void setSlideDirection() {
        isSideBarExpanded = !isSideBarExpanded;
        sideBarSlideButton.setImage(new Image(this.getClass().getResource(Resources.IMG
                + (isSideBarExpanded ? "leftArrowIcon.png" : "rightArrowIcon.png")).toString()));
    }
    
    // ANIMATIONS
    
    private Animation collapseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> setSlideDirection());
        }
        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (1.0 - frac);
            sideBar.setPrefWidth(curWidth);
        }
    };

    private Animation expandAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> {setVisibility(true); setSlideDirection();});
        }
        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (frac);
            sideBar.setPrefWidth(curWidth);
        }
    };

    private Animation loadViewAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(1000));
        }
        protected void interpolate(double frac) {
            subViewRoot.setVvalue(0);
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            if (frac < 0.25) {
                subViewRoot.getContent().setTranslateY(expandedHeight - curHeight * 4);
            } else {
                subViewRoot.getContent().setTranslateY(collapsedHeight);
            }
            subViewRoot.getContent().setOpacity(frac);
        }
    };
}
