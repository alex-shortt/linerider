import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.function.Function;

public class LineRider extends Application {

    private final int LINE_LENGTH = 30;
    private boolean physicsRunning = false;
    private static Canvas canvas;
    private static Group root;

    //TODO: Add Eraser - toolbar
    //TODO: Add Clear Button - toolbar
    //TODO: Panning (hand tool) - toolbar
    //TODO: ball placer tool (place ball where you click) - toolbar
    //TODO: clear balls tool - toolbar
    //TODO: Add Face Texture
    //TODO: Add rotation physics

    public void start(Stage stage) {
        root = new Group();
        Scene scene = new Scene(root, 800, 800);
        canvas = new Canvas(800,800);
        root.getChildren().addAll(canvas);

        PhysicsBody body = new PhysicsBody(root, new Circle(15, Color.BLUE), .999, .9, 0, -0.15);
        ArrayList<CollisionBody> bodies = new ArrayList<>();

        ArrayList<ToolBarItem> toolbar = new ArrayList<>();
        double spacing = 20;
        ToolBarItem pencil = new ToolBarItem(canvas, "/assets/pencil.png", 150, 5, 30);
        toolbar.add(pencil);
        ToolBarItem eraser = new ToolBarItem(canvas, "/assets/eraser.png", pencil.getEndX() + spacing, 5, 30);
        toolbar.add(eraser);
        ToolBarItem pencil2 = new ToolBarItem(canvas, "/assets/pencil.png", eraser.getEndX() + spacing, 5, 30);
        toolbar.add(pencil2);

        stage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            for(ToolBarItem tool : toolbar){
                if(tool.getBounds().contains(new Point2D(event.getX(), event.getY()))){
                    tool.setHilighted(true);
                }
            }
        });

        //Drawing lines
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

        //Stop and Start keypresses
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode().toString()) {
                case "SPACE":
                    physicsRunning = !physicsRunning;
                    if (!physicsRunning) {
                        body.reset(50, 50);
                    }
                    break;
                default:
                    break;
            }
            System.out.println(event.getCode());
        });

        //Physics Loop
        final Timeline loop = new Timeline(new KeyFrame(Duration.millis(10), t -> {
            if (physicsRunning) {
                body.updatePhysics();
                body.checkWorldCollisions(new BoundingBox(0, 0, scene.getWidth(), scene.getHeight()));
                body.checkBodyCollisions(bodies);
            }
        }));
        loop.setCycleCount(Timeline.INDEFINITE);
        loop.play();

        //begin
        stage.setScene(scene);
        stage.setTitle("LineRider v1.1");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

abstract class Body {
    private Shape shape;
    protected Group root;

    public Body(Group passRoot, Shape newShape) {
        shape = newShape;
        root = passRoot;
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
    double velX = 0;
    double velY = 0;
    double frictionX;
    double frictionY;
    double accelX;
    double accelY;
    final double RADIUS;
    Circle ball;

    public PhysicsBody(Group root, Shape shape, double fricX, double fricY, double accelerationX, double accelerationY) {
        super(root, shape);
        shape.relocate(50, 50);
        ball = ((Circle) shape);
        RADIUS = ball.getRadius();
        frictionX = fricX;
        frictionY = fricY;
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
                while (collidesWith(body)) {
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

    public void reset(int x, int y) {
        ball.relocate(x, y);
        velX = 0;
        velY = 0;
    }
}

class ToolBarItem{
    Image image;
    double x;
    double y;
    double height;
    double width;
    Canvas canvas;
    boolean isHilighted = false;

    public ToolBarItem(Canvas newCanvas,  String url, double posX, double posY, double setHeight){
        image = new Image(url);
        x = posX;
        y = posY;
        height = setHeight;
        width = image.getWidth() * (height / image.getHeight());
        canvas = newCanvas;

        canvas.getGraphicsContext2D().drawImage(image, x, y, width, height);
    }

    public void hide(){
        canvas.getGraphicsContext2D().clearRect(x, y, width, height);
    }

    public void show(){
        hide();
        canvas.getGraphicsContext2D().drawImage(image, x, y, width, height);
    }

    public void clickEvent(){

    }

    public Bounds getBounds(){
        return new BoundingBox(x, y, width, height);
    }

    public double getEndX(){
        return x + width;
    }

    public boolean isHilighted() {
        return isHilighted;
    }

    public void setHilighted(boolean visible){
        isHilighted = visible;
        if(visible){
            int topMargin = 10;
            canvas.getGraphicsContext2D().strokeLine(x, y + height + topMargin, x + width, y + height + topMargin);
        }
        else{
            int topMargin = 10;
            int padding = 3;
            canvas.getGraphicsContext2D().clearRect(x - padding, y + height + topMargin - padding, x + width + padding, y + height + topMargin + padding);
        }
    }
}