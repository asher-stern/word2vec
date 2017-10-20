package com.github.asher_stern.word2vec.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



/**
 * Wraps a DOM {@link Element} and provides convenient methods to get its text and its children.
 * Also, provides method to append text and new sub-elements to this element.
 *
 * <p>
 * Date: Apr 9, 2017
 * @author Asher Stern
 *
 */
public class XmlDomElement
{
    /**
     * A static method to create an empty XML document.
     * The document's <code>getDocumentElement()</code> can be called to get the root element, which can be used
     * as an argument to {@link XmlDomElement#XmlDomElement(Element)} constructor.
     *
     * <p>
     * The document can later be used by {@link #createChildElement(Document, String)}, {@link #appendText(Document, String)},
     * and  {@link #writeDocumentToFile(Document, File)}.
     *
     * @return a new empty XML document
     * @throws ParserConfigurationException
     */
    public static Document createEmptyDocument() throws ParserConfigurationException
    {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    /**
     * A static method that writes an XML document into a file.
     *
     * @param document an XML document.
     * @param file the file into which the XML will be printed.
     */
    public static void writeDocumentToFile(Document document, File file)
    {
        try
        {
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            try (FileOutputStream outputStream = new FileOutputStream(file))
            {
                StreamResult streamResult = new StreamResult(outputStream);
                DOMSource source = new DOMSource(document);
                trans.transform(source, streamResult);
            }
        }
        catch(IOException | TransformerException e)
        {
            throw new RuntimeException("Failed to write into file: "+file.getPath());
        }
    }

    /**
     * Constructs an {@link XmlDomElement} that wraps the root element of the XML file, whose contents is given.
     * @param xmlContents contents of an XML file.
     * @return the XML root element.
     */
    public static XmlDomElement fromString(String xmlContents) throws IOException, SAXException, ParserConfigurationException
    {
        try (Reader reader = new StringReader(xmlContents))
        {
            InputSource inputSource = new InputSource(reader);
            return fromDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource));
        }
    }

    /**
     * Constructs an {@link XmlDomElement} from the root element of the given XML file.
     * @param file an XML file
     * @return the XML document's root element.
     */
    public static XmlDomElement fromFile(File file) throws SAXException, IOException, ParserConfigurationException
    {
        return fromDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file));
    }

    /**
     * Constructs an {@link XmlDomElement} from the root element (document element) of the given {@link Document}.
     * @param document an XML {@link Document}.
     * @return The document's root element.
     */
    public static XmlDomElement fromDocument(Document document)
    {
        return new XmlDomElement(document.getDocumentElement());
    }

    /**
     * Constructor from {@link Element}.
     * @param element the {@link Element} to be wrapped.
     */
    public XmlDomElement(Element element)
    {
        super();
        this.element = element;
    }

    /**
     * Get the wrapped {@link Element}.
     */
    public Element getElement()
    {
        return element;
    }

    /**
     * Get the one and only one child element whose name is {@code childName}. If no such sub-element exists, or if
     * more than one such sub-element exists, an exception will be thrown.
     * @param childName name of the child element.
     * @return the child element.
     */
    public XmlDomElement getSingleChildElement(String childName)
    {
        List<XmlDomElement> list = getChildElements(childName);
        if (list.size()==1) {return list.get(0);}
        throw new RuntimeException("Number of child element whose name is \""+childName+"\" is not 1, but "+list.size());
    }

    /**
     * Get all the child elements of this element.
     */
    public List<XmlDomElement> getChildElements()
    {
        return getChildElements(null);
    }

    /**
     * Get all the child elements of this element whose name is {@code childName}.
     * @param childName the name of the sub-elements, or null to get all the sub-elements.
     * @return the child elements (the sub-elements).
     */
    public List<XmlDomElement> getChildElements(String childName)
    {
        NodeList nodeList = element.getChildNodes();
        final int length = nodeList.getLength();
        List<XmlDomElement> ret = new ArrayList<>(length);
        for (int index=0; index<length; ++index)
        {
            if (nodeList.item(index).getNodeType()==Node.ELEMENT_NODE)
            {
                boolean add = (childName==null);
                if (childName!=null) {add = childName.equals(nodeList.item(index).getNodeName());}
                if (add)
                {
                    ret.add( new XmlDomElement( (Element)nodeList.item(index) ) );
                }
            }
        }
        return ret;
    }

    /**
     * Get the text inside this element. See also {@link #getText(boolean, boolean, boolean, String)}.
     */
    public String getText()
    {
        return getText(true, true, true, "");
    }

    /**
     * Get the text inside this element.
     * <br>
     * This element might include sub-elements, and texts around them. In this case, the {@code firstOnly} parameter determines
     * whether all the texts will be returned (separated by {@code delimiter}) or only the first one.
     * <br>
     * If no text exist, and {@code textMustExist} is <tt>true</tt>, an exception will be thrown.
     * @param firstOnly whether to return only the first text (the text up to the first sub-element)
     * @param textMustExist whether a text must exist inside this node (so if no text exists, an exception will be thrown).
     * @param trim whether to trim the returned text.
     * @param delimiter if all the texts inside this node will be returned, they will be combined to a single String, separated
     * by {@code delimiter}.
     *
     * @return The text inside this element.
     */
    public String getText(boolean firstOnly, boolean textMustExist, boolean trim, String delimiter)
    {
        NodeList nodeList = element.getChildNodes();
        final int length = nodeList.getLength();
        boolean detected = false;
        StringBuilder sb = null;
        if (!firstOnly) {sb = new StringBuilder();}
        for (int index=0; index<length; ++index)
        {
            Node node = nodeList.item(index);
            if (node.getNodeType()==Node.TEXT_NODE)
            {
                detected = true;
                String text = node.getNodeValue();
                if (trim) {text=text.trim();}
                if (firstOnly)
                {
                    return text;
                }
                else
                {
                    if (sb.length()>0) {sb.append(delimiter);}
                    sb.append(text);
                }
            }
        }
        if (!detected)
        {
            if (textMustExist)
            {
                throw new RuntimeException("No text found");
            }
            else
            {
                return null;
            }
        }
        return sb.toString();
    }

    /**
     * Appends a new sub-element (child-element) to this element.
     * @param document the document to which this element belongs to.
     * @param name name of the new sub-element
     * @return
     */
    public XmlDomElement createChildElement(Document document, String name)
    {
        Element child = document.createElement(name);
        this.element.appendChild(child);
        return new XmlDomElement(child);
    }

    /**
     * Appends text to this element.
     * @param document the document to which this element belongs to.
     * @param text the text to append to this element.
     */
    public void appendText(Document document, String text)
    {
        Text textNode = document.createTextNode(text);
        this.element.appendChild(textNode);
    }



    private final Element element;
}
