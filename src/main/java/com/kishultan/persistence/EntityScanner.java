package com.kishultan.persistence;

import jakarta.persistence.Entity;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class EntityScanner {
    public static List<Class<?>> scan(String basePackage) {
        List<Class<?>> entities = new ArrayList<>();
        String packagePath = basePackage.replace('.', '/');
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddEntitiesInDirectory(basePackage, filePath, entities);
                } else if ("jar".equals(protocol)) {
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    findAndAddEntitiesInJar(jar, packagePath, basePackage, entities);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan package: " + basePackage, e);
        }
        return entities;
    }

    private static void findAndAddEntitiesInDirectory(String packageName, String dirPath, List<Class<?>> classes) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) return;
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                findAndAddEntitiesInDirectory(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                tryAddIfEntity(className, classes);
            }
        }
    }

    private static void findAndAddEntitiesInJar(JarFile jar, String packagePath, String basePackage, List<Class<?>> classes) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(packagePath) && name.endsWith(".class") && !entry.isDirectory()) {
                String className = name.replace('/', '.').substring(0, name.length() - 6);
                tryAddIfEntity(className, classes);
            }
        }
    }

    private static void tryAddIfEntity(String className, List<Class<?>> classes) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Entity.class)) {
                classes.add(clazz);
            }
        } catch (Throwable ignored) {
        }
    }
}
