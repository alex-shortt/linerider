import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

class ToolBarItem {
    Image image;
    double x;
    double y;
    double height;
    double width;
    Canvas canvas;
    boolean isHilighted = false;
    ItemAction action;

    public ToolBarItem(Canvas newCanvas, String url, double posX, double posY, double setHeight, ItemAction itemAction) {
        image = new Image(url);
        x = posX;
        y = posY;
        height = setHeight;
        width = image.getWidth() * (height / image.getHeight());
        canvas = newCanvas;
        action = itemAction;

        //canvas.getGraphicsContext2D().drawImage(image, x, y, width, height);
    }

    public void hide() {
        canvas.getGraphicsContext2D().clearRect(x, y, width, height);
    }

    public void show() {
        hide();
        canvas.getGraphicsContext2D().drawImage(image, x, y, width, height);
    }

    public ItemAction getActionType() {
        return action;
    }

    public void toggleImage(String newUrl) {
        //create new image, hide current, set image, show again
        //used for play/pause
    }

    public Bounds getBounds() {
        return new BoundingBox(x, y, width, height);
    }

    public double getEndX() {
        return x + width;
    }

    public boolean isHilighted() {
        return isHilighted;
    }

    public void setHilighted(boolean visible) {
        isHilighted = visible;
        if (visible) {
            int topMargin = 10;
            //canvas.getGraphicsContext2D().strokeLine(x, y + height + topMargin, x + width, y + height + topMargin);
        } else {
            int topMargin = 10;
            int padding = 3;
            //canvas.getGraphicsContext2D().clearRect(x - padding, y + height + topMargin - padding, x + width + padding, y + height + topMargin + padding);
        }
    }
}