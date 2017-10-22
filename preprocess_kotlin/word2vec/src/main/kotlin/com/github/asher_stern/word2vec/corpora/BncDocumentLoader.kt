package com.github.asher_stern.word2vec.corpora

import com.github.asher_stern.word2vec.utilities.BeginEnd
import com.github.asher_stern.word2vec.utilities.XmlDomElement
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token
import org.apache.uima.jcas.JCas
import java.io.File
import java.util.*

/**
 * Created by Asher Stern on October-19 2017.
 */

class BncDocumentLoader(private val cas: JCas, private val file: File, private val includeHeads: Boolean = false)
{
    fun load()
    {
        readXml()
        setCasContents()
    }


    private fun readXml()
    {
        val root = XmlDomElement.fromFile(file)

        val wtext = root.getChildElements("wtext")
        val stext = root.getChildElements("stext")
        var textElement: XmlDomElement? = null
        if ( (wtext.size==1) && (stext.size==0) ) { textElement=wtext.first() }
        else if ( (wtext.size==0) && (stext.size==1) ) { textElement=stext.first() }
        else throw RuntimeException("Cannot detect text element. wtext.size = ${wtext.size}. stext.size = ${stext.size}")

        for (paragraph in xmlListParagraphs(textElement!!))
        {
            addParagraph(paragraph)
        }
    }

    private fun xmlListParagraphs(base: XmlDomElement): List<XmlDomElement>
    {
        val ret = mutableListOf<XmlDomElement>()
        xmlListParagraphs(base, ret)
        return ret
    }

    private fun xmlListParagraphs(base: XmlDomElement, list: MutableList<XmlDomElement>)
    {
        for (element in base.childElements)
        {
            val tag = element.element.tagName
            when (tag)
            {
                "head" -> if (includeHeads) { list.add(element) }
                "p", "item" -> list.add(element)
                else -> xmlListParagraphs(element, list)
            }
        }
    }


    private fun addParagraph(paragraph: XmlDomElement)
    {
        for (sentence in paragraph.getChildElements("s"))
        {
            if (text.length > 0) { text.append(" ") }
            val beginSentence = text.length

            var firstIteration = true
            for (word in xmlListWords(sentence))
            {
                val tag = word.element.tagName

                if (firstIteration) { firstIteration = false }
                else
                {
                    if (tag != "c")
                    {
                        text.append(" ")
                    }
                }

                val surface: String? = word.getText(true, false, false, "")
                if (surface != null)
                {
                    val lemma = if (tag == "c") surface else word.element.getAttribute("hw")._ifEmpty(surface)

                    var pos = if (tag == "c") "PUNC" else word.element.getAttribute("pos")._ifEmpty("O")
                    pos = mapPos(pos)
                    tokens.add(TokenToAnnotate(BeginEnd(text.length, text.length + surface.length), pos, lemma))
                    text.append(surface)
                }
            }
            sentences.add(BeginEnd(beginSentence, text.length))
        }
    }

    private fun setCasContents()
    {
        cas.reset()
        cas.documentLanguage = "en"
        cas.documentText = text.toString()

        for (sentence in sentences)
        {
            Sentence(cas, sentence.begin, sentence.end).addToIndexes()
        }

        for (token in tokens)
        {
            val tokenAnnotation = Token(cas, token.beginEnd.begin, token.beginEnd.end)
            val posAnnotation = Class.forName(POS_PACKAGE+token.pos).getConstructor(JCas::class.java, java.lang.Integer.TYPE, java.lang.Integer.TYPE).newInstance(cas, token.beginEnd.begin, token.beginEnd.end) as POS
            val lemmaAnnotation = Lemma(cas, token.beginEnd.begin, token.beginEnd.end)
            lemmaAnnotation.value = token.lemma

            posAnnotation.addToIndexes()
            lemmaAnnotation.addToIndexes()
            tokenAnnotation.pos = posAnnotation
            tokenAnnotation.lemma = lemmaAnnotation
            tokenAnnotation.addToIndexes()
        }
    }


    private data class TokenToAnnotate(
            val beginEnd: BeginEnd,
            val pos: String,
            val lemma: String
    )

    companion object
    {
        val POS_PACKAGE = POS::class.java.`package`.name+"."
    }


    private val text = StringBuilder()
    private val tokens = mutableListOf<TokenToAnnotate>()
    private val sentences = mutableListOf<BeginEnd>()
}


private fun xmlListWords(sentence: XmlDomElement): List<XmlDomElement>
{
    val ret = mutableListOf<XmlDomElement>()

    val stack = Stack<XmlDomElement>()
    stack.push(sentence)
    while (!stack.empty())
    {
        val element = stack.pop()
        val tag = element.element.tagName
        if ( (tag == "w") || (tag == "c") )
        {
            ret.add(element)
        }
        else
        {
            for (child in element.childElements.reversed())
            {
                stack.push(child)
            }
        }
    }

    return ret
}

private fun String._ifEmpty(other: String): String
{
    if (this.isEmpty()) return other
    return this
}

private fun mapPos(given: String): String
{
    return when(given)
    {
        "CONJ" -> "CONJ"
        "SUBST" -> "N"
        "VERB" -> "V"
        "ART" -> "ART"
        "ADJ" -> "ADJ"
        "PREP" -> "PP"
        "ADV" -> "ADV"
        "PRON" -> "PR"
        "PUNC" -> "PUNC"
        "UNC" -> "O"
        "INTERJ" -> "O"
        else -> throw RuntimeException("Unrecognized POS: $given")
    }
}
