import java.io.IOException
import java.lang.invoke.MethodHandles
import java.util.*
import kotlin.Exception

fun fileToProperties(fileName: String): Result<Properties> {
    try {
        MethodHandles.lookup().lookupClass().getResourceAsStream(fileName)
            .use { inputStream ->
                val result: Result<Properties> = when (inputStream) {
                    null -> Result.failure("File $fileName not found")
                    else -> Properties().let {
                        it.load(inputStream)
                        Result(it)
                    }
                }
                return result
            }
    } catch (e: IOException) {
        return Result.failure("IOException occurred while reading file $fileName")
    } catch (e: Exception) {
        return Result.failure("Exception occurred: ${e.message} while reading file $fileName")
    }
}

fun propertiesToString(propertyName: String, properties: Result<Properties>): Result<String> {
    val result = properties.flatMap {
        Result.of {
            it.getProperty(propertyName)
        }.mapFailure("Property '$propertyName' not found")
    }
    return result
}

fun <T> propertiesToList(propertyName: String, transformFunction: (String) -> T, properties: Result<Properties>): Result<List<T>> {
    val result: Result<List<T>> = propertiesToString(propertyName, properties).flatMap {
        try {
            Result(it.split(",").map(transformFunction))
        } catch (e: Exception) {
            Result.failure<List<T>>("Invalid value while parsing property $propertyName:$it")
        }
    }
    return result;
}

fun propertiesToIntList(propertyName: String, properties: Result<Properties>): Result<List<Int>> =
        propertiesToList(propertyName, String::toInt, properties)

fun main() {
    // 1
    println("\n1. Trying to load a non-existing property file. Returns Failure object.")
    val failureProperties: Result<Properties> = fileToProperties(fileName = "/config.unknown")
    failureProperties.forEach(onSuccess = { println(it) }, onFailure = { println(it) })

    // 2
    println("\n2. Loading an existing property file. Returns Success object containing property values.")
    val successProperties: Result<Properties> = fileToProperties(fileName = "/config.properties")
    successProperties.forEach(onSuccess = { println(it) }, onFailure = { println(it) })

    // 3
    println("\n3. Try to convert a non-existing property into a integer list. Returns Failure object.")
    val failureNonExistingListProperty: Result<List<Int>> = propertiesToIntList("policiessss", successProperties)
    failureNonExistingListProperty.forEach(onSuccess = { println(it) }, onFailure = { println(it) })

    // 4
    println("\n4. Trying to convert a corrupted property containing an invalid value. Returns a Failure object.")
    val failureCorruptListProperty: Result<List<Int>> = propertiesToIntList("numbers", successProperties)
    failureCorruptListProperty.forEach(onSuccess = { println(it) }, onFailure = { println(it) })

    // 5
    println("\n5. Successfully converting a property into an integer list. Returns a Success object containing the list.")
    val successPolicies: Result<List<Int>> = propertiesToIntList("policies", successProperties)
    successPolicies.forEach(onSuccess = { println(it) }, onFailure = { println(it) })

    // 6
    println("\n6. Successfully converting another property into an integer list.")
    val users: Result<List<Int>> = propertiesToIntList("users", successProperties)
    users.forEach(onSuccess = { println(it) }, onFailure = { println(it) })

    // 7
    println("\n7. Successfully converting a property into a string object.")
    val currentLocation: Result<String> = propertiesToString("location", successProperties)
    currentLocation.forEach(onSuccess = { println(it) }, onFailure = { println(it) })
}
