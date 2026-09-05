package org.example;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {



        if(args.length == 0){

                printusage();
                System.exit(1);


        }
        TaskStore store = new TaskStore();          // reads/writes tasks.json
        List<Task> tasks = store.loadTasks();        // load current tasks

        String command = args[0];                    // e.g. "add"

        switch (command) {
            case "add" ->
                handleadd(args, store, tasks);

            case "update" -> updatetask(args,store,tasks);
            case "delete" -> handleDelete(args,store,tasks);
            case "mark-in-progress" -> handleMark(args, store, tasks, "in-progress");
            case "mark-done" -> handleMark(args, store, tasks, "done");
            case "list" -> handleList(args, tasks);
            default -> {
                System.err.println("Unknown command: " + command);
                printusage();
                System.exit(1);
            }

        }
    }

    private static void handleList(String[] args, List<Task> tasks) {
        String filter = args.length >= 2 ? args[1] : null;

        if (filter != null && !filter.equals("done") && !filter.equals("todo") && !filter.equals("in-progress")) {
            System.err.println("Unknown status filter: " + filter);
            System.err.println("Usage: task-cli list [done|todo|in-progress]");
            System.exit(1);
            return;
        }

        List<Task> filtered = tasks.stream()
                .filter(t -> filter == null || t.getStatus().equals(filter))
                .toList();

        if (filtered.isEmpty()) {
            System.out.println("No tasks found" + (filter != null ? " with status: " + filter : ""));
            return;
        }

        for (Task t : filtered) {
            System.out.printf(
                    "[%d] %-30s status: %-12s created: %s  updated: %s%n",
                    t.getTask_id(),
                    t.getDescription(),
                    t.getStatus(),
                    t.getCreatedat(),
                    t.getUpdatedat()
            );
        }
    }

    private static void handleMark(String[] args, TaskStore store, List<Task> tasks, String newStatus) {
        if (args.length < 2) {
            System.err.println("Usage: task-cli " + args[0] + " <id>");
            System.exit(1);
            return;
        }
        int id = findtaskid(args[1]);
        Task task = findtasks(tasks, id);
        if (task == null) {
            System.err.println("No task found with ID: " + id);
            System.exit(1);
            return;
        }
        task.setStatus(newStatus);
        task.setUpdatedat(LocalTime.now());
        store.saveTasks(tasks);
        System.out.println("Task " + id + " marked as " + newStatus);
    }




    private static void updatetask(String[] args, TaskStore store, List<Task> tasks) {
        if (args.length < 3) {
            System.err.println("Usage: task-cli update <id> \"<new description>\"");
            System.exit(1);
            return;
        }
        int id=findtaskid(args[1]);
       Task task= findtasks(tasks,id);

       task.setDescription(args[1]);
       task.setUpdatedat(LocalTime.now());
       store.saveTasks(tasks);
        System.out.println("task "+id + " is updated successfully....");
    }

    private static Task findtasks(List<Task> tasks, int id) {
        Optional<Task> match= tasks.stream()
                  .filter(t -> t.getTask_id() == id)
                  .findFirst();
        return match.orElse(null);
    }

    private static int findtaskid(String arg) {
        try{
           return Integer.parseInt(arg);
        }
        catch (NumberFormatException e){
            System.out.println("enter tasks id "+arg +" is invalid ");
            System.exit(1);
            return -1;
        }
    }
    public static void handleDelete(String[] args, TaskStore store, List<Task> tasks) {
        if (args.length < 2) {
            System.err.println("Usage: task-cli delete <id>");
            System.exit(1);
            return;
        }
        int id = findtaskid(args[1]);
        Task task = findtasks(tasks, id);
        if (task == null) {
            System.err.println("No task found with ID: " + id);
            System.exit(1);
            return;
        }
        tasks.remove(task);
        store.saveTasks(tasks);
        System.out.println("Task " + id + " deleted successfully");
    }


    public static void handleadd(String[] args, TaskStore taskStore, List<Task> tasks){
        if(args.length < 2 || args[1].isBlank()){
            System.err.println("Usage: task-cli add \"<description>\"");
            System.exit(1);
            return;
        }
       String description=args[1];
       int id= taskStore.nextId(tasks);
       tasks.add(new Task(id,description));
       taskStore.saveTasks(tasks);
            System.out.println("task added successsfully with id : "+id);


    }
    public static void printusage() {
        System.out.println("""
                Usage:
                  task-cli add "<description>"
                  task-cli update <id> "<new description>"
                  task-cli delete <id>
                  task-cli mark-in-progress <id>
                  task-cli mark-done <id>
                  task-cli list
                  task-cli list done
                  task-cli list todo
                  task-cli list in-progress
                """);
    }
}