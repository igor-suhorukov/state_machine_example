package com.github.isuhorukov.statemachine;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StateMachineGenerator {
    public static void main(String[] args) throws Exception{
        Set<String> states = new HashSet<>();
        List<String> transitions = new ArrayList<>();

        String stateMachineSource = StateMachineGenerator.class.getResource("/education.statemachine").getFile();
        TreeMap<Integer, TreeMap<Integer,String>> transition = new TreeMap<>();
        TreeMap<Integer,String> stateName=new TreeMap<>();
        ArrayList<String> transitionType = new ArrayList<>();
        ArrayList<String> calculated = new ArrayList<>();
        try (Stream<String> lines =  Files.lines(Paths.get(stateMachineSource))){
            lines.forEach(line->{ //A   B   STATE   TRANSITION_RULE
                String[] parts = line.split("\t");
                stateName.put(Integer.parseInt(parts[1]), parts[2]);
                states.add("state \""+parts[2]+"\" as state"+parts[1]);
                if(parts.length<4){
                    return;
                }
                final int state = Integer.parseInt(parts[0]);
                transition.computeIfAbsent(state, stateNum -> new TreeMap<>());
                transition.get(state).put(Integer.parseInt(parts[1]), parts[3]);
                transitions.add("state"+parts[0]+" --> state"+parts[1]+" : "+parts[3]);
                transitionType.add("st"+parts[0]+"_"+parts[1]+" boolean");
                calculated.add(parts[3]);
            });
        }
        final String collect = "select CASE\n" + transition.entrySet().stream().map(fromEntry -> {
            final StringBuilder builder = new StringBuilder();
            builder.append(" WHEN state=").append(fromEntry.getKey()).append(" THEN ")
                    .append("--").append(stateName.get(fromEntry.getKey())).append('\n');
            builder.append("\tCASE\n").append(fromEntry.getValue().entrySet().stream()
                    .map(toEntry -> "\t\tWHEN transition.st" + fromEntry.getKey() + "_" + toEntry.getKey()
                            + " -- " + toEntry.getValue()
                            + "\n\t\t\tTHEN " + toEntry.getKey()
                            + " --" + stateName.get(toEntry.getKey())).collect(Collectors.joining("\n"))).
                    append("\n\t\tELSE state\n\tEND\n");
            return builder.toString();
        }).collect(Collectors.joining())+"\tELSE state\nEND\n";

        String statesSQL = "(VALUES "+stateName.entrySet().stream().map(stateNameEntry -> "("
                + stateNameEntry.getKey() + ", '"
                + stateNameEntry.getValue() + "')").collect(Collectors.joining(", "))
                + ") AS state_name(state, name)";

        String type = "CREATE TYPE transition_parameter AS ("+String.join(",", transitionType)+")";
        final String query = "select *, fsm((" + String.join(",\n", calculated) + ")) over (PARTITION BY name ORDER BY age) from life order by name,age"; //over (PARTITION BY name ORDER BY age)
        // CREATE TABLE life( name text, age int, desire_to_learn boolean, exams text, CONSTRAINT pk_life PRIMARY KEY (name,age));
        // \copy life from 'state_machine_example/src/main/resources/life.txt';
        // select *, (age>=3 and desire_to_learn) st0_1, (age>=7 and exams is null and desire_to_learn) st1_2, (age>=11 and exams is null and desire_to_learn) st2_3, (age>=15 and exams='выпускные экзамены в 9 классе') st3_4, (age>=16 and exams='выпускные экзамены в 9 классе' and desire_to_learn) st4_5, (age>=17 and exams='выпускные экзамены в 11 классе') st5_6, (age>=16 and exams='вступительные экзамены в техникум' and desire_to_learn) st4_7, (age>=18 and exams='защита диплома') st7_8, (age>=18 and exams='вступительные экзамены в ВУЗ' and desire_to_learn) st6_9, (age>=18 and exams='вступительные экзамены в ВУЗ' and desire_to_learn) st8_9, (age>=22 and exams='защита диплома' and desire_to_learn) st9_10, (age>=22  and exams='экзамены в аспирантуру' and desire_to_learn) st10_11, (age>=24 and exams='кандидатский минимум' and desire_to_learn) st11_12, (age>=25 and exams='защита диссертации' and desire_to_learn) st12_13, (not(desire_to_learn)) st4_14, (not(desire_to_learn)) st4_14, (not(desire_to_learn)) st6_14, (not(desire_to_learn)) st8_14, (not(desire_to_learn)) st10_14, (not(desire_to_learn)) st13_14 from life order by name,age
        // select hstore(ARRAY['st0_0','true','st0_1','false','st1_2','true']) from (values('aaa',true,false,true)) as t(id,st0_0,st0_1,st1_2);
        //String uml = "@startuml\n" + String.join("\n", states) + "\n" + String.join("\n", transitions) + "\n@enduml";
        try(Writer writer = new FileWriter(new File("stateMachineSource").getName()+".puml")){
            writer.write("@startuml\n");
            writer.write(String.join("\n", states));
            writer.write("\n");
            writer.write(String.join("\n", transitions));
            writer.write( "\n@enduml");
        }
    }
}
