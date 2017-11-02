import javafx.scene.Group;
import javafx.scene.control.ToolBar;
import javafx.scene.shape.Shape;

public class Camera {
    private double panX = 0;
    private double panY = 0;
    private Group root;
    private ToolBar tools;

    public Camera(Group newRoot, ToolBar newTools){
        root = newRoot;
        tools = newTools;
    }

    public void setPan(double x, double y){
        panX = x;
        panY = y;

        root.setLayoutX(x);
        root.setLayoutY(y);

        tools.setLayoutX(-x);
        tools.setLayoutY(-y);
    }

    public void changePan(double startX, double startY, double endX, double endY){
        double x = panX + endX - startX;
        double y = panY + endY - startY;
        setPan(x, y);
    }

    public double getPanX(){
        return panX;
    }

    public double getPanY(){
        return panY;
    }

    public void followBall(World world){
        PhysicsBody ball = world.getBodies().get(0);
        Shape ballShape = ball.getShape();
        double velScale = 1.1;

        setPan(-ballShape.getLayoutX() + (root.getScene().getWidth() / 2) - (ball.getVelX() * velScale), -ballShape.getLayoutY() + (root.getScene().getHeight() / 2) - (ball.getVelY() * velScale));
    }
}
