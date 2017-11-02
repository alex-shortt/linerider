import javafx.geometry.Bounds;
import javafx.scene.Group;

import java.util.ArrayList;

/**
 * Class representing a collection of physics bodies in the game
 */
class World {
    ArrayList<PhysicsBody> physicsBodies;
    Group root;

    public World(Group newRoot) {
        root = newRoot;
        physicsBodies = new ArrayList<>();
    }

    /**
     * Add a body to this object
     * @param body body to add
     */
    public void addBody(PhysicsBody body){
        physicsBodies.add(body);
    }

    /**
     * Get the bodies of this object
     * @return Bodies from this object
     */
    public ArrayList<PhysicsBody> getBodies(){
        return physicsBodies;
    }

    /**
     * Remove all but one ball, reset that last ball
     */
    public void resetBodies(){
        for(int i = physicsBodies.size() - 1; i > 0; i--){
            root.getChildren().remove(physicsBodies.get(i).getShape());
            physicsBodies.remove(i);
        }
        physicsBodies.get(0).reset(50, 50);
    }

    /**
     * Update each body in this object
     * @param bounds bounding box to check collisions with
     * @param course course storing lines to check collisions with
     */
    public void updateBodies(Bounds bounds, Course course){
        for(PhysicsBody body : physicsBodies){
            //body.checkWorldCollisions(bounds);
            body.checkBodyCollisions(course.getBodies());
            body.updatePhysics();
        }
    }
}
