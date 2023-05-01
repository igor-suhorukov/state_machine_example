import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class ConvertDrawIoToPlantUml {
    public static void main(String[] args) throws Exception{
        String file = ConvertDrawIoToPlantUml.class.getResource("Untitled Diagram.drawio").getFile();
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList expressions = (NodeList) xPath.evaluate(
                            "//mxCell[starts-with(./@style,'edgeLabel')]", document, XPathConstants.NODESET);
        for(int idx = 0; idx < expressions.getLength(); idx++){
            Element item = (Element) expressions.item(idx);
            String value = item.getAttribute("value");
            String parent = item.getAttribute("parent");
            Element edge = getElement(document, xPath, parent);
            String source = edge.getAttribute("source");
            String target = edge.getAttribute("target");
            Element sourceState = getElement(document, xPath, source);
            String sourceStateName = sourceState.getAttribute("value");
            Element targetState = getElement(document, xPath, target);
            String targetStateName = targetState.getAttribute("value");
        }
    }

    private static Element getElement(Document document, XPath xPath, String id) throws XPathExpressionException {
        return (Element) xPath.evaluate(
                String.format("//mxCell[@id='%s']", id), document, XPathConstants.NODE);
    }
}
