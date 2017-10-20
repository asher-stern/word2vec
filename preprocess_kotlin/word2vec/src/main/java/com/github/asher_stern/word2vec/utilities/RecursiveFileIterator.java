package com.github.asher_stern.word2vec.utilities;

import org.apache.commons.collections4.iterators.ObjectArrayIterator;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * Iterator over all the files in a given directory and all its sub-directories recursively.
 * <br>
 * This iterator is similar to UNIX <tt>find</tt> command.
 *
 *
 * <p>
 * Date: Apr 3, 2017
 * @author Asher Stern
 *
 */
public class RecursiveFileIterator implements Iterator<File>
{
    /**
     * Constructor with the root directory.
     * @param root the root directory.
     */
    public RecursiveFileIterator(File root)
    {
        this(root, null, null, null);
    }

    /**
     * Constructor with the root directory and filters.
     * @param root the root directory.
     * @param firstDirectoriesFilter Filter for the immediate sub-directories of the root directories (i.e, not sub-sub-directories).
     * @param fileFilter Filter for files (i.e., not directories)
     * @param directoryFilter Filter for directories (all the sub-directories, recursively). Applied on directories only, not files.
     */
    public RecursiveFileIterator(File root, FileFilter firstDirectoriesFilter, FileFilter fileFilter, FileFilter directoryFilter)
    {
        super();
        this.root = root;
        this.firstDirectoriesFilter = firstDirectoriesFilter;
        this.fileFilter = fileFilter;
        this.directoryFilter = directoryFilter;

        if (!root.isDirectory()) {throw new RuntimeException("Given root directory is not a directory (or does not exist): "+root.getAbsolutePath());}
        Queue<File> firstQueue = new LinkedList<>();
        firstQueue.add(root);
        stack = new Stack<>();
        stack.push(firstQueue);
        findNextFile();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext()
    {
        return (nextFile != null);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public File next()
    {
        File ret = nextFile;
        findNextFile();
        return ret;
    }


    private void findNextFile()
    {
        boolean doItAgain = false;
        do
        {
            doItAgain = false;
            nextFile = null;
            if ((currentDirectoryIterator!=null)&&(currentDirectoryIterator.hasNext()))
            {
                nextFile = currentDirectoryIterator.next();
            }
            else
            {
                if (!stack.isEmpty())
                {
                    Queue<File> currentQueue = null;
                    while ( (!stack.isEmpty()) && (currentQueue==null) )
                    {
                        currentQueue = stack.pop();
                        if (currentQueue.isEmpty())
                        {
                            currentQueue = null;
                        }
                    }
                    if (currentQueue!=null)
                    {
                        File directory = currentQueue.remove();
                        stack.push(currentQueue);

                        File[] subDirectories = getDirectoriesOfDirectory(directory);
                        if ( (subDirectories!=null) && (subDirectories.length>0) )
                        {
                            Queue<File> newQueue = new LinkedList<>();
                            for (File subDir : subDirectories)
                            {
                                newQueue.add(subDir);
                            }
                            stack.push(newQueue);
                        }

                        File[] files = getFiles_notDirectories_OfDirectory(directory);
                        if ( (files!=null) && (files.length>0) )
                        {
                            currentDirectoryIterator = new ObjectArrayIterator<>(files);
                            nextFile = currentDirectoryIterator.next();
                        }
                        else
                        {
                            doItAgain = true;
                        }
                    }
                }
            }
        }while(doItAgain);
    }

    private File[] getFiles_notDirectories_OfDirectory(File dir)
    {
        return dir.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return
                        (pathname.isFile())
                                &&
                                ((fileFilter!=null)?fileFilter.accept(pathname):true)
                        ;
            }
        });
    }
    private File[] getDirectoriesOfDirectory(File dir)
    {
        return dir.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return
                        (pathname.isDirectory())
                                &&
                                (root.equals(dir)?( (firstDirectoriesFilter!=null)?firstDirectoriesFilter.accept(pathname):true ):true)
                                &&
                                ((directoryFilter!=null)?directoryFilter.accept(pathname):true)
                        ;
            }
        });
    }


    private final File root;

    private final FileFilter firstDirectoriesFilter;
    private final FileFilter fileFilter;
    private final FileFilter directoryFilter;


    private Stack<Queue<File>> stack;
    private Iterator<File> currentDirectoryIterator = null;
    private File nextFile = null;
}
