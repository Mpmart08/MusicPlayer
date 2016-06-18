package app.musicplayer.util;

import com.sun.javafx.scene.control.behavior.SliderBehavior;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * Region/css based skin for slider.
 *
 */
@SuppressWarnings("restriction")
public class CustomSliderSkin extends BehaviorSkinBase<Slider, SliderBehavior> {

    // Track if slider is vertical/horizontal and cause re layout.
    private NumberAxis tickLine = null;
    private double trackToTickGap = 2;

    private boolean showTickMarks;
    private double thumbWidth;

    private double trackStart;
    private double trackLength;
    private double thumbTop;
    private double thumbLeft;
    private double preDragThumbPos;
    private Point2D dragStart; // in skin coordinates

    private StackPane thumb;
    private StackPane track;
    private boolean trackClicked = false;

    public CustomSliderSkin(Slider slider) {
        super(slider, new SliderBehavior(slider));

        initialize();
        slider.requestLayout();
        registerChangeListener(slider.minProperty(), "MIN");
        registerChangeListener(slider.maxProperty(), "MAX");
        registerChangeListener(slider.valueProperty(), "VALUE");
        registerChangeListener(slider.orientationProperty(), "ORIENTATION");
        registerChangeListener(slider.showTickMarksProperty(), "SHOW_TICK_MARKS");
        registerChangeListener(slider.showTickLabelsProperty(), "SHOW_TICK_LABELS");
        registerChangeListener(slider.majorTickUnitProperty(), "MAJOR_TICK_UNIT");
        registerChangeListener(slider.minorTickCountProperty(), "MINOR_TICK_COUNT");
        registerChangeListener(slider.labelFormatterProperty(), "TICK_LABEL_FORMATTER");
    }

    private void initialize() {
        thumb = new StackPane();
        thumb.getStyleClass().setAll("thumb");
        track = new StackPane();
        track.getStyleClass().setAll("track");

        getChildren().clear();
        getChildren().addAll(track, thumb);
        setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
        track.setOnMousePressed(me -> {
            trackClicked = true;
            getBehavior().trackPress(me, (me.getX() / trackLength));
            getBehavior().thumbPressed(me, 0.0f);
            dragStart = track.localToParent(me.getX(), me.getY());
            preDragThumbPos = (me.getX() / trackLength);
            thumbPressAnimation.play();
        });

        track.setOnMouseReleased(me -> {
            getBehavior().thumbReleased(me);
            trackClicked = false;
            thumbReleaseAnimation.play();
        });

        track.setOnMouseDragged(me -> {
            Point2D cur = track.localToParent(me.getX(), me.getY());
            double dragPos = (getSkinnable().getOrientation() == Orientation.HORIZONTAL)?
                cur.getX() - dragStart.getX() : -(cur.getY() - dragStart.getY());
            getBehavior().thumbDragged(me, preDragThumbPos + dragPos / trackLength);
        });

        thumb.setOnMousePressed(me -> {
            getBehavior().thumbPressed(me, 0.0f);
            dragStart = thumb.localToParent(me.getX(), me.getY());
            preDragThumbPos = (getSkinnable().getValue() - getSkinnable().getMin()) /
                    (getSkinnable().getMax() - getSkinnable().getMin());
            thumbPressAnimation.play();
        });

        thumb.setOnMouseReleased(me -> {
            getBehavior().thumbReleased(me);
            thumbReleaseAnimation.play();
        });

        thumb.setOnMouseDragged(me -> {
            Point2D cur = thumb.localToParent(me.getX(), me.getY());
            double dragPos = (getSkinnable().getOrientation() == Orientation.HORIZONTAL)?
                cur.getX() - dragStart.getX() : -(cur.getY() - dragStart.getY());
            getBehavior().thumbDragged(me, preDragThumbPos + dragPos / trackLength);
        });
    }
    
    private void setShowTickMarks(boolean ticksVisible, boolean labelsVisible) {
        showTickMarks = (ticksVisible || labelsVisible);
        Slider slider = getSkinnable();
        if (showTickMarks) {
            if (tickLine == null) {
                tickLine = new NumberAxis();
                tickLine.setAutoRanging(false);
                tickLine.setSide(slider.getOrientation() == Orientation.VERTICAL ? Side.RIGHT : (slider.getOrientation() == null) ? Side.RIGHT: Side.BOTTOM);
                tickLine.setUpperBound(slider.getMax());
                tickLine.setLowerBound(slider.getMin());
                tickLine.setTickUnit(slider.getMajorTickUnit());
                tickLine.setTickMarkVisible(ticksVisible);
                tickLine.setTickLabelsVisible(labelsVisible);
                tickLine.setMinorTickVisible(ticksVisible);
                // Add 1 to the slider minor tick count since the axis draws one
                // less minor ticks than the number given.
                tickLine.setMinorTickCount(Math.max(slider.getMinorTickCount(),0) + 1);
                if (slider.getLabelFormatter() != null) {
                    tickLine.setTickLabelFormatter(stringConverterWrapper);
                }
                getChildren().clear();
                getChildren().addAll(tickLine, track, thumb);
            } else {
                tickLine.setTickLabelsVisible(labelsVisible);
                tickLine.setTickMarkVisible(ticksVisible);
                tickLine.setMinorTickVisible(ticksVisible);
            }
        } 
        else  {
            getChildren().clear();
            getChildren().addAll(track, thumb);
        }

        getSkinnable().requestLayout();
    }    
    
