package com.whz.javabase.atomic;

public class User {
        private String name;
        public volatile int old;

        public User(String name, int old) {
            this.name = name;
            this.old = old;
        }

        public String getName() {
            return name;
        }
        public int getOld() {
            return old;
        }
    }