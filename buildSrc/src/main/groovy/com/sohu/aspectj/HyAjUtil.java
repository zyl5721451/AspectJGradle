package com.sohu.aspectj;

import org.apache.commons.io.FileUtils;
import org.gradle.util.TextUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;

import javax.swing.plaf.TextUI;

/**
 * Created by allenzhang on 2019/4/16.
 */
public class HyAjUtil {
    static boolean isClassFile(File file) {
        if(file == null){
            return false;
        }
        String filePath = file.getAbsolutePath();
        if(filePath.toLowerCase().endsWith(".class")){
            return true;
        }
        return false;
    }

    static boolean isClassFile(String file) {
        if(file == null||"".equals(file)){
            return false;
        }
        if(file.toLowerCase().endsWith(".class")){
            return true;
        }
        return false;
    }
    static boolean isAspectClass(File classFile) {
        if(isClassFile(classFile)) {
            try {
                return isAspectClass(FileUtils.readFileToByteArray(classFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    static boolean isAspectClass(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        try {
            ClassReader classReader = new ClassReader(bytes);
            ClassWriter classWriter = new ClassWriter(classReader,ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
            AspectJClassVisitor aspectJClassVisitor = new AspectJClassVisitor(classWriter);
            classReader.accept(aspectJClassVisitor,ClassReader.EXPAND_FRAMES);
            return aspectJClassVisitor.isIsAspectJClass();

        }catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    static int countOfFiles(File file) {
        if (file.isFile()) {
            return 1;
        } else {
            File[] files = file.listFiles();
            int total = 0;
            for (File f : files) {
                total += countOfFiles(f);
            }

            return total;
        }
    }
}
