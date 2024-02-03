package com.example.demo;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.apache.logging.log4j.Level;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;

import java.util.*;


import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;


@RestController

@RequestMapping("/todo")
public class controller {

    public static Integer counter = 1;
    private TodoRep todoDataList = new TodoRep();
    public static final Logger loggerTodo = LogManager.getLogger("todo-logger");
    public static final Logger loggerReq = LogManager.getLogger("request-logger");

    private static void addRequestDuration(long start) {
        long durTime = System.currentTimeMillis() - start;
        loggerReq.debug("request #{} duration: {}ms", counter, durTime);
    }

    private static void addRequest() {
        ThreadContext.put("requestNumber", counter.toString());
        HttpServletRequest requestData = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestMethod = requestData.getMethod();
        String resourceData = requestData.getRequestURI();
        loggerReq.info("Incoming request | #{} | resource: {} | HTTP Verb {}", counter, resourceData, requestMethod);
    }

    @RestController

    @RequestMapping("/")
    public class LogsController {

        @RequestMapping(value = "/logs/level", method = RequestMethod.GET)
        public String getLoggerLevel(@RequestParam (name = "name") String loggerName) {

            if (loggerName.equals("request-logger")) {
                addRequest();

                long start = System.currentTimeMillis();
                long durData = System.currentTimeMillis() - start;
                addRequestDuration(start);

                counter++;

                return loggerReq.getLevel().name().toUpperCase();
            }
            if (!loggerName.equals("todo-logger")) {
                addRequest();
                long start = System.currentTimeMillis();
                long durTime = System.currentTimeMillis() - start;

                addRequestDuration(start);

                counter++;
                return loggerTodo.getLevel().name().toUpperCase();
            } else {
                return "Invalid logger name";
            }
        }
        @RequestMapping(value = "/logs/level", method = RequestMethod.PUT)

        public String setLoggerLevelData(@RequestParam (name = "name") String name, @RequestParam(name = "loggerLevelD") String loggerLevelD) {

            if (!loggerLevelD.equals("ERROR") && !loggerLevelD.equals("INFO") && !loggerLevelD.equals("DEBUG")) {
                return "Invalid logger level";
            }

            if (!name.equals("request-logger") && !name.equals("todo-logger")) {
                return "Invalid logger name";
            }

            LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);

            Configuration configuration = loggerContext.getConfiguration();

            org.apache.logging.log4j.core.config.LoggerConfig loggerConfig = configuration.getLoggerConfig(name);

            Level level = Level.valueOf(loggerLevelD.toUpperCase());

            loggerConfig.setLevel(level);

            loggerContext.updateLoggers();
            addRequest();

            long start = System.currentTimeMillis();
            long durTime = System.currentTimeMillis() - start;

            addRequestDuration(start);
            counter++;

