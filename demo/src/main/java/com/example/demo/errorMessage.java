package com.example.demo;

import org.apache.logging.log4j.ThreadContext;

import static com.example.demo.controller.loggerTodo;

public class errorMessage {
        private String errorMessage;

        public errorMessage(String message) {

            ThreadContext.put("requestNumber", controller.counter.toString());
            loggerTodo.error(message);

            this.errorMessage = message;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }


