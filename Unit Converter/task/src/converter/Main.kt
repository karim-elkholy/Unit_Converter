package converter

import java.lang.Exception

/**
 * Represents a unit of measurement.
 *
 * @param names Different abbreviations to write out the unit.
 * @param conversationRate Standard number to convert to meters(if distance)/grams(if weight). Not applicable to temp.
 * @param category Identifies category that unit belongs to. Either distance or weight.
 */
data class UnitTemplate(val names: List<String>, val conversationRate: Double, val category: String)

val listOfUnits = listOf(
    UnitTemplate(listOf("m", "meter", "meters"), 1.0, "distance"),
    UnitTemplate(listOf("km", "kilometer", "kilometers"), 1000.0, "distance"),
    UnitTemplate(listOf("cm", "centimeter", "centimeters"), 0.01, "distance"),
    UnitTemplate(listOf("mm", "millimeter", "millimeters"), 0.001, "distance"),
    UnitTemplate(listOf("mi", "mile", "miles"), 1609.35, "distance"),
    UnitTemplate(listOf("yd", "yard", "yards"), 0.9144, "distance"),
    UnitTemplate(listOf("ft", "foot", "feet"), 0.3048, "distance"),
    UnitTemplate(listOf("in", "inch", "inches"), 0.0254, "distance"),
    UnitTemplate(listOf("g", "gram", "grams"), 1.0, "weight"),
    UnitTemplate(listOf("kg", "kilogram", "kilograms"), 1000.0, "weight"),
    UnitTemplate(listOf("mg", "milligram", "milligrams"), 0.001, "weight"),
    UnitTemplate(listOf("lb", "pound", "pounds"), 453.592, "weight"),
    UnitTemplate(listOf("oz", "ounce", "ounces"), 28.3495, "weight"),
    UnitTemplate(listOf("degree celsius", "celsius", "degrees celsius", "dc", "c"), 273.15, "temp"),
    UnitTemplate(listOf("degree fahrenheit", "fahrenheit", "degrees fahrenheit",  "df", "f"), 273.15, "temp"),
    UnitTemplate(listOf( "k", "kelvin", "kelvins"), 273.15, "temp"),
)

/**
 * Thrown when a negative weight/distance is given.
 */
class NegativeConversation(message: String) : Exception(message)

/**
 * Thrown when an impossible conversation is attempted.
 */
class ImpossibleConversation(message: String) : Exception(message)

/**
 * Detects a unit of measurement.
 */
fun getUnitOfMeasurement(unit: String): UnitTemplate {
    //Find the unit of measurement
    listOfUnits.forEach { if ( it.names.contains(unit) ) return it }

    //If no unit of measurement was found, throw an error
    throw ImpossibleConversation("Wrong input. Unknown unit $unit")
}

fun convertNumber(inputNumber: Double, inputUnit: UnitTemplate, outputUnit: UnitTemplate ): String {

    //Throw an exception if the units of measurement do not belong to the same category
    if ( inputUnit.category != outputUnit.category ) throw ImpossibleConversation(
        "Conversion from $inputUnit to $outputUnit is impossible"
    )

    //If weight/distance is negative
    if ( inputUnit.category == "weight" && inputNumber < 0 ) throw NegativeConversation("Weight shouldn't be negative")
    if ( inputUnit.category == "distance" && inputNumber < 0 ) throw NegativeConversation("Length shouldn't be negative")

    //If the conversation involves weight/distance
    if ( inputUnit.category == "weight" || inputUnit.category == "distance")
    {
        //Convert to meters/gram
        val intermediateUnit = inputNumber * inputUnit.conversationRate

        //Convert to the requested output unit
        val outputNumber = intermediateUnit/outputUnit.conversationRate

        //Format the unit to their appropriate singular/plural form
        val unitFormattedOriginal = if ( inputNumber == 1.0) inputUnit.names[1] else inputUnit.names[2]
        val unitFormattedConverted = if ( outputNumber == 1.0) outputUnit.names[1] else outputUnit.names[2]

        return "$inputNumber $unitFormattedOriginal is $outputNumber $unitFormattedConverted"

    //If the conversation involves temperature
    } else {

        val celsius = when {
            inputUnit.names.contains("df") -> (inputNumber - 32.0) * 5.0/9.0
            inputUnit.names.contains("k") -> inputNumber - 273.15
            else -> inputNumber
        }

        //println("kelvins:" + kelvins)

        val outputNumber = when {
            outputUnit.names.contains("df") -> (celsius * (9.0/5.0) + 32.0)
            outputUnit.names.contains("k") -> celsius + 273.15
            else -> celsius
        }

        val unitFormattedOriginal = when {
            inputUnit.names.contains("dc") -> (if (inputNumber == 1.0) "degree" else "degrees") + " Celsius"
            inputUnit.names.contains("df") -> (if (inputNumber == 1.0) "degree" else "degrees") + " Fahrenheit"
            else -> if (inputNumber == 1.0) "kelvin" else "kelvins"
        }

        val unitFormattedConverted = when {
            outputUnit.names.contains("dc") -> (if (outputNumber == 1.0) "degree" else "degrees") + " Celsius"
            outputUnit.names.contains("df") -> (if (outputNumber == 1.0) "degree" else "degrees") + " Fahrenheit"
            else -> if (outputNumber == 1.0) "kelvin" else "kelvins"
        }

        return "$inputNumber $unitFormattedOriginal is $outputNumber $unitFormattedConverted"
    }
}

fun handleUser()
{
    while (true)
    {
        print("Enter what you want to convert (or exit): ")
        val input = readln().lowercase()

        //Exit if instructed to do so
        if ( input == "exit") return

        //Get the suffix used
        val regexMatches = "(-?[0-9]+(?:\\.[0-9]+)?) ([\\w ]+) (?:\\w*to|\\w*in) ([\\w ]+)".toRegex()
            .findAll(input)
            .toList()

        //Skip to the next iteration if the input is invalid
        if ( regexMatches.isEmpty() ) { println("Parse error"); continue }

        //Get the input number
        val inputNumber = regexMatches[0].groupValues[1].toDouble()

        //Get the unit to convert from
        val unitToConvertFrom = regexMatches[0].groupValues[2].trim()

        //Get the unit to convert to
        val unitToConvertTo = regexMatches[0].groupValues[3].trim()

        //println("unit to convert from $unitToConvertFrom")
        //println("unit to convert to $unitToConvertTo")


        try {

            //Convert the number to its specified output
            val outputMessage = convertNumber(
                inputNumber,
                getUnitOfMeasurement(unitToConvertFrom),
                getUnitOfMeasurement(unitToConvertTo)
            )

            //Print the message received
            println(outputMessage)

        //Print a message if no length/weight were negative
        } catch ( e: NegativeConversation) {

            println(e.message)

        //Print a message if no suitable unit was found
        } catch ( e: ImpossibleConversation) {

            val fromUnit: String = try {
                getUnitOfMeasurement(unitToConvertFrom).names[2]
            } catch (e: ImpossibleConversation) {
                "???"
            }
            val toUnit: String = try {
                getUnitOfMeasurement(unitToConvertTo).names[2]

            } catch (e: ImpossibleConversation) {
                "???"
            }

            println("Conversion from $fromUnit to $toUnit is impossible")
        }
    }
}
fun main() = handleUser()
