import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

public class LineRider extends Application {

    private final int LINE_LENGTH = 100;
    private static Circle ball = new Circle(15, Color.BLUE);
    private final int HEIGHT = 800;
    private final int WIDTH = 800;
    private static Group root;


    public void start(Stage primaryStage) {
        root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        final GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        ball.relocate(100, 100);
        root.getChildren().addAll(ball);
        root.getChildren().add(canvas);


        primaryStage.setScene(scene);
        primaryStage.setTitle("LineRider Prototype v0.1");
        primaryStage.show();

        ArrayList<Line> lines = new ArrayList<>();
        double[] startCoords = new double[2];

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            startCoords[0] = event.getX();
            startCoords[1] = event.getY();
            //sets the starting position of the line, just to get it off the ground
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (Math.hypot(startCoords[0] - event.getX(), startCoords[1] - event.getY()) > LINE_LENGTH) {
                graphicsContext.strokeLine(startCoords[0], startCoords[1], event.getX(), event.getY());
                lines.add(new Line(startCoords[0], startCoords[1], event.getX(), event.getY()));

                startCoords[0] = event.getX();
                startCoords[1] = event.getY();

                //just prints out how big the arraylist is so i know this works
                System.out.println(lines.size());
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            //empty, but you can do shit here if you want to have something upon mouse release
        });


        final Timeline loop = new Timeline(new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {
            double velX = 1;
            double velY = .1;
            double accelY = .01;
            double frictionY = .965;//.965;
            double frictionX = .999;
            final double RADIUS = ball.getRadius();

            //@Override
            public void handle(final ActionEvent t) {
                ball.setLayoutX(ball.getLayoutX() );
                //System.out.println("when does this happen?");
                ball.setLayoutY(ball.getLayoutY() + velY);

                velY += accelY;

                final Bounds bounds = canvas.getBoundsInLocal();
                final boolean atRightBorder = ball.getLayoutX() >= (bounds.getMaxX() - RADIUS);
                final boolean atLeftBorder = ball.getLayoutX() <= RADIUS;
                final boolean atBottomBorder = ball.getLayoutY() >= (bounds.getMaxY() - RADIUS);
                final boolean atTopBorder = ball.getLayoutY() <= RADIUS;
                final double padding = 1.00;
                if (atRightBorder) {
                    ball.setLayoutX(bounds.getMaxX() - (RADIUS * padding));
                    velX *= frictionX;
                    velX *= -1;
                }
                if (atLeftBorder) {
                    ball.setLayoutX(RADIUS * padding);
                    velX *= frictionX;
                    velX *= -1;
                }
                if (atBottomBorder) {
                    //velY *= -1;
                    //velY *= frictionY;
                    ball.setLayoutY(bounds.getMaxY() - (RADIUS * padding));
                }
                if (atTopBorder) {
                    velY *= -1;
                    //velY *= frictionY;
                    ball.setLayoutY(RADIUS * padding);
                }

                Line collide = checkCollisions(ball, lines);
                if (collide != null) {
                    double lineMidX = (collide.getStartX() + collide.getEndX()) / 2;
                    double lineMidY = (collide.getStartY() + collide.getEndY()) / 2;
                    double ballMidX = ball.getLayoutX();
                    double ballMidY = ball.getLayoutY();
                    double ratioX = (ballMidX - lineMidX) / Math.min(Math.abs(ballMidX - lineMidX), Math.abs(ballMidY - lineMidY));
                    double ratioY = (ballMidY - lineMidY) / Math.min(Math.abs(ballMidX - lineMidX), Math.abs(ballMidY - lineMidY));
                    double distX = ballMidX - lineMidX;
                    double distY = ballMidY - lineMidY;
                    double lineSlope = (collide.getStartY() - collide.getEndY()) / (collide.getStartX() - collide.getEndX());
                    double velSlope = velY / velX;
                    double resultAngle = Math.atan((velSlope - lineSlope) / (1 + velSlope * lineSlope));

                    double increment =  .8;
                    ball.setFill(Color.YELLOW);
                    //graphicsContext.strokeLine(lineMidX, lineMidY, ballMidX, ballMidY);
                    while (Shape.intersect(collide, ball).getBoundsInLocal().getWidth() != -1 || Shape.intersect(collide, ball).getBoundsInLocal().getHeight() != -1) {

                        if (velY > 0) {
                            if (lineSlope > 0) {
                                ball.setLayoutX(ball.getLayoutX() + (lineSlope + .0001) + .2);
                                ball.setLayoutY(ball.getLayoutY() + (-lineSlope + .0001) + .2);
                            }
                            else {
                                ball.setLayoutX(ball.getLayoutX() + (lineSlope * .05 + .0001) - .2);
                                //System.out.println("moving LEFT DOWN to: " + (ball.getLayoutX() + (lineSlope + .0001) - .2));
                                ball.setLayoutY(ball.getLayoutY() + (-lineSlope * .05 + .0001) - .2);
                            }
                        }
                        else {
                            velY*=0;
                            ball.setLayoutY(ball.getLayoutY() + 10);
                            //System.out.println(velY);
                        }
                    }
                    ball.setFill(Color.BLUE);

                    //velX *= Math.cos(resultAngle) * frictionX;
                    //velY *= -Math.sin(resultAngle)  * frictionY;
                }
            }
        }));

        loop.setCycleCount(Timeline.INDEFINITE);
        loop.play();
    }

    public Line checkCollisions(Circle ball, ArrayList<Line> lines) {
        ball.setFill(Color.BLUE);
        for (Line line : lines) {
            Shape intersect = Shape.intersect(line, ball);
            if (intersect.getBoundsInLocal().getWidth() != -1 || intersect.getBoundsInLocal().getHeight() != -1) {
                ball.setFill(Color.RED);
                //System.out.println("dink");
                return line;
            }
            else {
                //System.out.println("donk");
            }
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}