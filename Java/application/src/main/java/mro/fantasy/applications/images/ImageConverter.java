package mro.fantasy.applications.images;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class ImageConverter {

    public static void main(String[] args) throws IOException {

        String content = Files.readString(new File("C:\\Users\\Michael\\Downloads\\Image.txt").toPath());
        System.out.println(content.replace("\\\\n", "").replace(" ", ""));

        String[] hexValues = content.replaceAll("[^a-zA-Z0-9,]+","").split(",");
        int decode = Integer.decode(hexValues[0]);

        System.out.println(decode);


        FileOutputStream out = new FileOutputStream("C:\\Users\\Michael\\Downloads\\skills.dat");
        for (int i = 0; i < hexValues.length; i++) {
            System.out.println(i);
            out.write(Integer.decode(hexValues[i]).byteValue());
        }
        out.close();
    }


}
