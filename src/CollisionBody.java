import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

public class CollisionBody extends Body {
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