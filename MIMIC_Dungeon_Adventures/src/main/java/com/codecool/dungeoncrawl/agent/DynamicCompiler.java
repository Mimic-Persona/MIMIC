package com.codecool.dungeoncrawl.agent;

import javax.tools.*;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;

public class DynamicCompiler {

    public static final String BOT_ERR = "agent.DynamicCompiler: ";

    // Class to represent source code as a file object
    public static class JavaSourceFromString extends SimpleJavaFileObject {
        private final String code;

        public JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    // Method to compile the source code
    public boolean compile(String className, String code, Path outputDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        JavaFileObject file = new JavaSourceFromString(className, code);
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

        try {
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(outputDir.toFile()));
        } catch (IOException e) {
            GLog.e(BOT_ERR + "Error in setting location: " + e.getMessage());
        }

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
        boolean success = task.call();

        if (!success) {
            // Output compilation errors
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            for (var diagnostic : diagnostics.getDiagnostics()) {
                pw.println(diagnostic);
            }
            System.out.println("Compilation failed:\n" + sw.toString());
        }

        try {
            fileManager.close();
        } catch (Exception e) {
            GLog.e(BOT_ERR + "Error in closing file manager: " + e.getMessage());
        }

        return success;
    }

    // Custom class loader to load compiled classes
    public static class CustomClassLoader extends ClassLoader {
        private final Path classesDir;

        public CustomClassLoader(Path classesDir) {
            this.classesDir = classesDir;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                Path classPath = classesDir.resolve(name.replace('.', '/') + ".class");
                byte[] classBytes = Files.readAllBytes(classPath);
                return defineClass(name, classBytes, 0, classBytes.length);
            } catch (IOException e) {
                GLog.e(BOT_ERR + "Could not load class: " + e.getMessage());
                throw new ClassNotFoundException("Could not load class " + name, e);
            }
        }
    }

    // Method to load the compiled class using CustomClassLoader
    public Class<?> loadClass(String className, Path outputDir) throws ClassNotFoundException {
        CustomClassLoader classLoader = new CustomClassLoader(outputDir);
        return classLoader.loadClass(className);
    }

    // Method to run the main method of the compiled class with a timeout
    public boolean runMainWithTimeout(Class<?> clazz, long timeout, TimeUnit timeUnit) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                Method main = clazz.getMethod("main", String[].class);
                String[] params = new String[]{}; // Empty params array
                main.invoke(null, (Object) params);
            } catch (Exception e) {
                GLog.e(BOT_ERR + "Error in running main method: " + e.getMessage());
            }
        });

        try {
            future.get(timeout, timeUnit);

        } catch (TimeoutException e) {
            GLog.e(BOT_ERR + "TIMEOUT: " + e.getMessage());
            return true;

        } catch (Exception e) {
            GLog.e(BOT_ERR + "Error in future.get: " + e.getMessage());

        } finally {
            executor.shutdownNow();
        }

        return false;
    }

    // Helper method to compile and run Java code dynamically with timeout
    public boolean compileAndRun(String className, String code, Path outputDir, long timeout, TimeUnit timeUnit) {
        if (compile(className, code, outputDir)) {
            try {
                Class<?> clazz = loadClass(className, outputDir);
                return runMainWithTimeout(clazz, timeout, timeUnit);
            } catch (Exception e) {
                GLog.e(BOT_ERR + "Error in compileAndRun: " + e.getMessage());
            }
        }
        return false;
    }

    public static void main(String[] args) {
        String className = "DynamicClass";
        String code = "public class " + className + " {" +
                "    public static void main(String[] args) {" +
                "        System.out.println(\"Hello, dynamic world!\");" +
                "    }" +
                "}";

        Path outputDir = Paths.get("./src/main/java/com/codecool/dungeoncrawl/agent/skill_library/compiled_skills/");
        try {
            Files.createDirectories(outputDir); // Create directories if they don't exist
        } catch (IOException e) {
            GLog.e(BOT_ERR + "Error in creating directories: " + e.getMessage());
            return;
        }

        DynamicCompiler compiler = new DynamicCompiler();
        boolean isTimeOut = compiler.compileAndRun(className, code, outputDir, (long) (0.5 * 60000), TimeUnit.MILLISECONDS); // Set a timeout of 30 seconds
    }
}