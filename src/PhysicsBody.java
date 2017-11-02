import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

import java.util.ArrayList;

public class PhysicsBody extends Body {
    double velX = 0;
    double velY = 0;
    double frictionX;
    double frictionY;
    double accelX;
    double accelY;
    final double RADIUS;
    Circle ball;

    public PhysicsBody(Group root, Shape shape, double fricX, double fricY, double accelerationX, double accelerationY) {
        super(root, shape);
        shape.relocate(50, 50);
        ball = ((Circle) shape);
        RADIUS = ball.getRadius();
        frictionX = fricX;
        frictionY = fricY;
        accelX = accelerationX;
        accelY = accelerationY;
    }

    public void checkWorldCollisions(Bounds bounds) {
        final boolean atRightBorder = getShape().getLayoutX() >= (bounds.getMaxX() - RADIUS);
        final boolean atLeftBorder = getShape().getLayoutX() <= RADIUS;
        final boolean atBottomBorder = getShape().getLayoutY() >= (bounds.getMaxY() - RADIUS);
        final boolean atTopBorder = getShape().getLayoutY() <= RADIUS;
        final double padding = 1.00;

        if (atRightBorder) {
            getShape().setLayoutX(bounds.getMaxX() - (RADIUS * padding));
            velX *= frictionX;
            velX *= -1;
        }
        if (atLeftBorder) {
            getShape().setLayoutX(RADIUS * padding);
            velX *= frictionX;
            velX *= -1;
        }
        if (atBottomBorder) {
            velY *= -1;
            velY *= frictionY;
            getShape().setLayoutY(bounds.getMaxY() - (RADIUS * padding));
        }
        if (atTopBorder) {
            velY *= -1;
            velY *= frictionY;
            getShape().setLayoutY(RADIUS * padding);
        }
    }

    public void checkBodyCollisions(ArrayList<CollisionBody> bodies) {
        for (CollisionBody body : bodies) {
            if (this.collidesWith(body)) {
                Line line = (Line) body.getShape();

                double lineMidX = (line.getStartX() + line.getEndX()) / 2;
                double lineMidY = (line.getStartY() + line.getEndY()) / 2;
                double ballMidX = ball.getLayoutX();
                double ballMidY = ball.getLayoutY();
                double ratioX = (ballMidX - lineMidX) / Math.min(Math.abs(ballMidX - lineMidX), Math.abs(ballMidY - lineMidY));
                double ratioY = (ballMidY - lineMidY) / Math.min(Math.abs(ballMidX - lineMidX), Math.abs(ballMidY - lineMidY));

                double increment = 0.05;
                while (collidesWith(body)) {
                    System.out.print("|");
                    ball.setLayoutX(ball.getLayoutX() + (ratioX * increment));
                    ball.setLayoutY(ball.getLayoutY() + (ratioY * increment));
                }
                System.out.println("");

                double lineAngle = getAngleFromXY(line.getEndX() - line.getStartX(), line.getStartY() - line.getEndY());
                double velAngle = getAngleFromXY(velX, velY);
                double totalVel = Math.hypot(velX, velY);

                velAngle -= lineAngle;
                velAngle = normalizeAngle(velAngle);
                velAngle = 360 - velAngle;
                velAngle += lineAngle;

                velX = totalVel * Math.cos(Math.toRadians(velAngle)) * frictionX;
                velY = totalVel * Math.sin(Math.toRadians(velAngle)) * frictionY;
            }
        }
    }

    public double getVelX(){
        return velX;
    }

    public double getVelY(){
        return velY;
    }

    public void updatePhysics() {
        ball.setLayoutX(ball.getLayoutX() + velX);
        ball.setLayoutY(ball.getLayoutY() - velY);

        velY += accelY;
        velX += accelX;
    }

    public void reset(int x, int y) {
        ball.relocate(x, y);
        velX = 0;
        velY = 0;
    }
}