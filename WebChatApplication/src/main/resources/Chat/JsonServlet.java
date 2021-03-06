package MyPackage.WebChatApplication.src.main.resources.Chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
 
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JsonServlet extends HttpServlet {
    private List<Message> history = new ArrayList<Message>();
    private MessageExchange messageExchange = new MessageExchange();
    private Document doc;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            File xmlFile = new File("file.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile);
            NodeList nodeList = doc.getElementsByTagName("message");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nNode = nodeList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    	    		Element eElement = (Element)nNode;
                    String dateNode = eElement.getElementsByTagName("date").item(0).getTextContent();
                    String author = eElement.getElementsByTagName("author").item(0).getTextContent();
                    String text = eElement.getElementsByTagName("text").item(0).getTextContent();
                    System.out.println(dateNode + " " + author + " : " + text);
                    history.add(new Message(text, author));
		        }
            }
        }
        catch (Exception e){}
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            ServletOutputStream soStream = res.getOutputStream();
            soStream.println(messageExchange.getServerResponse(history));
        }
        catch (Exception e) {}
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Message message = messageExchange.getClientMessage(request.getInputStream());
            MessageExchange ms = new MessageExchange();
            if (doc == null) {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                doc = docBuilder.newDocument();
                doc.appendChild(doc.createElement("messages"));
            }
            Element root = doc.getElementById("messages");
            Element messageNode = doc.createElement("message");
            Element author = doc.createElement("author");
            author.appendChild(doc.createTextNode(message.getAuthor()));
            Element dateNode = doc.createElement("date");
            Element text = doc.createElement("text");
            text.appendChild(doc.createTextNode(message.getText()));
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yy HH:mm");
            Date date = new Date();
            dateNode.appendChild(doc.createTextNode(dateFormat.format(d)));
            messageNode.appendChild(author);
            messageNode.appendChild(text);
            messageNode.appendChild(dateNode);
            root.appendChild(messageNode);
            history.add(message);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("file.xml"));
            try {
                transformer.transform(domSource, result);
            } catch (TransformerException ex) {
                Logger.getLogger(JsonServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(dateformat.format(date) + " " + message.getAuthor() + " : " + message.getText());
        }
        catch (ParseException e) {
            System.err.println("Invalid message: " + e.getMessage());
        }
        catch (TransformerConfigurationException e) {}
        catch (ParserConfigurationException e) {}
    }
}