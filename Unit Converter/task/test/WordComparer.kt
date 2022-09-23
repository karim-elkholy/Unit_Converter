import org.hyperskill.hstest.testcase.CheckResult
import java.util.*
import kotlin.math.max
import kotlin.math.min

class InputToken(
        val content: Any,
        /** Position in range.*/
        val range: IntRange
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InputToken

        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }

    override fun toString() =
            if (content::class.simpleName == "String") "$content"
            else "$content(${content::class.simpleName})"
}

/**Compares two output strings by words (splitted by spaces, commas etc).
 * Numbers in different formats are parsed equally.
 * Doubles are compared with [roundDoubleTo] precision.
 * All integer numbers should fit to long or they will be compared as strings.
 *
 * Run [compare] function to compare.
 * @param myStr authors output string.
 * @param hisStr  students output string.
 * @param roundDoubleTo number of digits after the dot to compare doubles.
 * @param integersAsDoubles Doubles such as 1.0 are now equal to integers.
 * @param trimErrorOnlyByLine if true, ignore [errorContextStd] and trim the error by \n
 * @param errorContextStd number of words in [myStr] and [hisStr] before and after the error word.
 * @param moreIgnoredSymbolsRegex add some symbols in regex notation to ignore. For example: "\.," - will ignore dot or comma. */
class WordComparer(
        val myStr: String,
        val hisStr: String,
        val roundDoubleTo: Int = 2,
        val integersAsDoubles: Boolean = false,
        val trimErrorOnlyByLine: Boolean = true,
        val errorContextStd: Int = 1,
        val moreIgnoredSymbolsRegex: String = ""
) {
    /** Parse either double, long, word. */

    private val wordRegex = Regex("""([+-]?\d+\.\d+([eE][+-]\d+)?)|([+-]?\d+)|([^\s\d $moreIgnoredSymbolsRegex]+)""")

    private fun tokenizeWordsRegex(str: String): List<InputToken> {
        return wordRegex.findAll(str)
                .map { match ->
                    val (_, double, _, long, word) = match.groups.map { it?.value }

                    val content: Any = when {
                        long?.toLongOrNull() != null ->
                            if (integersAsDoubles) long.toDouble() else long.toLong()

                        double?.toDoubleOrNull() != null -> {
                            // Locale.US puts a dot separator
                            "%.${roundDoubleTo}f".format(Locale.US, double.toDouble()).toDouble()
                        }

                        else -> match.value
                    }
                    InputToken(content, match.range)
                }.toList()
    }


    fun compare(): CheckResult {
        // I am author
        val myTokens = tokenizeWordsRegex(myStr.toLowerCase())
        val hisTokens = tokenizeWordsRegex(hisStr.toLowerCase())

        val badTokenIdx = myTokens.zip(hisTokens).indexOfFirst { (my, his) -> my != his }

        if (badTokenIdx != -1) {
            fun substr(tokens: List<InputToken>, input: String): String {
                if (trimErrorOnlyByLine) {
                    // trim by \n
                    val badToken = tokens[badTokenIdx]
                    val start = input.take(badToken.range.start).lastIndexOf('\n').let { if (it == -1) 0 else it + 1 }
                    val end = input.indexOf('\n', badToken.range.endInclusive).let { if (it == -1) input.length else it }
                    return input.substring(start, end).trimEnd('\r')
                }
                // trim by nearest tokens
                val std = errorContextStd
                val subTokens = tokens.subList(max(badTokenIdx - std, 0), min(badTokenIdx + std + 1, tokens.size))
                val strStart = subTokens.first().range.start
                val strEnd = subTokens.last().range.endInclusive + 1
                return input.substring(strStart, strEnd)
            }

            val myContext = substr(myTokens, myStr)
            val hisContext = substr(hisTokens, hisStr)
            if (trimErrorOnlyByLine) {
                val hisBadToken = hisTokens[badTokenIdx]
                val myBadToken = myTokens[badTokenIdx]
                return CheckResult(false, "Your line \"$hisContext\"\n" +
                        "doesn't match with \"$myContext\"\n" +
                        "in parts \"${hisBadToken.content}\" and \"${myBadToken.content}\".")
            }
            return CheckResult(false, "Your output ...$hisContext... doesn't match with ...$myContext...")
        }
    
        // check unequal size after other mistakes.
        if (hisTokens.size < myTokens.size) {
            return CheckResult(false, "Your output is too short. " +
                    "It contains only ${hisTokens.size} words, but should contain ${myTokens.size} words.")
        }
        if (hisTokens.size > myTokens.size) {
            return CheckResult(false, "Your output is too long. " +
                    "It contains ${hisTokens.size - myTokens.size} extra words.")
        }
        return CheckResult.correct()
    }
}
