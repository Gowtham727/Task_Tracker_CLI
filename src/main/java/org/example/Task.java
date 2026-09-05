package org.example;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.time.LocalTime.now;

public class Task {
    private int task_id;
    private String description;
    private String status;
    private LocalTime createdat;
    private LocalTime updatedat;

    public Task(int id,String description) {
        this(id, description,"todo", LocalTime.now(),LocalTime.now());
    }

    Task(int task_id,String description,String taskStatus,LocalTime createdat,LocalTime updatedat){
        this.task_id=task_id;
        this.description=description;
        this.status=taskStatus;
        this.createdat=createdat;
        this.updatedat=updatedat;;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public void setCreatedat(LocalTime createdat) {
        this.createdat = now();
    }

    public LocalTime getCreatedat() {
        return createdat;
    }

    public void setUpdatedat(LocalTime updatedat) {
        this.updatedat = now();
    }

    public LocalTime getUpdatedat() {
        return updatedat;
    }


    Map<String,Object> tomap(){
        Map<String,Object> taskmap=new LinkedHashMap<>();
        taskmap.put("id",this.getTask_id());
        taskmap.put("description",this.getDescription());
        taskmap.put("status",this.getStatus());
        taskmap.put("created_At",this.getCreatedat());
        taskmap.put("updated At",this.getUpdatedat());
        return taskmap;
    }
}
