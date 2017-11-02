import javafx.scene.Group;
import javafx.scene.shape.Line;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Class to handle saving/loading courses
 */
public class CourseHandler {
    Group root;

    public CourseHandler(Group newRoot) {
        root = newRoot;
    }

    /**
     * Save the current set of lines as a course
     * @param lines
     * @throws IOException
     */
    public void saveCourse(ArrayList<CollisionBody> lines) throws IOException {
        String name = JOptionPane.showInputDialog("Name Your World");
        if (name == null || (name != null && ("".equals(name)))) {
            return;
        }
        PrintWriter writer = new PrintWriter("courses/" + name + ".txt", "UTF-8");

        for (CollisionBody line : lines) {
            Line body = ((Line) line.getShape());
            writer.println(body.getStartX() + "," + body.getStartY() + "," + body.getEndX() + "," + body.getEndY());
        }

        writer.close();
    }

    /**
     * Load course from a selected text file
     * @return List of Lines from course
     * @throws FileNotFoundException
     */
    public ArrayList<CollisionBody> loadCourse() throws FileNotFoundException {
        File folder = new File("courses/");
        File[] listOfFiles = folder.listFiles();
        String[] names = new String[listOfFiles.length];

        int strIND = 0;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                names[strIND] = listOfFiles[i].getName().replace(".txt", "");
                strIND++;
            }
        }

        String name = JOptionPane.showInputDialog(new JFrame(),
                "Load a Course",
                "Pick a Course",
                JOptionPane.QUESTION_MESSAGE,
                null,
                names,
                names[0]).toString();

        if (name == null) {
            return null;
        }

        ArrayList<String> records = new ArrayList<String>();
        try {
            File toRead = new File("courses/" + name + ".txt");
            if (!toRead.exists()) {
                return null;
            }
            BufferedReader reader = new BufferedReader(new FileReader(toRead));
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
            reader.close();

            ArrayList<CollisionBody> bodies = new ArrayList<>();

            for (String string : records) {
                Line body = new Line(Double.parseDouble(string.split(",")[0]), Double.parseDouble(string.split(",")[1]), Double.parseDouble(string.split(",")[2]), Double.parseDouble(string.split(",")[3]));
                bodies.add(new CollisionBody(root, body));
            }

            return bodies;
        } catch (Exception e) {
            System.err.format("Could not find World " + name);
            e.printStackTrace();
            return null;
        }
    }
}
