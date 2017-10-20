package com.github.asher_stern.word2vec.corpora

import com.github.asher_stern.word2vec.utilities.RecursiveFileIterator
import org.apache.uima.UimaContext
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase
import org.apache.uima.fit.descriptor.ConfigurationParameter
import org.apache.uima.jcas.JCas
import org.apache.uima.util.Progress
import java.io.File

/**
 * Created by Asher Stern on October-19 2017.
 */


/**
 * Total 4671272 sentences
 */
class BncCollectionReader : JCasCollectionReader_ImplBase()
{
    companion object
    {
        const val DIRECTORY_PARAM = "BncCollectionReader.directory"
    }
    @field:ConfigurationParameter(name = DIRECTORY_PARAM, mandatory = true)
    private var directory: String? = null


    override fun initialize(context: UimaContext?)
    {
        super.initialize(context)
        iterator = RecursiveFileIterator(File(directory!!), null, { it.name.endsWith(".xml") }, null )
    }

    override fun getProgress(): Array<Progress>? = null

    override fun hasNext(): Boolean = iterator!!.hasNext()

    override fun getNext(jCas: JCas)
    {
        val file = iterator!!.next()
        BncDocumentLoader(jCas, file, true).load()
    }


    private var iterator: Iterator<File>? = null
}