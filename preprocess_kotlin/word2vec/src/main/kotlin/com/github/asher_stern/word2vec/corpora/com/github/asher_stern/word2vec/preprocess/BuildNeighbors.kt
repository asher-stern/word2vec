package com.github.asher_stern.word2vec.corpora.com.github.asher_stern.word2vec.preprocess


import com.github.asher_stern.word2vec.utilities._provide
import org.apache.commons.collections4.BidiMap
import org.apache.commons.collections4.bidimap.DualLinkedHashBidiMap
import java.io.File

/**
 * Created by Asher Stern on October-16 2017.
 */

const val UNKNOWN = "__UNKNOWN__"
const val OUTPUT_WORDS = false


fun main(args: Array<String>)
{
    args._provide {
        BuildNeighbors(File(arg), File(arg), arg.toInt(), File(arg), arg.toInt(), arg.toInt()).use { it.build() }
    }
}

class BuildNeighbors(
        private val wordFile: File,
        private val sentenceFile: File,
        private val numberOfWords: Int,
        private val outputFile: File,
        private val windowSize: Int,
        private val numberOfStopWords: Int
) : AutoCloseable
{

    fun build()
    {
        sentenceFile.bufferedReader().useLines { lines ->
            var index = 0
            for ( line in lines)
            {
                write(line.split("\\s+".toRegex()).toTypedArray())

                ++index
                if (0==(index % 100)) { println(index) }
            }
            println(index)
        }
    }

    override fun close()
    {
        output.close()
    }


    private fun write(sentence: Array<String>)
    {
        if (sentence.size>=(2*windowSize+1))
        {
            for (index in windowSize until (sentence.size-windowSize))
            {
                if (wordMap.containsKey(sentence[index]))
                {
                    val wordId = wordMap.getOrDefault(sentence[index], 0)
                    if (wordId > numberOfStopWords)
                    {
                        for (contextIndex in (index - windowSize)..(index + windowSize))
                        {
                            if (contextIndex != index)
                            {
                                if (wordMap.containsKey(sentence[contextIndex]))
                                {
                                    val contextWordId = wordMap.getOrDefault(sentence[contextIndex], 0)
                                    if (contextWordId > numberOfStopWords)
                                    {
                                        if (OUTPUT_WORDS)
                                        {
                                            output.println("${sentence[index].asRegistered} ${sentence[contextIndex].asRegistered}")
                                        }
                                        else
                                        {
                                            output.println("$wordId $contextWordId")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    val String.asRegistered: String
        get() = if (wordMap.containsKey(this)) this else UNKNOWN

    private val wordMap: BidiMap<String, Int> = loadWords(wordFile, numberOfWords)
    private val output = outputFile.printWriter()
}


private fun loadWords(wordFile: File, numberOfWords: Int): BidiMap<String, Int>
{
    val ret = DualLinkedHashBidiMap<String, Int>()

    wordFile.bufferedReader().useLines { lines->
        for ( (index,line) in (sequenceOf(UNKNOWN)+lines).withIndex())
        {
            if (index>numberOfWords) { break }
            ret.put(line, index)
        }
    }

    return ret
}