package com.example.projects;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        System.out.println(greeting().message());
    }

    static Greeting greeting() {
        return new Greeting("Hello from the Java 21 project scaffold.");
    }
}
