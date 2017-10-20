package com.github.asher_stern.word2vec.corpora;

import com.github.asher_stern.word2vec.corpora.ReutersParser.ParseType;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * A UIMA collection reader for Reuters corpus, RCV1.
 *
 * <p>
 * Date: 15 Jun 2017
 * @author Asher Stern
 *
 */
public class ReutersCollectionReader extends JCasCollectionReader_ImplBase
{
    public static final String LANGUAGE = "en";

    public static final String DIRECTORY_PARAM = "directory";
    @ConfigurationParameter(name=DIRECTORY_PARAM, mandatory=true)
    private String directory;



    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);
        if (null==directory) throw new ResourceInitializationException(new RuntimeException("Null directory."));
        iterator = new ReutersIterator(directory);
//		iterator = new RecursiveFileIterator(new File(directory), null, (f)->f.getPath().endsWith(".xml"), null);
    }


    @Override
    public boolean hasNext() throws IOException, CollectionException
    {
        return iterator.hasNext();
    }


    @Override
    public void getNext(JCas jCas) throws IOException, CollectionException
    {
        File file = iterator.next();
        jCas.reset();
        jCas.setDocumentLanguage(LANGUAGE);
        ReutersParser reutersParser = ReutersParser.fromFile(file);
        reutersParser.parse(ParseType.TEXT);
        jCas.setDocumentText(reutersParser.getText());
    }

    @Override
    public Progress[] getProgress()
    {
        return null;
    }


    private Iterator<File> iterator = null;
}
