import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LineRider extends Application{
    public static void main(String[] args){
        launch(args);
    }
    //----------------------------------------------------//
    public static Circle circle;
    public static Rectangle rectangle;
    public static Pane canvas;
    private long counter = 0;
    double X = 0;
    double Y = 0;

    final static int WIDTH = 800;
    final static int HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) throws Exception {
        canvas = new Pane();
        Scene scene = new Scene(canvas, WIDTH,WIDTH);

        primaryStage.setTitle("Bouncing balls at angels");
        primaryStage.setScene(scene);
        primaryStage.show();

        circle = new Circle(15, Color.BLUE);
        circle.relocate(200,200);

        //rectangle = new Rectangle(750, 500);
        //rectangle.relocate(100,100);

        //canvas.getChildren().addAll(rectangle);
        canvas.getChildren().addAll(circle);

        final Timeline loop = new Timeline(new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {

            double deltaX = .5;
            double deltaY = 0;
            double gravity = .15;
            double Yfriction = .07;
            final double RADIUS = circle.getRadius();

            //@Override
            public void handle(final ActionEvent t) {
                circle.setLayoutX(circle.getLayoutX() + deltaX);
                circle.setLayoutY(circle.getLayoutY() + deltaY);

                deltaY += gravity;

                final Bounds bounds = canvas.getBoundsInLocal();
                final boolean atRightBorder = circle.getLayoutX() >= (bounds.getMaxX() - RADIUS);
                final boolean atLeftBorder = circle.getLayoutX() <= RADIUS;
                final boolean atBottomBorder = circle.getLayoutY() >= (bounds.getMaxY() - RADIUS);
                final boolean atTopBorder = circle.getLayoutY() <= RADIUS;

                if (atRightBorder || atLeftBorder) {
                    deltaX *= -1;
                }
                if (atBottomBorder || atTopBorder) {
                    deltaY *= -1;
                    if(deltaY<0)deltaY+=Yfriction;
                    if(deltaY>0)deltaY-=Yfriction;
                    if(atBottomBorder){
                        circle.setLayoutY(bounds.getMaxY() - RADIUS);
                    }
                }
            }
        }));
        loop.setCycleCount(Timeline.INDEFINITE);
        loop.play();
    }
}
