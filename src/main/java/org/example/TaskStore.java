package org.example;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles reading and writing the task list to a JSON file on disk.
 * Hand-rolled JSON (no external libraries), per the project constraints.
 *
 * NOTE: this is written against Task.java as currently defined (LocalTime,
 * package-private full constructor). If you switch Task to LocalDateTime
 * later (recommended), swap the formatter/parsing calls below accordingly.
 */
public class TaskStore {

    private final Path filePath;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ISO_LOCAL_TIME;

    public TaskStore(String fileName) {
        this.filePath = Path.of(fileName);
    }

    public TaskStore() {
        this("tasks.json");
    }

    /** Loads all tasks from the JSON file. Creates an empty file if none exists. */
    public List<Task> loadTasks() {
        try {
            if (!Files.exists(filePath)) {
                Files.writeString(filePath, "[]", StandardCharsets.UTF_8);
                return new ArrayList<>();
            }
            String content = Files.readString(filePath, StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return new ArrayList<>();
            }
            return parseTasks(content);
        } catch (IOException e) {
            System.err.println("Error reading " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        } catch (RuntimeException e) {
            System.err.println("Error parsing " + filePath + " (corrupt JSON?): " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Writes the full task list back to the JSON file, overwriting its contents. */
    public void saveTasks(List<Task> tasks) {
        try {
            Files.writeString(filePath, toJson(tasks), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error writing " + filePath + ": " + e.getMessage());
        }
    }

    /** Returns the next available id (max existing id + 1, or 1 if empty). */
    public int nextId(List<Task> tasks) {
        int max = 0;
        for (Task t : tasks) {
            if (t.getTask_id() > max) {
                max = t.getTask_id();
            }
        }
        return max + 1;
    }

    // ---------- serialization ----------

    private String toJson(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            sb.append("  {\n");
            sb.append("    \"id\": ").append(t.getTask_id()).append(",\n");
            sb.append("    \"description\": \"").append(escape(t.getDescription())).append("\",\n");
            sb.append("    \"status\": \"").append(escape(t.getStatus())).append("\",\n");
            sb.append("    \"createdAt\": \"").append(t.getCreatedat().format(TIME_FMT)).append("\",\n");
            sb.append("    \"updatedAt\": \"").append(t.getUpdatedat().format(TIME_FMT)).append("\"\n");
            sb.append("  }");
            if (i < tasks.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    // ---------- parsing ----------
    // Minimal parser tailored to the flat object shape we write above.
    // Not a general-purpose JSON parser (no nested objects/arrays needed here).

    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{(.*?)}", Pattern.DOTALL);
    private static final Pattern FIELD_PATTERN =
            Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"(?:[^\"\\\\]|\\\\.)*\"|-?\\d+)");

    private List<Task> parseTasks(String json) {
        List<Task> tasks = new ArrayList<>();
        Matcher objectMatcher = OBJECT_PATTERN.matcher(json);

        while (objectMatcher.find()) {
            String body = objectMatcher.group(1);
            Matcher fieldMatcher = FIELD_PATTERN.matcher(body);

            int id = 0;
            String description = "";
            String status = "todo";
            LocalTime createdAt = LocalTime.now();
            LocalTime updatedAt = LocalTime.now();

            while (fieldMatcher.find()) {
                String key = fieldMatcher.group(1);
                String rawValue = fieldMatcher.group(2);

                switch (key) {
                    case "id" -> id = Integer.parseInt(rawValue);
                    case "description" -> description = unescape(stripQuotes(rawValue));
                    case "status" -> status = unescape(stripQuotes(rawValue));
                    case "createdAt" -> createdAt = LocalTime.parse(stripQuotes(rawValue));
                    case "updatedAt" -> updatedAt = LocalTime.parse(stripQuotes(rawValue));
                    default -> { /* ignore unknown fields */ }
                }
            }

            tasks.add(new Task(id, description, status, createdAt, updatedAt));
        }
        return tasks;
    }

    // ---------- string escaping helpers ----------

    private String stripQuotes(String s) {
        return s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s;
    }

    private String escape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    private String unescape(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"' -> { out.append('"'); i++; }
                    case '\\' -> { out.append('\\'); i++; }
                    case 'n' -> { out.append('\n'); i++; }
                    case 'r' -> { out.append('\r'); i++; }
                    case 't' -> { out.append('\t'); i++; }
                    default -> out.append(c);
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}