            return level.name();
        }

    }
    @GetMapping("/health")
    public ResponseEntity<String> healthC() {
        addRequest();

        long start = System.currentTimeMillis();
        long time = System.currentTimeMillis() - start;

        addRequestDuration(start);

        counter++;

        return ResponseEntity.ok().body("OK");
    }

    @PostMapping("")

    @ResponseBody

    public ResponseEntity<?> createNewTodo(@RequestBody TodoData todo) {


        if (todoDataList.isTitleExists(todo.getTitleStat())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new errorMessage("Error: TODO with the title [" + todo.getTitleStat() + "] already exists in the system"));
        }
        LocalDateTime localDateTime = LocalDateTime.now();

        long timestamp = todo.getDate();

        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime newTimeByCal = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        if (newTimeByCal.isBefore(localDateTime)) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body(new errorMessage("Error: Can’t create new TODO that its due date is in the past"));
        }
        addRequest();
        long start = System.currentTimeMillis();

        todo.setStatus(TodoData.TodoStatus.PENDING);
        todoDataList.add(todo);

        addRequestDuration(start);
        loggerTodo.info("Creating new TODO with Title [{}]", todo.getTitleStat());

        loggerTodo.debug("Currently there are {} TODOs in the system. New TODO will be assigned with id {}",
                todoDataList.getAll().size()-1, todo.getId());

        counter++;

        return ResponseEntity.ok(new resultMessage(todo.getId()));
    }

    @GetMapping("/size")

    public ResponseEntity<?> sizeWithDFilter(@RequestParam (name = "status") String stat) {///written stat but change to status!!!!!!

        if(Arrays.stream(TodoData.TodoStatus.values()).noneMatch(tds -> tds.toString().equals(stat)) && !stat.equals("ALL")) {
            return ResponseEntity.badRequest().body("Invalid request");
        }


        long start = System.currentTimeMillis();
        addRequest();

        Integer integerCounter;
        if(stat.equals("ALL"))
            integerCounter = todoDataList.getAll().size();
        else
            integerCounter = Integer.valueOf((int) todoDataList.getAll().stream().filter(td -> stat.equals(td.getStatus().toString())).count());
        loggerTodo.info("Total TODOs count for state {} is {}", stat,integerCounter);
        controller.counter++;

        addRequestDuration(start);


        return ResponseEntity.ok().body(new resultMessage(integerCounter));
    }
    @GetMapping("/content")
    public ResponseEntity<?> getTodoData(@RequestParam (name = "status") String status, @RequestParam(name = "howToSort", required = false) String howToSort) {

        if(Arrays.stream(TodoData.TodoStatus.values()).noneMatch(tds -> tds.toString().equals(status)) && !status.equals("ALL"))
            return ResponseEntity.badRequest().body("Invalid request");

        if(howToSort != null)
            if(!howToSort.equals("ID") && !howToSort.equals("DUE_DATE") && !howToSort.equals("TITLE"))
                return ResponseEntity.badRequest().body("Invalid request");

        addRequest();
        long start = System.currentTimeMillis();

        List<TodoData> listDataAfterFilter;

        if(status.equals("ALL"))
            listDataAfterFilter = todoDataList.getAll();
        else {
            listDataAfterFilter = todoDataList.getAll().stream().filter(td -> status.equals(td.getStatus().toString())).toList();
        }
        if(howToSort != null) {
            if (howToSort.equals("ID")) {
                listDataAfterFilter = listDataAfterFilter.stream().sorted(Comparator.comparing(TodoData::getId)).toList();
            } else if (howToSort.equals("DUE_DATE")) {
                listDataAfterFilter = listDataAfterFilter.stream().sorted(Comparator.comparing(TodoData::getDate)).toList();
            } else {
                listDataAfterFilter = listDataAfterFilter.stream().sorted(Comparator.comparing(TodoData::getTitleStat, String.CASE_INSENSITIVE_ORDER)).toList();
            }
        }
        else {
            howToSort = "ID";
        }
        long durTime = System.currentTimeMillis() - start;

        addRequestDuration(start);
        loggerTodo.info("Extracting todos content. Filter: {} | Sorting by: {}", status, howToSort);
        loggerTodo.debug("There are a total of {} todos in the system. The result holds " + "{} todos", todoDataList.getAll().size(), listDataAfterFilter.size());

        counter++;

        return ResponseEntity.ok().body(new resultMessage(listDataAfterFilter));
    }
    @DeleteMapping ("")
    public ResponseEntity<?> deleteByID(@RequestParam (name = "id") int id) {

        if(todoDataList.getAll().stream().noneMatch(td -> td.getId() == id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new errorMessage("Error: no such TODO with id " + id));
        }

        long start = System.currentTimeMillis();
        addRequest();

        todoDataList.getAll().removeIf(td -> td.getId() == id);

        loggerTodo.info("Removing Todo id {}", id);
        addRequestDuration(start);

        loggerTodo.debug("After Removing Todo id [{}] there are {} TODO's in our system", id, todoDataList.getAll().size());
        counter++;

        return ResponseEntity.ok().body(new resultMessage(todoDataList.getAll().size()));

    }
    @PutMapping("")
    public ResponseEntity<?> updateStatus(@RequestParam (name = "id") int id, @RequestParam (name = "status") String  status) {

        if (Arrays.stream(TodoData.TodoStatus.values()).noneMatch(tds -> tds.toString().equals(status)))

            return ResponseEntity.badRequest().body("Invalid request");

        long startTime = System.currentTimeMillis();
        addRequest();
        if (todoDataList.getAll().stream().noneMatch(td -> td.getId() == id)) {
            long durTime = System.currentTimeMillis() - startTime;
            addRequestDuration(startTime);
            ThreadContext.put("requestNumber", counter.toString());
            loggerTodo.info("Update TODO Id [{}] state to {}", id, status);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new errorMessage("Error: their is no TODO with the  Id: " + id));
        }

        TodoData todoData = todoDataList.getAll().stream().filter(td -> td.getId() == id).findFirst().get();
        String name = todoData.getStatus().name();

        if (status.equals(TodoData.TodoStatus.DONE.name())) {
            todoData.setStatus(TodoData.TodoStatus.DONE);
        } else if (status.equals(TodoData.TodoStatus.PENDING.name())) {
            todoData.setStatus(TodoData.TodoStatus.PENDING);
        } else if (status.equals(TodoData.TodoStatus.LATE.name())) {
            todoData.setStatus(TodoData.TodoStatus.LATE);
        } else {

            addRequestDuration(startTime);
            loggerTodo.info("Update TODO Id [{}] state to {}", id, status);

            loggerTodo.debug("Todo Id [{}] state change: {} -> {}", id, name, status);

            counter++;

            return ResponseEntity.ok().body(new resultMessage(name));
        }
        return ResponseEntity.ok().body(new resultMessage(name));
    }
}
