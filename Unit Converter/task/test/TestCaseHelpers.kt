import org.hyperskill.hstest.testcase.CheckResult
import org.hyperskill.hstest.testcase.TestCase
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*

/** Output based clue. */
data class OutputClue(
        val output: String,
        /** Do not show correct output and input. */
        var isPrivate: Boolean = false,
        /** Optionally you can add the console input. */
        val input: String? = null
)

/** Default testCase. */
fun <T> testCase(attach: T, input: String) = TestCase<T>().apply {
    setInput(input)
    setAttach(attach)
}

/** [OutputClue] based testCase. Doesn't mean that lines will be compared by letters!!!*/
fun outputCase(input: String, output: String, isPrivate: Boolean = false) =
        testCase(OutputClue(output, isPrivate, input), input)


/** TestCase, based on authors solution output. */
fun authorsCaseFromFun(
        mainMethod: (Scanner, PrintStream) -> Unit,
        input: String,
        isPrivate: Boolean = false
): TestCase<OutputClue> {

    val authorsBytes = ByteArrayOutputStream()
    mainMethod(Scanner(input), PrintStream(authorsBytes, true))
    val authors = authorsBytes.toString()

    return testCase(OutputClue(authors, isPrivate, input), input)
}

fun TestCase<OutputClue>.private() = this.apply { attach.isPrivate = true }

/** Hide error description in private test. */
fun CheckResult.ciphered() =
        if (!isCorrect)
            CheckResult(false)
        else CheckResult(true, feedback)

