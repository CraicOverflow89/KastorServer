package craicoverflow89.kastor.tools;

import java.util.ArrayList;

/**
 *
 * @author Jamie
 */
public class Collection
{

    public static String convertArrayListToString(ArrayList<String> list)
    {
        return convertArrayListToString(list, "\n");
    }

    public static String convertArrayListToString(ArrayList<String> list, String delimiter)
    {
        final StringBuilder buffer = new StringBuilder();
        for(int x = 0; x < list.size(); x ++)
        {
            if(x > 0) buffer.append(delimiter);
            buffer.append(list.get(x));
        }
        return buffer.toString();
    }

    public static String listLast(String input, String delimiter)
    {
        return input.substring(input.lastIndexOf(delimiter) + 1, input.length());
        // NOTE: might be a bit dodgey if the last character is the delimeter
        //       come back to this
    }

}