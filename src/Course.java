import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import java.util.ArrayList;

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

    public void addBodies(ArrayList<CollisionBody> bodies){
        for(CollisionBody body: bodies){
            addBody(body);
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
