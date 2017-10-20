package com.github.asher_stern.word2vec.corpora;

import com.github.asher_stern.word2vec.utilities.RecursiveFileIterator;

import java.io.File;
import java.util.regex.Pattern;

/**
 * An iterator over all the files in Reuters corpus RCV1.
 *
 * <p>
 * Date: 25 May 2017
 * @author Asher Stern
 *
 */
public class ReutersIterator extends RecursiveFileIterator
{
    public ReutersIterator(String rootDirectory)
    {
        super(new File(rootDirectory),
                (f)->digitsOnly.matcher(f.getName()).matches(),
                (f)->f.getAbsolutePath().endsWith(".xml"),
                null
        );
    }

    private static Pattern digitsOnly = Pattern.compile("\\d+");
}
