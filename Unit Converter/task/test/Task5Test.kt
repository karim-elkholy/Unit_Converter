import org.hyperskill.hstest.stage.StageTest
import org.hyperskill.hstest.testcase.CheckResult
import org.hyperskill.hstest.testcase.TestCase


/** TestCase, based on authors solution output. */
fun authorsCase(input: String, isPrivate: Boolean = false) =
        authorsCaseFromFun(::solveAuthors, input, isPrivate)

class Task5Test : StageTest<OutputClue>() {

    override fun generate(): List<TestCase<OutputClue>> {
        val l1 = listOf(
                // tests from the example
                authorsCase("1 degree Celsius to kelvins\n" +
                        "-272.15 dc to K\n" +
                        "1 kn to feet\n" +
                        "1 km to feet\n" +
                        "3 pount to ounces\n" +
                        "3 pound to ounces\n" +
                        "3 kelvins to grams\n" +
                        "3 grams to meters\n" +
                        "exit\n"),
                authorsCase("1 F in K\n" +
                        "1 K in F\n" +
                        "1 C in K\n" +
                        "1 K in C\n" +
                        "1 F in C\n" +
                        "1 C in F\n" +
                        "exit\n"),

                // my tests.

                // errors
                // unknown
                authorsCase("100 AAA convertTo BBB\nexit"),
                authorsCase("-100.0 X to Y\nexit", true),
                authorsCase("-100.0 kelvin to Y\nexit"),
                authorsCase("-100.0 X to kelvin\nexit", true),
                authorsCase("100.0 X to Pound\nexit", true),
                authorsCase("Random string without numbers\nexit"),
                // negative unsupported
                authorsCase("-100.1 cm to M\nexit"),
                authorsCase("-10.10 Kilometer to mm\nexit", true),
                authorsCase("-100.1 Gram to kg\nexit"),
                authorsCase("-100.1 mg to g\nexit", true),
                // incomparable
                authorsCase("1 Pound to degrees Celsius\nexit"),
                authorsCase("1 cm to kelvin\nexit", true),
                authorsCase("1 k to mm\nexit", true),
                authorsCase("1 g to df\nexit", true),
                authorsCase("1 degrees celsius to Grams\nexit"),
                authorsCase("1 Grams to degree Fahrenheit\nexit", true),
                //km
                authorsCase("5 km to km\nexit"),
                authorsCase("1 foot to kilometers\nexit"),
                authorsCase("4023 km to miles\nexit"),
                //mi
                authorsCase("5 mi to km\nexit"),
                authorsCase("1 mile to mm\nexit"),
                authorsCase("4 MILES to feet\nexit"),
                // two word names
                authorsCase("1 degree XX to degrees YY\nexit"),
                authorsCase("1 degrees XX to degree YY\nexit", true),
                authorsCase("12 degree Celsius to degrees nnn\nexit", true),
                authorsCase("15 DEGREES CELSIUS in DEGREE FAHRENHEIT\nexit"),
                authorsCase("12 DEGREE xxx to degrees Fahrenheit\nexit"),
                authorsCase("15 DEGREE CELsius in Degrees CELsius\nexit", true),
                authorsCase("15 DEGREE CELsius in mm\nexit", true),
                authorsCase("17 celsius in fahrenheit\nexit"), // another short names.

                authorsCase("79 celsius in degrees celsius\nexit", true),
        )

        val temps = listOf(
                "kelvins", "kelvin", "k",
                "degrees celsius", "degree celsius", "celsius", "dc", "c",
                "degrees fahrenheit", "degree fahrenheit", "fahrenheit", "df", "f",
                "XX", "yard" // plus some noise.
        )
        val lastTest = temps
                // all combinations
                .flatMap { t1 -> temps.map { t2 -> t1 to t2 } }
                .mapIndexed { i, (t1, t2) ->
                    "${i + 1} $t1 in $t2"
                }
                .joinToString("\n", postfix = "\nexit")
                .let { authorsCase(it, true) }

        return l1 + lastTest
    }

    override fun check(reply: String, clue: OutputClue): CheckResult {
        // compare the clue output and reply with our custom comparer.
//        println("Input: ${clue.input}")
        val checkResult = WordComparer(
                clue.output,
                reply,
                moreIgnoredSymbolsRegex = """\."""
        ).compare()

        if (clue.isPrivate)
            return checkResult.ciphered()
        return checkResult
    }
}
