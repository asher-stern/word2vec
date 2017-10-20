package com.github.asher_stern.word2vec.corpora

import com.github.asher_stern.word2vec.utilities.RecursiveFileIterator
import com.github.asher_stern.word2vec.utilities._provide
import java.io.File
import java.nio.file.Files
import java.util.*


/**
 * Created by Asher Stern on October-20 2017.
 */


fun main(args: Array<String>)
{
    args._provide {
        BncCopyFractionRandomly(File(arg), File(arg), arg.toDouble()).copy()
    }
}

class BncCopyFractionRandomly(private val rootDirectory: File, private val destination: File, private val fraction: Double)
{
    fun copy()
    {
        if(destination.listFiles().count()>0) { throw RuntimeException("Destination directory is not empty") }
        val iterator = RecursiveFileIterator(rootDirectory)
        while (iterator.hasNext())
        {
            val file = iterator.next()
            if (random.nextDouble()<fraction)
            {
                val newFile = changePath(file)
                val parent = newFile.parentFile
                if (!parent.exists()) { parent.mkdirs() }
                println("copy ${file.absolutePath} to ${newFile.absolutePath}")
                Files.copy(file.toPath(), newFile.toPath())
            }
        }
    }


    private fun changePath(file: File): File = File(destinationString+file.absolutePath.substring(rootString.length))


    private val random = Random()
    private val rootString = rootDirectory.absolutePath
    private val destinationString: String
    init
    {
        var destinationStr = destination.absolutePath
        if (!destinationStr.endsWith(File.separator)) { destinationStr += File.separator }
        destinationString = destinationStr
    }
}