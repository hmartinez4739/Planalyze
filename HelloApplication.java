package com.example.demo1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.*;
import java.util.HashMap;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane pane = new StackPane();
        Button okButton = new Button("Let's get started!");
        pane.getChildren().add(okButton);
        Scene firstScene = new Scene(pane, 200, 50);
        primaryStage.setTitle("Planalyze");
        primaryStage.setScene(firstScene);
        primaryStage.show();

        okButton.setOnAction(e -> showNextStage(primaryStage));
    }

    private void showNextStage(Stage stage) {
        stage.setTitle("Planalyze");
        Scene scene = new Scene(new Group(), 450, 250);

        final ComboBox<String> emailComboBox = new ComboBox<>();
        emailComboBox.getItems().addAll(
                "210",
                "240",
                "160",
                "150"
        );

        final ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll(
                "CS",
                "BIO",
                "MATH",
                "ENG"
        );

        priorityComboBox.setValue("CS");

        GridPane grid = new GridPane();
        grid.setVgap(4);
        grid.setHgap(10);
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.add(new Label("Course: "), 0, 0);
        grid.add(emailComboBox, 1, 0);
        grid.add(new Label("Department: "), 2, 0);
        grid.add(priorityComboBox, 3, 0);

        TextField subject = new TextField("");
        TextArea text = new TextArea("");
        Button sendButton = new Button("Go");

        grid.add(new Label("Word: "), 0, 1);
        grid.add(subject, 1, 1, 3, 1);
        grid.add(sendButton, 3, 4);

        Group root = (Group) scene.getRoot();
        root.getChildren().add(grid);

        stage.setScene(scene);
        stage.show();

        sendButton.setOnAction(e -> {
            String word = subject.getText().trim().toLowerCase();
            lingStage(stage, word);
        });
    }

    private void lingStage(Stage stage, String word) {
        analysisProgram.concordanceList.head = null;
        analysisProgram.initialize();
        stage.setTitle("Planalyze");
        Scene scene = new Scene(new Group(), 600, 400);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        Label frequencyLabel = new Label("Frequency: ");
        Label sentenceLabel = new Label("Sentences that contain the word(s): ");
        Label frequencyResult = new Label();
        TextArea sentenceResult = new TextArea();

        grid.add(frequencyLabel, 0, 0);
        grid.add(frequencyResult, 1, 0);
        grid.add(sentenceLabel, 0, 1);
        grid.add(sentenceResult, 0, 2, 2, 1);

        Integer frequency = wordFrequency.frequencyMap.get(word);

        if (frequency != null) {
            frequencyResult.setText(String.valueOf(frequency));
        } else {
            frequencyResult.setText("Word not found.");
            sentenceResult.setText("");
        }

        try {
            new concordance(word);

            Node current = analysisProgram.concordanceList.head;
            if (current == null) {
                sentenceResult.setText("No sentences found containing the word: " + word);
            } else {
                StringBuilder concordanceText = new StringBuilder();
                while (current != null) {
                    concordanceText.append(current.data).append("\n");
                    current = current.next;
                }
                sentenceResult.setText(concordanceText.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button button = new Button("Input another word");
        grid.add(button, 5, 5);

        Group root = (Group) scene.getRoot();
        root.getChildren().add(grid);
        stage.setScene(scene);
        stage.show();

        button.setOnAction(e -> showNextStage(stage));
    }

    public void scanDocStage(Stage stage) {}

    public static void main(String[] args) {
        launch(args);
    }
}

class analysisProgram {
    public static MyList tokenList = new MyList();
    public static Trie wordTrie = new Trie();
    public static MyList concordanceList = new MyList();

    public static void initialize() {
        String database = "database";
        String tokenData = "tokenData";

        try {
            tokenizer.tokenize(database, tokenData, tokenList);
            wordFrequency.setFrequency();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void searchForToken(String target) {
        if (wordTrie.search(target) && !wordFrequency.frequencyMap.containsKey(target)) {
            wordFrequency.frequencyMap.put(target, 1);
        }
        wordFrequency.updateFrequency(target);
    }
}

class tokenizer {
    public static void tokenize(String database, String tokenData, MyList tokenList) throws IOException {
        String dataLine;
        BufferedReader reader = new BufferedReader(new FileReader(database));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tokenData));

        String tokenString = "";
        while ((dataLine = reader.readLine()) != null) {
            token(dataLine, tokenList);
        }

        tokenList.fileDisplay(writer);

        tokenList.tokenizeTrie();

        writer.close();
        reader.close();
    }

    public static String token(String dataLine, MyList tokenList) {
        String[] tokenString = dataLine.replace(",", "").split(" ");
        String token = " ";

        for (int i = 0; i < tokenString.length; i++) {
            token = tokenString[i].toLowerCase();
            tokenList.myAppend(token);
        }
        return token;
    }
}

class wordFrequency extends tokenizer {
    int frequency;
    public static HashMap<String, Integer> frequencyMap = new HashMap<>();

    public wordFrequency(int number) {
        this.frequency = number;
    }

    public static void printFrequency(String target) {
        if (frequencyMap.containsKey(target)) {
            System.out.println("Frequency of '" + target + "': " + frequencyMap.get(target));
        } else {
            System.out.println("The word '" + target + "' was not found.");
        }
    }

    public static void updateFrequency(String target) {
        int length = analysisProgram.tokenList.getLength();

        for (int i = 1; i <= length; i++) {
            Node currentNode = analysisProgram.tokenList.getNNode(i);
            String token = currentNode.data;

            if (analysisProgram.wordTrie.startsWith(target)) {
                wordFrequency.frequencyMap.put(token, wordFrequency.frequencyMap.getOrDefault(token, 0) + 1);
            }
        }

        printFrequency(target);
    }

    public static void setFrequency() {
        int length = analysisProgram.tokenList.getLength();

        for (int i = 1; i <= length; i++) {
            Node currentNode = analysisProgram.tokenList.getNNode(i);
            String token = currentNode.data;

            if (currentNode == null) {
                continue;
            }

            if (frequencyMap.containsKey(token)) {
                frequencyMap.put(token, frequencyMap.get(token) + 1);
            } else {
                frequencyMap.put(token, 1);
            }
        }
    }
}

class concordance {
    public concordance(String target) throws IOException {
        String database = "database";
        String concordanceLine;
        BufferedReader reader = new BufferedReader(new FileReader(database));

        while ((concordanceLine = reader.readLine()) != null) {
            String line = concLine(concordanceLine, target);
            if (line != null) {
                analysisProgram.concordanceList.myAppend(line);
                analysisProgram.concordanceList.myAppend("");
            }
        }
        reader.close();
    }

    public String concLine(String string, String target) {
        if (string.contains(target)) {
            return string;
        }
        return null;
    }
}

class wordGraph {}

class Node {
    String data;
    Node next;

    public Node(String _data) {
        this.data = _data;
        this.next = null;
    }
}

class MyList {
    Node head;

    public MyList() {
        this.head = null;
    }

    public Node getLastNode(Node node) {
        while (node.next != null) {
            node = node.next;
        }
        return node;
    }

    public void display() {
        Node current = head;
        while (current != null) {
            System.out.print(current.data + " ");
            current = current.next;
        }
        System.out.println();
    }

    public void fileDisplay(BufferedWriter writer) throws IOException {
        Node current = head;
        while (current != null) {
            writer.write(current.data + " ");
            current = current.next;
        }
        writer.newLine();
    }

    public void myAppend(String data) {
        Node newNode = new Node(data);

        if (head == null) {
            head = newNode;
        } else {
            Node current = getLastNode(head);
            current.next = newNode;
        }
    }

    public boolean search(String dataTarget) {
        Node current = head;

        while (current != null) {
            if (current.data.equals(dataTarget)) {
                return true;
            }

            current = current.next;
        }
        return false;
    }

    public int getLength() {
        int counter = 0;
        Node current = head;
        while (current != null) {
            counter++;
            current = current.next;
        }

        return counter;
    }

    public Node getNNode(int n) {
        Node current = head;
        int count = 1;

        while (current != null && count < n) {
            current = current.next;
            count++;
        }

        return current;
    }

    public void tokenizeTrie() {
        Node current = head;
        while (current != null) {
            analysisProgram.wordTrie.insert(current.data);
            current = current.next;
        }
        System.out.println();
    }
}

class TrieNode {
    TrieNode[] children;
    boolean isWord;

    public TrieNode() {
        children = new TrieNode[26];
        isWord = false;
    }
}

class Trie {
    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toLowerCase().toCharArray()) {
            int index = c - 'a';
            if (index < 0 || index >= 26) {
                continue;
            }
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }
            node = node.children[index];
        }
        node.isWord = true;
    }

    public boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                return false;
            }
            node = node.children[index];
        }
        return node.isWord;
    }

    public boolean startsWith(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                return false;
            }
            node = node.children[index];
        }
        return true;
    }
}
