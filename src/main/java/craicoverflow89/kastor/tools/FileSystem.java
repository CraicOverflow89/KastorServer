package craicoverflow89.kastor.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author Jamie
 */
public class FileSystem
{

    public static ArrayList<String> loadFile(String path)
    {
        final ArrayList<String> data = new ArrayList();
        final BufferedReader reader;
        String line;
        try
        {
            reader = new BufferedReader(new FileReader(path));
            try
            {
                while(true)
                {
                    line = reader.readLine();
                    if(line != null) data.add(line);
                    else break;
                }
                reader.close();
            }
            catch(IOException ex) {System.err.println(ex);}
        }
        catch(FileNotFoundException ex) {System.err.println(ex);}
        return data;
        // NOTE: we should use a File Stream
    }

    public static ArrayList<File> loadFolder(String path, boolean getFiles, boolean getDirectories)
    {
        // Define Result
        final ArrayList<File> result = new ArrayList();

        // Iterate Contents
        final File[] folderList = new File(path).listFiles();
        for(File folder : folderList)
        {
            if(getFiles && folder.isFile()) result.add(folder);
            if(getDirectories && folder.isDirectory()) result.add(folder);
        }

        // Return Result
        return result;
    }

    public static void saveFile(String path, String data)
    {
        // Process
        try
        {
            // File Writer
            final PrintWriter writer = new PrintWriter(new FileWriter(path, false));

            // Write Data
            writer.printf("%s" + "%n", data);

            // Close File
            writer.close();
        }

        // Error Handling
        catch(IOException ex) {System.err.println(ex);}
        // NOTE: is this what we want to do?
    }

}