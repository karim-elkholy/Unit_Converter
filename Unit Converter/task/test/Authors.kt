import MeasureType.*
import java.io.*
import java.lang.Exception
import java.lang.NumberFormatException
import java.util.*


enum class MeasureType {
    Length, Weight, Temperature;

    fun of(short: String,
           normal: String,
           plural: String,
           multiplier: Double,
           shift: Double = 0.0,
           vararg otherNames: String) = Measure(this, short, normal, plural, multiplier, shift, *otherNames)
}

class Measure(
        val type: MeasureType,
        val short: String,
        val normal: String,
        val plural: String,
        val multiplier: Double,
        val shift: Double = 0.0,
        vararg val otherNames: String
) {
    fun name(amount: Double) = if (amount == 1.0) normal else plural

    val allNames get() = listOf(short, normal, plural, *otherNames)
}

val measures = listOf(
        Length.of("m", "meter", "meters", 1.0),
        Length.of("km", "kilometer", "kilometers", 1000.0), // one km is 1000.0 * 1 m
        Length.of("cm", "centimeter", "centimeters", 0.01),
        Length.of("mm", "millimeter", "millimeters", 0.001),
        Length.of("mi", "mile", "miles", 1609.35),
        Length.of("yd", "yard", "yards", 0.9144),
        Length.of("ft", "foot", "feet", 0.3048),
        Length.of("in", "inch", "inches", 0.0254),

        Weight.of("g", "gram", "grams", 1.0),
        Weight.of("kg", "kilogram", "kilograms", 1000.0),
        Weight.of("mg", "milligram", "milligrams", 0.001),
        Weight.of("lb", "pound", "pounds", 453.592),
        Weight.of("oz", "ounce", "ounces", 28.3495),

        Temperature.of("c", "degree Celsius", "degrees Celsius", 1.0, 0.0, "dc", "celsius"),
        Temperature.of("f", "degree Fahrenheit", "degrees Fahrenheit", 5 / 9.0, -32.0 * 5 / 9.0, "df", "fahrenheit"), // one df is (1 -32)* 5/9 dc
        Temperature.of("k", "kelvin", "kelvins", 1.0, -273.15) // one Kelvin is 1*1.0 - 273.15 dc
)

// we use lowercase names in map, but original names are not.
val namesToMeasures = measures
        .flatMap { m ->
            m.allNames.map { name -> name.toLowerCase() to m }
        }.toMap()


fun solveAuthors(sin: Scanner, sout: PrintStream) {
    while (true) {
        sout.print("Enter what you want to convert (or exit): ")
        val valueStr = sin.next()
        if (valueStr == "exit") {
            break
        }
        val value: Double
        try {
            value = valueStr.toDouble()
        }
        catch(e: NumberFormatException) {
            sout.println("Parse error\n")
            sin.nextLine() // skip the whole line
            continue
        }
        // read measures:
        fun readMeasure(): String {
            var word = sin.next()
            if (word.toLowerCase() == "degree" || word.toLowerCase() == "degrees") {
                word += " " + sin.next()
            }
            return word
        }

        val m1Str = readMeasure()

        sin.next() // unknown word like to or in
        val m2Str = readMeasure()

        val m1 = namesToMeasures[m1Str.toLowerCase()]
        val m2 = namesToMeasures[m2Str.toLowerCase()]

        // error handlers:
        if (m1 == null || m2 == null) {
            sout.println("Conversion from ${m1?.plural ?: "???"} to ${m2?.plural ?: "???"} is impossible")
            continue
        }
        if (m1.type != m2.type) {
            sout.println("Conversion from ${m1.plural} to ${m2.plural} is impossible")
            continue
        }
        if (m1.type != Temperature && value < 0.0) {
            sout.println("${m1.type.name} shouldn't be negative.")
            continue
        }

        // a measure with 1.0 multiplier and 0.0 shift
        val standard = value * m1.multiplier + m1.shift
        val converted = (standard - m2.shift) / m2.multiplier

        sout.println("$value ${m1.name(value)} is $converted ${m2.name(converted)}")
    }
}
