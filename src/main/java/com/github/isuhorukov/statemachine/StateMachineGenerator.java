package com.github.isuhorukov.statemachine;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class StateMachineGenerator {
    public static void main(String[] args) throws Exception{
        Set<String> states = new HashSet<>();
        List<String> transitions = new ArrayList<>();

        String stateMachineSource = StateMachineGenerator.class.getResource("/education.statemachine").getFile();
        try (Stream<String> lines =  Files.lines(Paths.get(stateMachineSource))){
            lines.forEach(line->{ //A   B   STATE   TRANSITION_RULE
                String[] parts = line.split("\t");
                states.add("state \""+parts[2]+"\" as state"+parts[1]);
                transitions.add("state"+parts[0]+" --> state"+parts[1]+" : "+parts[3]);
            });
        }
        String uml = "@startuml\n" + String.join("\n", states) + "\n" + String.join("\n", transitions) + "\n@enduml";
        try(Writer writer = new FileWriter(new File("stateMachineSource").getName()+".puml")){
            writer.write("@startuml\n");
            writer.write(String.join("\n", states));
            writer.write("\n");
            writer.write(String.join("\n", transitions));
            writer.write( "\n@enduml");
        }
    }
}
