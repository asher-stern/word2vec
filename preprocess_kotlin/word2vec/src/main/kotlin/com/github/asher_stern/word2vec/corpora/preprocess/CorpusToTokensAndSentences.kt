package com.github.asher_stern.word2vec.corpora.preprocess

import com.github.asher_stern.word2vec.corpora.BncCollectionReader
import com.github.asher_stern.word2vec.corpora.ReutersCollectionReader
import com.github.asher_stern.word2vec.utilities._provide
import com.github.asher_stern.word2vec.utilities._use
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolSegmenter
import org.apache.uima.collection.CollectionReader
import org.apache.uima.fit.factory.AnalysisEngineFactory
import org.apache.uima.fit.factory.CollectionReaderFactory
import org.apache.uima.fit.factory.JCasFactory
import org.apache.uima.fit.util.JCasUtil
import org.apache.uima.jcas.JCas
import java.io.File
import java.io.PrintWriter
import java.util.*
import java.util.regex.Pattern

/**
 * Created by Asher Stern on September-24 2017.
 */


fun main(args: Array<String>)
{
    args._provide {
        val corpusPath = arg
        val outputDirectory = arg
        val corpusType = CorpusType.valueOf(arg)
        CorpusToTokensAndSentences(corpusPath, outputDirectory, corpusType).go()
    }
}

class CorpusToTokensAndSentences(private val reader: CollectionReader, private val outputDirectory: String, private val corpusType: CorpusType)
{
    constructor(corpusPath: String, outputDirectory: String, corpusType: CorpusType) : this(
            when (corpusType)
            {
                CorpusType.REUTERS -> createReutersCollectionReader(corpusPath)
                CorpusType.BNC -> createBncCollectionReader(corpusPath)
            },
            outputDirectory,
            corpusType
    )

    companion object
    {
        const val SENTENCES_FILE = "sentences.txt"
        const val WORDS_FILE = "words.txt"
        const val NUMBER_OF_DISTINCT_WORDS = 10000
    }

    fun go()
    {
        val wordCount = mutableMapOf<String, Int>()
        val directory = File(outputDirectory)
        File(directory, SENTENCES_FILE).printWriter().use { writer ->
            JCasFactory.createJCas()._use { jcas ->
                go(writer, jcas, wordCount)
            }
        }

        val sortedWords = wordCount.toList().sortedByDescending { (_, v) -> v }.map { (k, _) -> k }

        File(directory, WORDS_FILE).printWriter().use { writer ->
            for (word in sortedWords.asSequence().take(NUMBER_OF_DISTINCT_WORDS))
            {
                writer.println(word)
            }
        }
    }


    private fun go(writer: PrintWriter, jcas: JCas, wordCount: MutableMap<String, Int>)
    {
        val lap = AnalysisEngineFactory.createEngine(AnalysisEngineFactory.createEngineDescription(LanguageToolSegmenter::class.java))
        var index = 0
        while (reader.hasNext())
        {
            jcas.reset()
            reader.getNext(jcas.cas)
            if (corpusType != CorpusType.BNC)
            {
                lap.process(jcas)
            }

            val indexedTokens = JCasUtil.indexCovered(jcas, Sentence::class.java, Token::class.java)

            for (sentence in jcas.getAnnotationIndex(Sentence::class.java))
            {
                val tokens = indexedTokens.getValue(sentence)
                val tokenList = normalizeTokens(tokens)
                for (token in tokenList)
                {
                    wordCount._inc(token)
                }
                writer.println(tokenList.joinToString(" "))
            }

            ++index
            if (0 == (index % 100))
            {
                println(index)
            }
        }
        println(index)
    }

    private fun normalizeTokens(tokens: Collection<Token>): List<String>
    {
        return tokens.map { it.coveredText.trim { !it.isLetterOrDigit() }.toLowerCase(Locale.ENGLISH) }.filter { it.isNotEmpty() }
    }
}


enum class CorpusType {REUTERS, BNC}


private fun createReutersCollectionReader(directory: String): CollectionReader
{
    return CollectionReaderFactory.createReader(ReutersCollectionReader::class.java, ReutersCollectionReader.DIRECTORY_PARAM, directory)
}

private fun createBncCollectionReader(directory: String): CollectionReader
{
    return CollectionReaderFactory.createReader(BncCollectionReader::class.java, BncCollectionReader.DIRECTORY_PARAM, directory)
}


private fun <K> MutableMap<K, Int>._inc(key: K)
{
    val value = this.getOrDefault(key, 0) + 1
    this.put(key, value)
}



