import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
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

enum PenType {
    PENCIL, ERASER, BALLPLACER, HAND
}

enum ItemAction {
    SET_PENCIL, SET_ERASER, SET_BALLPLACER, SET_HAND, TRASH
}

public class LineRider extends Application {
    private final int LINE_LENGTH = 24;
    private boolean physicsRunning = false;
    private static Canvas canvas;
    private static Group root;
    private static PenType penType = PenType.PENCIL;

    //TODO: Panning (hand tool) - toolbar
    //TODO: ball placer tool (place ball where you click) - toolbar
    //TODO: clear balls tool - toolbar
    //TODO: Add Face Texture
    //TODO: Add rotation physics

    public void start(Stage stage) {
        root = new Group();
        Scene scene = new Scene(root, 800, 800);
        canvas = new Canvas(800, 800);
        root.getChildren().addAll(canvas);

        World world = new World(root);
        Course course = new Course(root);

        //add first ball
        world.addBody(new PhysicsBody(root, new Circle(15, Color.BLUE), .999, .9, 0, -0.15));

        //toolbar
        ArrayList<ToolBarItem> toolbar = new ArrayList<>();
        double spacing = 20;
        final double TOOL_HEIGHT = 30;
        ToolBarItem pencil = new ToolBarItem(canvas, "/assets/pencil.png", 150, 5, TOOL_HEIGHT, ItemAction.SET_PENCIL);
        toolbar.add(pencil);
        pencil.setHilighted(true);
        ToolBarItem eraser = new ToolBarItem(canvas, "/assets/eraser.png", pencil.getEndX() + spacing, 5, TOOL_HEIGHT, ItemAction.SET_ERASER);
        toolbar.add(eraser);
        ToolBarItem ballPlacer = new ToolBarItem(canvas, "/assets/ball.png", eraser.getEndX() + spacing, 5, TOOL_HEIGHT, ItemAction.SET_BALLPLACER);
        toolbar.add(ballPlacer);
        ToolBarItem hand = new ToolBarItem(canvas, "/assets/hand.png", ballPlacer.getEndX() + spacing, 5, TOOL_HEIGHT, ItemAction.SET_HAND);
        toolbar.add(hand);
        ToolBarItem trash = new ToolBarItem(canvas, "/assets/trash.png", hand.getEndX() + spacing, 5, TOOL_HEIGHT, ItemAction.TRASH);
        toolbar.add(trash);


        //Drawing collisionBodies
        double[] startCoords = new double[2];
        stage.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            startCoords[0] = event.getX();
            startCoords[1] = event.getY();
        });
        stage.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (penType == PenType.PENCIL && Math.hypot(startCoords[0] - event.getX(), startCoords[1] - event.getY()) > LINE_LENGTH) {
                CollisionBody newLine = new CollisionBody(root, new Line(startCoords[0], startCoords[1], event.getX(), event.getY()));
                course.addBody(newLine);

                startCoords[0] = event.getX();
                startCoords[1] = event.getY();
            } else if (penType == PenType.ERASER) {
                course.erase(event.getX(), event.getY());
            }
        });
        stage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            boolean clickedToolbar = false;
            for (ToolBarItem tool : toolbar) {
                if (tool.getBounds().contains(new Point2D(event.getX(), event.getY()))) {
                    //click event
                    switch (tool.getActionType()) {
                        case SET_PENCIL:
                            penType = PenType.PENCIL;
                            for (ToolBarItem tool2 : toolbar) {
                                tool2.setHilighted(false);
                            }
                            tool.setHilighted(true);
                            break;
                        case SET_ERASER:
                            penType = PenType.ERASER;
                            for (ToolBarItem tool2 : toolbar) {
                                tool2.setHilighted(false);
                            }
                            tool.setHilighted(true);
                            break;
                        case SET_HAND:
                            penType = PenType.HAND;
                            for (ToolBarItem tool2 : toolbar) {
                                tool2.setHilighted(false);
                            }
                            tool.setHilighted(true);
                            break;
                        case SET_BALLPLACER:
                            penType = PenType.BALLPLACER;
                            for (ToolBarItem tool2 : toolbar) {
                                tool2.setHilighted(false);
                            }
                            tool.setHilighted(true);
                            break;
                        case TRASH:
                            course.clear();
                            world.resetBodies();
                            break;
                    }
                    clickedToolbar = true;
                }
            }
            if (!clickedToolbar && penType == PenType.BALLPLACER) {
                PhysicsBody newbody = new PhysicsBody(root, new Circle(15, Color.BLUE), .999, .9, 0, -0.15);
                newbody.reset((int) event.getX(), (int) event.getY());
                world.addBody(newbody);
            } else if (!clickedToolbar && penType == PenType.ERASER) {
                course.erase(event.getX(), event.getY());
            } else if (!clickedToolbar && penType == PenType.HAND) {

            }
        });

        //Stop and Start keypresses
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode().toString()) {
                case "SPACE":
                    physicsRunning = !physicsRunning;
                    if (!physicsRunning) {
                        world.resetBodies();
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
                world.updateBodies(new BoundingBox(0, 0, scene.getWidth(), scene.getHeight()), course);
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

class Course {
    ArrayList<CollisionBody> collisionBodies;
    Group root;

    public Course(Group newRoot) {
        root = newRoot;
        collisionBodies = new ArrayList<>();
    }

    public void addBody(CollisionBody body) {
        collisionBodies.add(body);
    }

    public ArrayList<CollisionBody> getBodies() {
        return collisionBodies;
    }

    public void clear() {
        for (int i = collisionBodies.size() - 1; i >= 0; i--) {
            CollisionBody line = collisionBodies.get(i);
            root.getChildren().remove(line.getLine());
            collisionBodies.remove(line);
        }
    }

    public void erase(double x, double y) {
        Shape test = new Circle(x, y, 10);
        for (int i = collisionBodies.size() - 1; i >= 0; i--) {
            CollisionBody line = collisionBodies.get(i);
            Shape intersect = Shape.intersect(line.getShape(), test);
            if (intersect.getBoundsInLocal().getWidth() != -1 || intersect.getBoundsInLocal().getHeight() != -1) {
                root.getChildren().remove(line.getLine());
                collisionBodies.remove(line);
            }
        }
    }
}

class World {
    ArrayList<PhysicsBody> physicsBodies;
    Group root;

    public World(Group newRoot) {
        root = newRoot;
        physicsBodies = new ArrayList<>();
    }

    public void addBody(PhysicsBody body){
        physicsBodies.add(body);
    }

    public ArrayList<PhysicsBody> getBodies(){
        return physicsBodies;
    }

    public void resetBodies(){
        for(int i = physicsBodies.size() - 1; i > 0; i--){
            root.getChildren().remove(physicsBodies.get(i).getShape());
            physicsBodies.remove(i);
        }
        physicsBodies.get(0).reset(50, 50);
    }

    public void updateBodies(Bounds bounds, Course course){
        for(PhysicsBody body : physicsBodies){
            body.checkWorldCollisions(bounds);
            body.checkBodyCollisions(course.getBodies());
            body.updatePhysics();
        }
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

    public Bounds getBounds() {
        return new BoundingBox(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
    }

    public Line getLine() {
        return line;
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

class ToolBarItem {
    Image image;
    double x;
    double y;
    double height;
    double width;
    Canvas canvas;
    boolean isHilighted = false;
    ItemAction action;

    public ToolBarItem(Canvas newCanvas, String url, double posX, double posY, double setHeight, ItemAction itemAction) {
        image = new Image(url);
        x = posX;
        y = posY;
        height = setHeight;
        width = image.getWidth() * (height / image.getHeight());
        canvas = newCanvas;
        action = itemAction;

        canvas.getGraphicsContext2D().drawImage(image, x, y, width, height);
    }

    public void hide() {
        canvas.getGraphicsContext2D().clearRect(x, y, width, height);
    }

    public void show() {
        hide();
        canvas.getGraphicsContext2D().drawImage(image, x, y, width, height);
    }

    public ItemAction getActionType() {
        return action;
    }

    public void toggleImage(String newUrl) {
        //create new image, hide current, set image, show again
        //used for play/pause
    }

    public Bounds getBounds() {
        return new BoundingBox(x, y, width, height);
    }

    public double getEndX() {
        return x + width;
    }

    public boolean isHilighted() {
        return isHilighted;
    }

    public void setHilighted(boolean visible) {
        isHilighted = visible;
        if (visible) {
            int topMargin = 10;
            canvas.getGraphicsContext2D().strokeLine(x, y + height + topMargin, x + width, y + height + topMargin);
        } else {
            int topMargin = 10;
            int padding = 3;
            canvas.getGraphicsContext2D().clearRect(x - padding, y + height + topMargin - padding, x + width + padding, y + height + topMargin + padding);
        }
    }
}