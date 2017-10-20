package com.github.asher_stern.word2vec.corpora.com.github.asher_stern.word2vec.preprocess


import java.io.File

/**
 * Created by Asher Stern on October-16 2017.
 */

fun main(args: Array<String>)
{
    val app = CompareWords(File(args[0]))
    while (true)
    {
        println("Enter word1, then word2")
        val word1 = readLine()!!
        val word2 = readLine()!!
        app.compare(word1, word2)
    }
}

class CompareWords(private val neighborsFile: File)
{
    fun compare(word1: String, word2: String)
    {
        val forWord1 = findForWord(word1)
        val forWord2 = findForWord(word2)

        var total = 0
        var intersection = 0
        for (neighbor in forWord1.keys.union(forWord2.keys))
        {
            total += forWord1.getOrDefault(neighbor, 0)
            total += forWord2.getOrDefault(neighbor, 0)
            intersection += Math.min(forWord1.getOrDefault(neighbor, 0), forWord2.getOrDefault(neighbor, 0))
        }

        println("Total = $total")
        println("Intersection = $intersection")
        println("Fraction = " + intersection.toDouble()/total.toDouble())
    }


    private fun findForWord(word: String): Map<String, Int>
    {
        val ret = mutableMapOf<String, Int>()
        neighborsFile.useLines { lines ->
            for (line in lines)
            {
                val (source, target) = line.split("\\s+".toRegex())
                if (source == word)
                {
                    ret._inc(target)
                }
            }
        }
        return ret
    }
}

private fun <K> MutableMap<K, Int>._inc(key: K)
{
    val value = this.getOrDefault(key, 0) + 1
    this.put(key, value)
}
