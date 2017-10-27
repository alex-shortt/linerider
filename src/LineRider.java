import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static javafx.scene.input.KeyCode.SPACE;

enum PenType {
    PENCIL, ERASER, BALLPLACER, HAND
}

enum ItemAction {
    SET_PENCIL, SET_ERASER, SET_BALLPLACER, SET_HAND, TRASH
}

public class LineRider extends Application {
    private final int LINE_LENGTH = 15;
    private boolean physicsRunning = false;
    private static Canvas canvas;
    private static Group root;
    private static PenType penType = PenType.PENCIL;

    //TODO: Panning (hand tool) - toolbar
    //TODO: Add Face Texture
    //TODO: Add rotation physics

    public void start(Stage stage) {
        Button btnDraw = new Button("Draw");
        Button btnErase = new Button("Erase");
        Button btnAdd = new Button("Add Ball");
        Button btnPan = new Button("Pan");
        Button btnTrash = new Button("Trash");
        Button btnPlay = new Button("Play");
        Button btnReset = new Button("Reset");
        Button btnZoom = new Button("Zoom");
        ToolBar tools = new ToolBar();
        tools.getItems().addAll (
                btnDraw,
                btnErase,
                btnAdd,
                btnPan,
                btnTrash,
                btnZoom,
                new Separator(),
                btnPlay,
                btnReset
        );
        tools.setPrefWidth(1154);
        root = new Group();
        Scene scene = new Scene(root, 800, 800);
        canvas = new Canvas(800, 800);
        root.getChildren().addAll(canvas);
        root.getChildren().add(tools);
        stage.setScene(scene);
        World world = new World(root);
        Course course = new Course(root);
        CourseHandler courseHandler = new CourseHandler(root);

        //add first ball
        world.addBody(new PhysicsBody(root, new Circle(15, Color.BLUE), .999, .9, 0, -0.15));

        ArrayList<ToolBarItem> toolbar = new ArrayList<>();
        double spacing = 20;
        final double TOOL_HEIGHT = 30;
        ToolBarItem pencil = new ToolBarItem(canvas, "/assets/pencil.png", 150, 5, TOOL_HEIGHT, ItemAction.SET_PENCIL);
        Image penIm = new Image("/assets/pencil.png");
        Image erIm = new Image("/assets/eraser.png");
        Image ballIm = new Image("/assets/ball.png");
        Image handIm = new Image("/assets/hand.png");
        scene.setCursor(new ImageCursor(penIm, 150, 700));
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

        btnPlay.setOnAction(e -> {
            physicsRunning = !physicsRunning;
            if (physicsRunning) {
                btnPlay.setText("Pause");
            }  else {
                btnPlay.setText("Play");
            }
        });

        btnTrash.setOnAction(e -> {
            course.clear();
            world.resetBodies();
        });

        btnReset.setOnAction(e -> world.resetBodies());

        btnDraw.setOnAction(e -> {
            penType = PenType.PENCIL;
            scene.setCursor(new ImageCursor(penIm, 150, 700));
        });

        btnErase.setOnAction(e -> {
            penType = PenType.ERASER;
            scene.setCursor(new ImageCursor(erIm, 150, 700));
        });
        btnPan.setOnAction(e -> {
            penType = PenType.HAND;
            scene.setCursor(new ImageCursor(handIm, 10, 10));
        });
        btnAdd.setOnAction(e -> {
            penType = PenType.BALLPLACER;
            scene.setCursor(new ImageCursor(ballIm, 10, 10));
        });


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
            } else if (penType == PenType.HAND) {
                root.setLayoutX(root.getLayoutX() + (event.getX() - startCoords[0]));
                root.setLayoutY(root.getLayoutY() + (event.getY() - startCoords[1]));

                startCoords[0] = event.getX();
                startCoords[1] = event.getY();
            }
        });
        stage.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
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
                Random r = new Random();
                PhysicsBody newbody = new PhysicsBody(root, new Circle(15, Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255))), .999, .9, 0, -0.15);
                newbody.reset((int) event.getX(), (int) event.getY());
                world.addBody(newbody);
            } else if (!clickedToolbar && penType == PenType.ERASER) {
                course.erase(event.getX(), event.getY());
            } else if (!clickedToolbar && penType == PenType.HAND) {

            }
        });

        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case SPACE:
                    physicsRunning = !physicsRunning;
                    if (!physicsRunning) {
                        world.resetBodies();
                    }
                    break;
                case ALT:
                    try {
                        courseHandler.saveCourse(course.getBodies());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case P:
                    try {
                        ArrayList<CollisionBody> bodies = courseHandler.loadCourse();
                        if(bodies == null){
                            JOptionPane.showMessageDialog(new JOptionPane(), "Could not find World");
                            break;
                        }
                        course.clear();
                        course.addBodies(bodies);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
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



