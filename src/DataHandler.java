import java.io.File;
import java.io.FileInputStream;

public class DataHandler {

    public static String data(byte[] byteArray) {
        if (byteArray == null)
            return null;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if (byteArray[i] != 0)
                stringBuilder.append((char) byteArray[i]);
        }
        return stringBuilder.toString();
    }

    public static byte[] readFile(String path) {
        File file = new File(path);
        FileInputStream fileInputStream = null;
        byte[] byteFile = new byte[(int) file.length()];
        try {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(byteFile);
            fileInputStream.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return byteFile;
    }
}
