import javafx.geometry.Bounds;
import javafx.scene.Group;

import java.util.ArrayList;

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
