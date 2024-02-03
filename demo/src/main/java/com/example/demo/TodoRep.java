package com.example.demo;

import java.util.ArrayList;
import java.util.List;



public class TodoRep {

    static int Id = 1;
    private List<TodoData> todosArr;

    public TodoRep() {
        this.todosArr = new ArrayList<>();
    }

    public void add(TodoData todo) {
        todo.setId(Id++);
        todosArr.add(todo);
    }
    public List<TodoData> getAll() {
        return todosArr;
    }
    public boolean isTitleExists(String title) {
        for (TodoData todo : todosArr) {
            if (todo.getTitleStat().equals(title)) {
                return true;
            }
        }
        return false;
    }
}
