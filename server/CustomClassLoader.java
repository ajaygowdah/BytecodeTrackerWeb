package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class CustomClassLoader extends ClassLoader {

    public Class<?> loadClassFromFile(String filePath) throws Exception {

        File file = new File(filePath);

        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        byte[] classData = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);
        try {
            fis.read(classData);
        } finally {
            fis.close();
        }

        return defineClass(null, classData, 0, classData.length);
    }
}