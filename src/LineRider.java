import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

public class LineRider extends Application {

    private final int LINE_LENGTH = 15;

    private final int HEIGHT = 800;
    private final int WIDTH = 800;
    private static Group root;


    public void start(Stage stage) {
        root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        stage.setScene(scene);
        stage.setTitle("LineRider v1.0");
        stage.titleProperty().bind(scene.widthProperty().asString().concat(" : ").concat(scene.heightProperty().asString()));
        stage.show();

        PhysicsBody body = new PhysicsBody(root, new Circle(15, Color.BLUE), .9, .8, 0.5, 0, 0, -0.15);
        ArrayList<CollisionBody> bodies = new ArrayList<CollisionBody>();

        double[] startCoords = new double[2];
        stage.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            startCoords[0] = event.getX();
            startCoords[1] = event.getY();
        });
        stage.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (Math.hypot(startCoords[0] - event.getX(), startCoords[1] - event.getY()) > LINE_LENGTH) {
                CollisionBody newLine = new CollisionBody(root, new Line(startCoords[0], startCoords[1], event.getX(), event.getY()));
                bodies.add(newLine);

                startCoords[0] = event.getX();
                startCoords[1] = event.getY();
            }
        });

        final Timeline loop = new Timeline(new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {
            //@Override
            public void handle(final ActionEvent t) {
                body.updatePhysics();
                body.checkWorldCollisions(new BoundingBox(0, 0, scene.getWidth(), scene.getHeight()));
                body.checkBodyCollisions(bodies);
            }
        }));

        loop.setCycleCount(Timeline.INDEFINITE);
        loop.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

abstract class Body {
    private Shape shape;

    public Body(Group root, Shape newShape) {
        shape = newShape;
        root.getChildren().addAll(shape);
    }

    public boolean collidesWith(Body body) {
        Shape intersect = Shape.intersect(this.getShape(), body.getShape());
        return (intersect.getBoundsInLocal().getWidth() != -1 || intersect.getBoundsInLocal().getHeight() != -1);
    }

    public Shape getShape() {
        return shape;
    }

    protected double getAngleFromXY(double x, double y) {
        double slope = y / x;
        double angle = Math.toDegrees(Math.atan((slope)));
        if ((y > 0 && x < 0) || (y < 0 && x < 0)) {
            angle += 180;
        }
        return normalizeAngle(angle);
    }

    protected double normalizeAngle(double angle) {
        while (angle < 0) {
            angle += 360;
        }
        while (angle > 360) {
            angle -= 360;
        }
        return angle;
    }
}

class CollisionBody extends Body {
    Line line;

    public CollisionBody(Group root, Shape shape) {
        super(root, shape);
        line = (Line) shape;
    }
}

class PhysicsBody extends Body {
    double velX;
    double velY;
    double frictionX;
    double frictionY;
    double accelX;
    double accelY;
    final double RADIUS;
    Circle ball;

    public PhysicsBody(Group root, Shape shape, double fricX, double fricY, double velocityX, double velocityY, double accelerationX, double accelerationY) {
        super(root, shape);
        shape.relocate(50, 50);
        ball = ((Circle) shape);
        RADIUS = ball.getRadius();
        frictionX = fricX;
        frictionY = fricY;
        velX = velocityX;
        velY = velocityY;
        accelX = accelerationX;
        accelY = accelerationY;
    }

    public void checkWorldCollisions(Bounds bounds) {
        final boolean atRightBorder = getShape().getLayoutX() >= (bounds.getMaxX() - RADIUS);
        final boolean atLeftBorder = getShape().getLayoutX() <= RADIUS;
        final boolean atBottomBorder = getShape().getLayoutY() >= (bounds.getMaxY() - RADIUS);
        final boolean atTopBorder = getShape().getLayoutY() <= RADIUS;
        final double padding = 1.00;

        if (atRightBorder) {
            getShape().setLayoutX(bounds.getMaxX() - (RADIUS * padding));
            velX *= frictionX;
            velX *= -1;
        }
        if (atLeftBorder) {
            getShape().setLayoutX(RADIUS * padding);
            velX *= frictionX;
            velX *= -1;
        }
        if (atBottomBorder) {
            velY *= -1;
            velY *= frictionY;
            getShape().setLayoutY(bounds.getMaxY() - (RADIUS * padding));
        }
        if (atTopBorder) {
            velY *= -1;
            velY *= frictionY;
            getShape().setLayoutY(RADIUS * padding);
        }
    }

    public void checkBodyCollisions(ArrayList<CollisionBody> bodies) {
        for (CollisionBody body : bodies) {
            if (this.collidesWith(body)) {
                Line line = (Line) body.getShape();

                double lineMidX = (line.getStartX() + line.getEndX()) / 2;
                double lineMidY = (line.getStartY() + line.getEndY()) / 2;
                double ballMidX = ball.getLayoutX();
                double ballMidY = ball.getLayoutY();
                double ratioX = (ballMidX - lineMidX) / Math.min(Math.abs(ballMidX - lineMidX), Math.abs(ballMidY - lineMidY));
                double ratioY = (ballMidY - lineMidY) / Math.min(Math.abs(ballMidX - lineMidX), Math.abs(ballMidY - lineMidY));

                double increment = 0.05;
                ball.setFill(Color.YELLOW);
                while (Shape.intersect(line, ball).getBoundsInLocal().getWidth() != -1 || Shape.intersect(line, ball).getBoundsInLocal().getHeight() != -1) {
                    System.out.print("|");
                    ball.setLayoutX(ball.getLayoutX() + (ratioX * increment));
                    ball.setLayoutY(ball.getLayoutY() + (ratioY * increment));
                }
                ball.setFill(Color.BLUE);
                System.out.println("");

                double lineAngle = getAngleFromXY(line.getEndX() - line.getStartX(), line.getStartY() - line.getEndY());
                double velAngle = getAngleFromXY(velX, velY);
                double totalVel = Math.hypot(velX, velY);

                velAngle -= lineAngle;
                velAngle = normalizeAngle(velAngle);
                velAngle = 360 - velAngle;
                velAngle += lineAngle;

                velX = totalVel * Math.cos(Math.toRadians(velAngle)) * frictionX;
                velY = totalVel * Math.sin(Math.toRadians(velAngle)) * frictionY;
            }
        }
    }

    public void updatePhysics() {
        ball.setLayoutX(ball.getLayoutX() + velX);
        ball.setLayoutY(ball.getLayoutY() - velY);

        velY += accelY;
        velX += accelX;
    }
}