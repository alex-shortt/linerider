import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import java.util.ArrayList;

/**
 * Class to handle collection of Lines in game
 */
class Course {
    ArrayList<CollisionBody> collisionBodies;
    Group root;

    public Course(Group newRoot) {
        root = newRoot;
        collisionBodies = new ArrayList<>();
    }

    /**
     * Add a line to this object
     * @param body body to add to object
     */
    public void addBody(CollisionBody body) {
        collisionBodies.add(body);
    }

    /**
     * Get list of lines in this object
     * @return lines in this object
     */
    public ArrayList<CollisionBody> getBodies() {
        return collisionBodies;
    }

    /**
     * Remove all the lines from this object
     */
    public void clear() {
        for (int i = collisionBodies.size() - 1; i >= 0; i--) {
            CollisionBody line = collisionBodies.get(i);
            root.getChildren().remove(line.getLine());
            collisionBodies.remove(line);
        }
    }

    /**
     * Add a list of lines, mainly used for loading a course
     * @param bodies collection of bodies to add to object
     */
    public void addBodies(ArrayList<CollisionBody> bodies){
        for(CollisionBody body: bodies){
            addBody(body);
        }
    }

    /**
     * Erase line from this course at specific x and y coordinates
     * @param x event x
     * @param y event y
     */
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
