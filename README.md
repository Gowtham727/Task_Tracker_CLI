# Task Tracker CLI

A simple command-line tool to track tasks — what you need to do, what you're
working on, and what you've finished. Tasks are stored in a local `tasks.json`
file. No external libraries or frameworks are used; only the Java standard
library.

## Project Structure

```
Task Tracker CLI/
└── org/
    └── example/
        ├── Task.java        # Task model (id, description, status, timestamps)
        ├── TaskStore.java   # Reads/writes tasks.json (hand-rolled JSON, no libs)
        └── Main.java        # CLI entry point — parses commands and args
```

## Requirements

- Java Development Kit (JDK) 17 or later (uses `switch` expressions and
  text blocks)
- No external dependencies

## Build

From the project root (the folder containing `org/`):

```powershell
javac org/example/*.java -d out
```

This compiles all three classes into an `out` folder.

## Run

```powershell
java -cp out org.example.Main <command> [arguments]
```

For example:

```powershell
java -cp out org.example.Main add "Buy groceries"
```

### Optional: a `task-cli` shortcut

To type `task-cli` instead of the full `java -cp out ...` command, create a
file named `task-cli.bat` in your project folder with:

```bat
@echo off
java -cp "C:\path\to\Task Tracker CLI\out" org.example.Main %*
```

Then run it from that folder as `task-cli add "Buy groceries"`.

## Usage

```
task-cli add "<description>"
task-cli update <id> "<new description>"
task-cli delete <id>
task-cli mark-in-progress <id>
task-cli mark-done <id>
task-cli list
task-cli list done
task-cli list todo
task-cli list in-progress
```

### Examples

```powershell
# Add a new task
task-cli add "Buy groceries"
# Output: Task added successfully (ID: 1)

# Update a task's description
task-cli update 1 "Buy groceries and cook dinner"

# Delete a task
task-cli delete 1

# Mark a task's status
task-cli mark-in-progress 1
task-cli mark-done 1

# List tasks
task-cli list                # all tasks
task-cli list done           # only completed tasks
task-cli list todo           # only not-yet-started tasks
task-cli list in-progress    # only in-progress tasks
```

## Task Properties

Each task stored in `tasks.json` has:

| Field         | Description                                   |
|---------------|------------------------------------------------|
| `id`          | Unique integer identifier                      |
| `description` | Short text describing the task                 |
| `status`      | One of `todo`, `in-progress`, `done`           |
| `createdAt`   | Timestamp set when the task was created        |
| `updatedAt`   | Timestamp updated whenever the task changes    |

## Data Storage

- Tasks are stored in `tasks.json` in the directory you run the command from.
- The file is created automatically (as `[]`) the first time you run any
  command if it doesn't already exist.
- JSON reading/writing is implemented by hand in `TaskStore.java` — no
  external JSON library is used, per the project constraints.

## Known Limitations

- IDs are not reused after deletion (next ID is always `max existing ID + 1`).
- `TaskStore`'s JSON parser is intentionally minimal — it understands only
  the flat task-object shape this project writes, not arbitrary JSON.
- Timestamps currently use `LocalTime` (time of day only, no date). This is
  a known limitation inherited from `Task.java` and would need to change to
  `LocalDateTime` to fully match a real "created at / updated at" timestamp.
