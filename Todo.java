///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0
//JAVA 11+

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(name = "Todo", mixinStandardHelpOptions = true, version = "Todo 0.1",
        description = "Todo made with jbang")
class Todo implements Callable<Integer> {

    @Parameters(index = "0", description = "Text for new Todo item or comment to append on check / uncheck.", defaultValue = "")
    private String text;

    @Option(names = {"-t", "--todo", "-todo"}, description = "Create a new Todo item with given text.")
    private boolean newTodo;

    @Option(names = {"-n", "--new", "-new"}, description = "Create a new Todo file.")
    private boolean newTodoFile;

    @Option(names = {"-c", "--check", "-check"}, description = "Line number for item to check/uncheck and additional comment to append to the item line.")
    private Long itemToCheck;

    @Option(names = {"-l", "--list", "-list"}, description = "List todos of current file.")
    private boolean printCurrentTodo;

    public static void main(String... args) {
        int exitCode = new CommandLine(new Todo()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        if (prepareTodo()) {
            if (newTodoFile) {
                this.createNewTodoFile();
            }

            if (printCurrentTodo) {
                this.printCurrentTodoList();
            } else if (newTodo) {
                this.createNewTodoEntry();
            } else if (itemToCheck != null && itemToCheck > 0) {
                this.checkUncheckTodoItem();
            }

        } else {
            System.err.println("Error occurred quitting ...");
        }


        return 0;
    }

    private Path getCurrentWorkingDir() {
        String dir = System.getProperty("user.home");
        String customDir = System.getenv("TODO_DIR");

        Path path = null;

        try {
            if (customDir != null) {
                dir = customDir;
            }

            dir = dir + File.separator + "Todo";
            path = Paths.get(dir);

            if (Files.notExists(path)) {
                System.out.println("Working dir does not exists, will be created: " + path.toString());
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Failed to create directory (" + dir + ")!" + e.getMessage());
        }

        return path;
    }

    private Boolean prepareTodo() {
        boolean success = true;
        Path path = getCurrentWorkingDir();

        if (path != null) {
            try {
                if (Files.notExists(path)) {
                    System.out.println("Working dir does not exists, will be created: " + path.toString());
                    Files.createDirectories(path);
                }
            } catch (IOException e) {
                success = false;
                System.err.println("Failed to create directory (" + path.toString() + ")!" + e.getMessage());
            }
        } else {
            success = false;
            System.err.println("Working directory not set!");
        }

        return success;
    }

    private void createNewTodoFile() {
        ZonedDateTime date = ZonedDateTime.now(ZoneId.systemDefault());
        String dateStr = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'kk_mm_ss").format(date);

        Path dir = getCurrentWorkingDir();
        Path fileToCreatePath = dir.resolve(dateStr + ".md");
        try {
            Path newFilePath = Files.createFile(fileToCreatePath);
        } catch (IOException e) {
            System.err.println("Failed to create new Todo file (" + fileToCreatePath.toString() + ")!" + e.getMessage());
        }

        System.out.println("\nCreated new Todo file: " + fileToCreatePath.toString() + "\n");
    }

    private Path getLatestTodo(boolean descending) {
        Path path = null;

        try (Stream<Path> stream = Files.list(getCurrentWorkingDir())) {
            List<Path> pathList = stream
                    .filter(file -> !Files.isDirectory(file))
                    .collect(Collectors.toList());

            Comparator<Path> comparator = Comparator.comparingLong(this::getFileCreationEpoch);

            if (descending) {
                comparator = comparator.reversed();
            }

            pathList.sort(comparator);
            if (pathList.size() > 0) {
                path = pathList.get(0);
            }

        } catch (IOException ex) {
            System.err.println("Error on loading latest Todo!");
        }
        return path;
    }

    private long getFileCreationEpoch(Path filePath) {
        try {
            BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
            return attr.creationTime()
                    .toInstant()
                    .toEpochMilli();
        } catch (IOException e) {
            throw new RuntimeException(filePath.toString(), e);
        }
    }

    private void createNewTodoEntry() {
        Path path = getLatestTodo(true);

        if (path == null) {
            System.err.println("No todo list found!");
        } else {           
            String newTodoItem = "- [ ] " + this.text + System.lineSeparator();

            try {
                Files.write(
                        path,
                        newTodoItem.getBytes(),
                        StandardOpenOption.APPEND);

                this.printTodoList(path);
            } catch (IOException e) {
                System.err.println("Failed to write new item to " + path.toString() + ")!" + e.getMessage());
            }         
        }
    }

    private void printTodoList(Path path) throws IOException {
        System.out.println("\n##########################################");
        System.out.println("# " + path.getFileName());
        System.out.println("##########################################");

        Stream<String> lines = Files.lines(path);

        lines.forEach(System.out::println);

        System.out.println("\n");

        lines.close();
    }

    private void checkUncheckTodoItem() {
        Path path = null;

        try {
            path = getLatestTodo(true);
            if (path == null) {
                System.err.println("No todo list found!");
            } else {  
                Stream<String> lines = Files.lines(path);

                List<String> lineList = lines.collect(Collectors.toList());

                lines.close();

                String temp = lineList.get(this.itemToCheck.intValue() - 1);

                if (temp.startsWith("- [ ]")) {
                    temp = temp.replaceFirst("- \\[ \\]", "- [x]");
                } else if (temp.startsWith("- [x]")) {
                    temp = temp.replaceFirst("- \\[x\\]", "- [ ]");
                }

                ZonedDateTime date = ZonedDateTime.now(ZoneId.systemDefault());
                String dateStr = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'kk:mm:ss").format(date);

                if (this.text != null && !this.text.isEmpty()) {
                    temp += " | " + dateStr + " | " + this.text;
                }

                lineList.set(this.itemToCheck.intValue() - 1, temp);

                BufferedWriter writer = Files.newBufferedWriter(path);
                writer.write("");
                writer.flush();

                for (String line : lineList) {
                    String newLine = line + System.lineSeparator();
                    Files.write(
                            path,
                            newLine.getBytes(),
                            StandardOpenOption.APPEND);
                }

                this.printTodoList(path);
            }
        } catch (IOException e) {
            System.err.println("Failed to check/uncheck item [" + this.itemToCheck + "] " + path.toString() + ")!" + e.getMessage());
        }
    }

    private void printCurrentTodoList() {
        Path path = getLatestTodo(true);
        if (path == null) {
            System.err.println("No todo list found!");
        } else {  
            try {
                this.printTodoList(path);
            } catch (IOException e) {
                System.err.println("Failed to read current todo list: " + path.toString() + ")!" + e.getMessage());
            }
        }
    }
}
