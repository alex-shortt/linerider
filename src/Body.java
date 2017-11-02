import javafx.scene.Group;
import javafx.scene.shape.Shape;

/**
 * Body to represent a basic node in the game-- either a ball or a line
 */
public abstract class Body {
    private Shape shape;
    protected Group root;

    public Body(Group passRoot, Shape newShape) {
        shape = newShape;
        root = passRoot;
        root.getChildren().addAll(shape);
    }

    /**
     * Check whether one body is colliding with another
     * @param body Body to check collision with
     * @return boolean-- are two bodies colliding?
     */
    public boolean collidesWith(Body body) {
        Shape intersect = Shape.intersect(this.getShape(), body.getShape());
        return (intersect.getBoundsInLocal().getWidth() != -1 || intersect.getBoundsInLocal().getHeight() != -1);
    }

    /**
     * Get the shape of the body
     * @return shape of this body
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * Return an angle from 0-360 based on x and y vectors
     * @param x x vector
     * @param y y vector
     * @return angle from 0-360
     */
    protected double getAngleFromXY(double x, double y) {
        double slope = y / x;
        double angle = Math.toDegrees(Math.atan((slope)));
        if ((y > 0 && x < 0) || (y < 0 && x < 0)) {
            angle += 180;
        }
        return normalizeAngle(angle);
    }

    /**
     * Convert angle to stay within 0 and 360
     * @param angle angle to normalize
     * @return angle from 0-360
     */
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