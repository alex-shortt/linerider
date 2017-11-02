import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

/**
 * Type of body representing a line in the game
 */
public class CollisionBody extends Body {
    Line line;

    public CollisionBody(Group root, Shape shape) {
        super(root, shape);
        line = (Line) shape;
    }

    /**
     * Get the Line object
     * @return Line object
     */
    public Line getLine() {
        return line;
    }
}