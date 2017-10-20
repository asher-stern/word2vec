package com.github.asher_stern.word2vec.corpora;

import com.github.asher_stern.word2vec.utilities.XmlDomElement;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;


/**
 * Parses a single Reuters-corpus file (a single document).
 * <br>
 * The file is an XML file, with several XML-elements, amongst which the most important is the one that contains the text
 * (the document contents).
 *
 * <p>
 * Date: Apr 9, 2017
 * @author Asher Stern
 *
 */
public class ReutersParser
{
    /**
     * An enum determining how to process the text. As a single bunch of text, or as a list of paragraphs.
     */
    public static enum ParseType {TEXT, PARAGRAPHS}

    /**
     * Constructor with the entire Reuters XML file as a string.
     * @param xml the XML contents.
     */
    public ReutersParser(String xml)
    {
        super();
        this.xml = xml;
    }

    /**
     * Builds {@link ReutersParser} from an XML file.
     * @param file an XML file, which is a single Reuters document.
     * @return {@link ReutersParser}
     */
    public static ReutersParser fromFile(File file)
    {
        try(Reader reader = new FileReader(file))
        {
            return new ReutersParser(IOUtils.toString(reader));
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse the XML given in the constructor. After calling this method, getters can be called.
     * @param parseType determines whether it is required to get the entire text as a single string, or get the text as a list
     * of strings, each contains a single paragraph.
     */
    public void parse(ParseType parseType)
    {
        text = null;
        paragraphs = null;
        StringBuilder sb = null;
        switch(parseType)
        {
            case PARAGRAPHS: paragraphs=new LinkedList<>(); break;
            case TEXT: sb=new StringBuilder(); break;
            default: throw new RuntimeException();
        }

        try
        {
            XmlDomElement root = XmlDomElement.fromString(xml);
            XmlDomElement textElement = root.getSingleChildElement("text");
            boolean firstIteration = true;
            for (XmlDomElement paragraphElement : textElement.getChildElements("p"))
            {
                String text = paragraphElement.getText(true, false, true, "");
                if ( (text!=null) && (text.length()>0) )
                {
                    if (parseType==ParseType.TEXT)
                    {
                        if (!firstIteration) {sb.append(" ");}
                        sb.append(text);
                    }
                    else if (parseType==ParseType.PARAGRAPHS)
                    {
                        paragraphs.add(text);
                    }
                    else {throw new RuntimeException();}

                    firstIteration = false;
                }
            }
            if (parseType==ParseType.TEXT) {this.text = sb.toString();}
        }
        catch (IOException | SAXException | ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }



    /**
     * Returns the entire text, as retrieved in {@link #parse(ParseType)}, assuming that the {@link ParseType} was {@link ParseType#TEXT}
     * @return the entire document text.
     */
    public String getText()
    {
        if (null==text) {throw new RuntimeException("Not parsed for text.");}
        return text;
    }

    /**
     * Returns a list of paragraph, retrieved by {@link #parse(ParseType)}, assuming that the {@link ParseType} was {@link ParseType#PARAGRAPHS}.
     * @return a list of the document paragraphs.
     */
    public List<String> getParagraphs()
    {
        if (null==paragraphs) {throw new RuntimeException("Not parsed for paragraphs.");}
        return paragraphs;
    }



    private final String xml;

    private String text = null;
    private List<String> paragraphs = null;
}