    public StackPane getThumb() {
    	return thumb;
    }
    
    public StackPane getTrack() {
    	return track;
    }

    private StringConverter<Number> stringConverterWrapper = new StringConverter<Number>() {
        Slider slider = getSkinnable();
        @Override public String toString(Number object) {
            return(object != null) ? slider.getLabelFormatter().toString(object.doubleValue()) : "";
        }
        @Override public Number fromString(String string) {
            return slider.getLabelFormatter().fromString(string);
        }
    };

    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        Slider slider = getSkinnable();
        if ("ORIENTATION".equals(p)) {
            if (showTickMarks && tickLine != null) {
                tickLine.setSide(slider.getOrientation() == Orientation.VERTICAL ? Side.RIGHT : (slider.getOrientation() == null) ? Side.RIGHT: Side.BOTTOM);
            }
            getSkinnable().requestLayout();
        } else if ("VALUE".equals(p)) {
            // Only animate thumb if the track was clicked, not if the thumb is dragged
            positionThumb(trackClicked);
        } else if ("MIN".equals(p) ) {
            if (showTickMarks && tickLine != null) {
                tickLine.setLowerBound(slider.getMin());
            }
            getSkinnable().requestLayout();
        } else if ("MAX".equals(p)) {
            if (showTickMarks && tickLine != null) {
                tickLine.setUpperBound(slider.getMax());
            }
            getSkinnable().requestLayout();
        } else if ("SHOW_TICK_MARKS".equals(p) || "SHOW_TICK_LABELS".equals(p)) {
            setShowTickMarks(slider.isShowTickMarks(), slider.isShowTickLabels());
        }  else if ("MAJOR_TICK_UNIT".equals(p)) {
            if (tickLine != null) {
                tickLine.setTickUnit(slider.getMajorTickUnit());
                getSkinnable().requestLayout();
            }
        } else if ("MINOR_TICK_COUNT".equals(p)) {
            if (tickLine != null) {
                tickLine.setMinorTickCount(Math.max(slider.getMinorTickCount(),0) + 1);
                getSkinnable().requestLayout();
            }
        } else if ("TICK_LABEL_FORMATTER".equals(p)) {
            if (tickLine != null) {
                if (slider.getLabelFormatter() == null) {
                    tickLine.setTickLabelFormatter(null);
                } else {
                    tickLine.setTickLabelFormatter(stringConverterWrapper);
                    tickLine.requestAxisLayout();
                }
            }
        }
    }

    /**
     * Called when ever either min, max or value changes, so thumb's layoutX, Y is recomputed.
     */
    private void positionThumb(final boolean animate) {
        Slider s = getSkinnable();
        if (s.getValue() > s.getMax()) return;// this can happen if we are bound to something 
        boolean horizontal = s.getOrientation() == Orientation.HORIZONTAL;
        final double endX = (horizontal) ? trackStart + (((trackLength * ((s.getValue() - s.getMin()) /
                (s.getMax() - s.getMin()))) - thumbWidth/2)) : thumbLeft;
        final double endY = (horizontal) ? thumbTop :
            snappedTopInset() + trackLength - (trackLength * ((s.getValue() - s.getMin()) /
                (s.getMax() - s.getMin()))); //  - thumbHeight/2
        
        if (animate) {
            // lets animate the thumb transition
            final double startX = thumb.getLayoutX();
            final double startY = thumb.getLayoutY();
            Transition transition = new Transition() {
                {
                    setCycleDuration(Duration.millis(200));
                }

                @Override protected void interpolate(double frac) {
                    if (!Double.isNaN(startX)) {
                        thumb.setLayoutX(startX + frac * (endX - startX));
                    }
                    if (!Double.isNaN(startY)) {
                        thumb.setLayoutY(startY + frac * (endY - startY));
                    }
                }
            };
            transition.play();
        } else {
            thumb.setLayoutX(endX);
            thumb.setLayoutY(endY);
        }
    }

    @Override protected void layoutChildren(final double x, final double y,
            final double w, final double h) {
         // calculate the available space
        // resize thumb to preferred size
        thumbWidth = snapSize(thumb.prefWidth(-1));
        double thumbHeight = snapSize(thumb.prefHeight(-1));
        thumb.resize(thumbWidth, thumbHeight);
        // we are assuming the is common radius's for all corners on the track
        double trackRadius = track.getBackground() == null ? 0 : track.getBackground().getFills().size() > 0 ?
                track.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius() : 0;

        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            double tickLineHeight =  (showTickMarks) ? tickLine.prefHeight(-1) : 0;
            double trackHeight = snapSize(track.prefHeight(-1));
            double trackAreaHeight = Math.max(trackHeight, thumbHeight);
            double totalHeightNeeded = trackAreaHeight  + ((showTickMarks) ? trackToTickGap+tickLineHeight : 0);
            double startY = y + ((h - totalHeightNeeded)/2); // center slider in available height vertically
            trackLength = snapSize(w - thumbWidth);
            trackStart = snapPosition(x + (thumbWidth/2));
            double trackTop = (int)(startY + ((trackAreaHeight-trackHeight)/2));
            thumbTop = (int)(startY + ((trackAreaHeight- thumbHeight)/2));

            positionThumb(false);
            // layout track
            track.resizeRelocate((int)(trackStart - trackRadius),
                                 trackTop ,
                                 (int)(trackLength + trackRadius + trackRadius),
                                 trackHeight);
            // layout tick line
            if (showTickMarks) {
                tickLine.setLayoutX(trackStart);
                tickLine.setLayoutY(trackTop+trackHeight+trackToTickGap);
                tickLine.resize(trackLength, tickLineHeight);
                tickLine.requestAxisLayout();
            } else {
                if (tickLine != null) {
                    tickLine.resize(0,0);
                    tickLine.requestAxisLayout();
                }
                tickLine = null;
            }
        } else {
            double tickLineWidth = (showTickMarks) ? tickLine.prefWidth(-1) : 0;
            double trackWidth = snapSize(track.prefWidth(-1));
            double trackAreaWidth = Math.max(trackWidth,thumbWidth);
            double totalWidthNeeded = trackAreaWidth  + ((showTickMarks) ? trackToTickGap+tickLineWidth : 0) ;
            double startX = x + ((w - totalWidthNeeded)/2); // center slider in available width horizontally
            trackLength = snapSize(h - thumbHeight);
            trackStart = snapPosition(y + (thumbHeight /2));
            double trackLeft = (int)(startX + ((trackAreaWidth-trackWidth)/2));
            thumbLeft = (int)(startX + ((trackAreaWidth-thumbWidth)/2));

            positionThumb(false);
            // layout track
            track.resizeRelocate(trackLeft,
                                 (int)(trackStart - trackRadius),
                                 trackWidth,
                                 (int)(trackLength + trackRadius + trackRadius));
            // layout tick line
            if (showTickMarks) {
                tickLine.setLayoutX(trackLeft+trackWidth+trackToTickGap);
                tickLine.setLayoutY(trackStart);
                tickLine.resize(tickLineWidth, trackLength);
                tickLine.requestAxisLayout();
            } else {
                if (tickLine != null) {
                    tickLine.resize(0,0);
                    tickLine.requestAxisLayout();
                }
                tickLine = null;
            }
        }
    }

    private double minTrackLength() {
        return 2*thumb.prefWidth(-1);
    }

    @Override protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            return (leftInset + minTrackLength() + thumb.minWidth(-1) + rightInset);
        } else {
            return(leftInset + thumb.prefWidth(-1) + rightInset);
        }
    }

    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
         if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            return(topInset + thumb.prefHeight(-1) + bottomInset);
        } else {
            return(topInset + minTrackLength() + thumb.prefHeight(-1) + bottomInset);
        }
    }

    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Slider s = getSkinnable();
        if (s.getOrientation() == Orientation.HORIZONTAL) {
            if(showTickMarks) {
                return Math.max(140, tickLine.prefWidth(-1));
            } else {
                return 140;
            }
        } else {
            return leftInset + Math.max(thumb.prefWidth(-1), track.prefWidth(-1)) +
            ((showTickMarks) ? (trackToTickGap+tickLine.prefWidth(-1)) : 0) + rightInset;
        }
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Slider s = getSkinnable();
        if (s.getOrientation() == Orientation.HORIZONTAL) {
            return topInset + Math.max(thumb.prefHeight(-1), track.prefHeight(-1)) +
             ((showTickMarks) ? (trackToTickGap+tickLine.prefHeight(-1)) : 0)  + bottomInset;
        } else {
            if(showTickMarks) {
                return Math.max(140, tickLine.prefHeight(-1));
            } else {
                return 140;
            }
        }
    }

    @Override protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            return Double.MAX_VALUE;
        } else {
            return getSkinnable().prefWidth(-1);
        }
    }

    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            return getSkinnable().prefHeight(width);
        } else {
            return Double.MAX_VALUE;
        }
    }
    
    private Animation thumbPressAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(100));
        }
        protected void interpolate(double frac) {
        	double padding = 10 + frac * 5;
        	thumb.setStyle("-fx-padding: " + padding + ";");
        }
    };
    
    private Animation thumbReleaseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(100));
        }
        protected void interpolate(double frac) {
        	double padding = 15 - frac * 5;
        	thumb.setStyle("-fx-padding: " + padding + ";");
        }
    };
}
