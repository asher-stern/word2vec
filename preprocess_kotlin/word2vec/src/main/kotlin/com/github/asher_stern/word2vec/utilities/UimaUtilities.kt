package com.github.asher_stern.word2vec.utilities

import org.apache.uima.jcas.JCas

/**
 * Created by Asher Stern on October-20 2017.
 */

fun JCas._use(block: (JCas)->Unit)
{
    try
    {
        block(this)
    }
    finally
    {
        this.release()
    }
}