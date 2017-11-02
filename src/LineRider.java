import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
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


enum PenType {
    PENCIL, ERASER, BALLPLACER, HAND
}

public class LineRider extends Application {
    private final int LINE_LENGTH = 15;
    private boolean physicsRunning = false;
    private boolean lockCamera = true;
    private static Canvas canvas;
    private static Group root;
    private static PenType penType = PenType.PENCIL;
    private static ToolBar tools = new ToolBar();

    //TODO: Add background image
    //TODO: Allow shortcuts (get rid of toolbar focus)
    //TODO: Add Face Texture
    //TODO: Add rotation physics

    public void start(Stage stage) {
        //set up root variables
        root = new Group();
        canvas = new Canvas(800, 800);
        tools.setPrefWidth(5000);
        tools.setFocusTraversable(false);

        //set up all necessary variables
        Scene scene = new Scene(root, 800, 800);
        World world = new World(root);
        Course course = new Course(root);
        CourseHandler courseHandler = new CourseHandler(root);
        Camera camera = new Camera(root, tools);

        //initialize world and cursor
        root.getChildren().addAll(canvas);
        root.getChildren().add(tools);
        stage.setScene(scene);
        world.addBody(new PhysicsBody(root, new Circle(15, Color.BLUE), .999, .9, 0, -0.15));
        scene.setCursor(new ImageCursor(new Image("/assets/pencil.png"), 150, 700));

        //toolbar setup
        Button btnDraw = new Button("Draw");
        Button btnErase = new Button("Erase");
        Button btnAdd = new Button("Add Ball");
        Button btnPan = new Button("Pan");
        Button btnClear = new Button("Clear");
        Button btnPlay = new Button("Play");
        Button btnReset = new Button("Reset");
        Button btnSave = new Button("Save");
        Button btnLoad = new Button("Load");
        Button btnLock = new Button("Unlock Camera");
        tools.getItems().addAll(
                btnDraw,
                btnErase,
                btnAdd,
                btnPan,
                btnClear,
                new Separator(),
                btnPlay,
                btnReset,
                btnLock,
                new Separator(),
                btnSave,
                btnLoad
        );
        btnPlay.setOnAction(e -> {
            physicsRunning = !physicsRunning;
            if (physicsRunning) {
                btnPlay.setText("Pause");
            } else {
                btnPlay.setText("Play");
            }
        });
        btnClear.setOnAction(e -> {
            course.clear();
            world.resetBodies();
        });
        btnReset.setOnAction(e -> {
            world.resetBodies();
            camera.setPan(0, 0);

            physicsRunning = false;
            btnPlay.setText("Play");
        });
        btnDraw.setOnAction(e -> {
            penType = PenType.PENCIL;
            scene.setCursor(new ImageCursor(new Image("/assets/pencil.png"), 150, 700));
        });
        btnLock.setOnAction(e -> {
            lockCamera = !lockCamera;
            if (lockCamera) {
                btnLock.setText("Unlock Camera");
            } else {
                btnLock.setText("Lock Camera");
            }
        });
        btnErase.setOnAction(e -> {
            penType = PenType.ERASER;
            scene.setCursor(new ImageCursor(new Image("/assets/eraser.png"), 150, 700));
        });
        btnPan.setOnAction(e -> {
            penType = PenType.HAND;
            scene.setCursor(new ImageCursor(new Image("/assets/hand.png"), 10, 10));
        });
        btnAdd.setOnAction(e -> {
            penType = PenType.BALLPLACER;
            scene.setCursor(new ImageCursor(new Image("/assets/ball.png"), 10, 10));
        });
        btnSave.setOnAction(e -> {
            try {
                courseHandler.saveCourse(course.getBodies());
            } catch (IOException err) {
                err.printStackTrace();
            }
        });
        btnLoad.setOnAction(e -> {
            try {
                ArrayList<CollisionBody> bodies = courseHandler.loadCourse();
                if (bodies == null) {
                    JOptionPane.showMessageDialog(new JOptionPane(), "Could not find World");
                    return;
                }
                course.clear();
                course.addBodies(bodies);
            } catch (IOException err) {
                err.printStackTrace();
            }
        });

        //mouse events
        double[] mouseStartCoords = new double[2];
        stage.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            mouseStartCoords[0] = event.getX();
            mouseStartCoords[1] = event.getY();
        });
        stage.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (penType == PenType.PENCIL && Math.hypot(mouseStartCoords[0] - event.getX(), mouseStartCoords[1] - event.getY()) > LINE_LENGTH) {
                CollisionBody newLine = new CollisionBody(root, new Line(mouseStartCoords[0] - camera.getPanX(), mouseStartCoords[1] - camera.getPanY(), event.getX() - camera.getPanX(), event.getY() - camera.getPanY()));
                course.addBody(newLine);

                mouseStartCoords[0] = event.getX();
                mouseStartCoords[1] = event.getY();
            } else if (penType == PenType.ERASER) {
                course.erase(event.getX(), event.getY());
            } else if (penType == PenType.HAND) {
                camera.changePan(mouseStartCoords[0], mouseStartCoords[1], event.getX(), event.getY());
                mouseStartCoords[0] = event.getX();
                mouseStartCoords[1] = event.getY();
            }
        });
        stage.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            if (penType == PenType.BALLPLACER) {
                Random r = new Random();
                PhysicsBody newbody = new PhysicsBody(root, new Circle(15, Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255))), .999, .9, 0, -0.15);
                newbody.reset((int) (event.getX() - camera.getPanX()), (int) (event.getY() - camera.getPanY()));
                world.addBody(newbody);
            } else if (penType == PenType.ERASER) {
                course.erase(event.getX(), event.getY());
            }
        });

        //Physics Loop
        final Timeline loop = new Timeline(new KeyFrame(Duration.millis(10), t -> {
            if (physicsRunning) {
                world.updateBodies(new BoundingBox(0, 0, scene.getWidth(), scene.getHeight()), course);
                if(lockCamera) camera.followBall(world);
            }
        }));
        loop.setCycleCount(Timeline.INDEFINITE);
        loop.play();

        //begin
        stage.setScene(scene);
        stage.setTitle("LineRider v2.2");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}



