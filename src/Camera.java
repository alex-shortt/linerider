import javafx.scene.Group;
import javafx.scene.control.ToolBar;
import javafx.scene.shape.Shape;

/**
 * The Camera class controls the panning of the game
 */
public class Camera {
    private double panX = 0;
    private double panY = 0;
    private Group root;
    private ToolBar tools;

    public Camera(Group newRoot, ToolBar newTools){
        root = newRoot;
        tools = newTools;
    }

    /**
     * Set the pan of the game
     * @param x x pan
     * @param y y pan
     */
    public void setPan(double x, double y){
        panX = x;
        panY = y;

        root.setLayoutX(x);
        root.setLayoutY(y);

        tools.setLayoutX(-x);
        tools.setLayoutY(-y);
    }

    /**
     * Change pan based on drag start/end coordinates
     * @param startX drag start x
     * @param startY drag start y
     * @param endX drag end x
     * @param endY drag end y
     */
    public void changePan(double startX, double startY, double endX, double endY){
        double x = panX + endX - startX;
        double y = panY + endY - startY;
        setPan(x, y);
    }

    /**
     * Get the x pan value
     * @return x pan value
     */
    public double getPanX(){
        return panX;
    }

    /**
     * Get the y pan value
     * @return y pan value
     */
    public double getPanY(){
        return panY;
    }

    /**
     * Sets the pan of the game to center on the ball
     * @param world world to follow the ball
     */
    public void followBall(World world){
        PhysicsBody ball = world.getBodies().get(0);
        Shape ballShape = ball.getShape();
        double velScale = 1.1;

        setPan(-ballShape.getLayoutX() + (root.getScene().getWidth() / 2) - (ball.getVelX() * velScale), -ballShape.getLayoutY() + (root.getScene().getHeight() / 2) - (ball.getVelY() * velScale));
    }
}
