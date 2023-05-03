import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.*;
import java.util.stream.Collectors;

public class ConvertDrawIoToPlantUml {
    private static class Transition{
        String from;
        String to;
        String rule;

        public Transition(String from, String to, String rule) {
            this.from = from;
            this.to = to;
            this.rule = rule;
        }
    }
    private static class State{
        String state;
        String description;

        public State(String state, String description) {
            this.state = state;
            this.description = description;
        }
    }
    public static void main(String[] args) throws Exception{
        String file = ConvertDrawIoToPlantUml.class.getResource("Untitled Diagram.drawio").getFile();
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        XPath xPath = XPathFactory.newInstance().newXPath();
        Map<String,State> states = new LinkedHashMap<>();
        List<Transition> transitions = new ArrayList<>();
        NodeList expressions = (NodeList) xPath.evaluate(
                            "//mxCell[starts-with(./@style,'edgeLabel') " +
                                    " or (starts-with(./@style,'edgeStyle') and @value)]",
                document, XPathConstants.NODESET);
        for(int idx = 0; idx < expressions.getLength(); idx++){
            Element item = (Element) expressions.item(idx);
            String edgeRule = item.getAttribute("value");
            String parent = item.getAttribute("parent");
            Element edge = "1".equals(parent)? item : getElement(document, xPath, parent);
            String source = edge.getAttribute("source");
            String target = edge.getAttribute("target");
            Element sourceState = getElement(document, xPath, source);
            String sourceStateName = getStateName(sourceState);
            String sourceDescription = getDescription(document, xPath, sourceState);
            states.put(source, new State(sourceStateName, sourceDescription));
            Element targetState = getElement(document, xPath, target);
            String targetStateName = getStateName(targetState);
            String targetDescription = getDescription(document, xPath, targetState);
            states.put(target, new State(targetStateName, targetDescription));
            transitions.add(new Transition(source,target,edgeRule));
        }
        Map<String, String> stateIdToNumber = new HashMap<>();
        String initState = states.entrySet().stream().filter(stringStringEntry ->
                "startState".equals(stringStringEntry.getValue().state)).findFirst().get().getKey();
        stateIdToNumber.put(initState, "[*]");
        String endState = states.entrySet().stream().filter(stringStringEntry ->
                "endState".equals(stringStringEntry.getValue().state)).findFirst().get().getKey();
        stateIdToNumber.put(endState, "[*]");
        List<Map.Entry<String, State>> entries = states.entrySet().stream().
                filter(stringStringEntry -> !("startState".equals(stringStringEntry.getValue().state)
                                                || "endState".equals(stringStringEntry.getValue().state))).
                collect(Collectors.toList());
        for(int idx = 0; idx < entries.size(); idx++){
            stateIdToNumber.put(entries.get(idx).getKey(), Integer.toString(idx));
        }
        StringBuilder plantUml = new StringBuilder();
        plantUml.append("@startuml\nhide empty description\n");
        states.forEach((stateId, state) -> {
            if("[*]".equals(stateIdToNumber.get(stateId))){
                return;
            }
            plantUml.append("state \"").append(state.state).append("\" as state").
                    append(stateIdToNumber.get(stateId)).append("\n");
            if(state.description != null){
                plantUml.append("state").append(stateIdToNumber.get(stateId)).append(" : ").
                        append(state.description).append("\n");
            }
        });
        transitions.forEach(transition -> {
            plantUml.append("[*]".equals(stateIdToNumber.get(transition.from))?"":"state").
                        append(stateIdToNumber.get(transition.from)).append(" --> ").
                        append("[*]".equals(stateIdToNumber.get(transition.to))?"":"state").
                        append(stateIdToNumber.get(transition.to)).append(" : ").
                        append(transition.rule).append("\n");
        });
        plantUml.append("@enduml\n");
        System.out.println(plantUml.toString());
    }

    private static String getDescription(Document document, XPath xPath, Element state) throws XPathExpressionException {
        String id = state.getAttribute("id");
        String description = (String) xPath.evaluate(
                String.format("//mxCell[./@parent='%s' and starts-with(./@style,'text')]/@value", id),
                document, XPathConstants.STRING);
        return !description.isEmpty() ? description: null;
    }

    private static String getStateName(Element state) {
        String stateName = state.getAttribute("value");
        if(stateName.isEmpty()){
            NamedNodeMap attributes = state.getAttributes();
            for(int attrIdx = 0 ; attrIdx < attributes.getLength(); attrIdx++) {
                String attrValue = attributes.item(attrIdx).getTextContent();
                if(attrValue.contains("shape=startState;")){
                    stateName = "startState";
                    break;
                } else if(attrValue.contains("shape=endState")){
                    stateName = "endState";
                    break;
                }
            }
        }
        return stateName;
    }

    private static Element getElement(Document document, XPath xPath, String id) throws XPathExpressionException {
        return (Element) xPath.evaluate(
                String.format("//mxCell[@id='%s']", id), document, XPathConstants.NODE);
    }
}
