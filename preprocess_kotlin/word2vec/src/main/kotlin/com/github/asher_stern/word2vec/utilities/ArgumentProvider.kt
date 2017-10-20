package com.github.asher_stern.word2vec.utilities


/**
 * Created by Asher Stern on October-16 2017.
 */

/**
 * Provides an easy usage of command line arguments.
 * Assuming "args" is the parameter for "main" function, call args._provide { } on it, and each call to "arg" would return
 * the next argument.
 *
 */
fun Array<String>._provide(block: ArgumentProvider.()->Unit)
{
    ArgumentProvider(this).block()
}

/**
 * Used by [_provide], to get the next argument at each call to the property [arg]
 */
class ArgumentProvider(private val args: Array<String>)
{
    val arg: String
        get() = args[index++]

    val again: String
        get() = args[index-1]

    private var index = 0
}

