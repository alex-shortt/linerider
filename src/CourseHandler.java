import javafx.scene.Group;
import javafx.scene.shape.Line;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

public class CourseHandler {
    Group root;

    public CourseHandler(Group newRoot){
        root = newRoot;
    }

    public void saveCourse(ArrayList<CollisionBody> lines) throws IOException {
        String name = JOptionPane.showInputDialog("Name Your World");
        if(name == null || (name != null && ("".equals(name))))
        {
            return;
        }
        PrintWriter writer = new PrintWriter("courses/" + name + ".txt", "UTF-8");

        for (CollisionBody line : lines){
            Line body = ((Line) line.getShape());
            writer.println(body.getStartX() + "," + body.getStartY() + "," + body.getEndX() + "," + body.getEndY());
        }

        writer.close();
    }

    public ArrayList<CollisionBody> loadCourse() throws FileNotFoundException {
        String name = JOptionPane.showInputDialog("World Name");

        ArrayList<String> records = new ArrayList<String>();
        try
        {
            File toRead = new File("courses/" + name + ".txt");
            if(!toRead.exists()){
               return null;
            }
            BufferedReader reader = new BufferedReader(new FileReader(toRead));
            String line;
            while ((line = reader.readLine()) != null)
            {
                records.add(line);
            }
            reader.close();

            ArrayList<CollisionBody> bodies = new ArrayList<>();

            for (String string : records){
                Line body = new Line(Double.parseDouble(string.split(",")[0]), Double.parseDouble(string.split(",")[1]), Double.parseDouble(string.split(",")[2]), Double.parseDouble(string.split(",")[3]));
                bodies.add(new CollisionBody(root, body));
            }

            return bodies;
        }
        catch (Exception e)
        {
            System.err.format("Could not find World " + name);
            e.printStackTrace();
            return null;
        }
    }
}