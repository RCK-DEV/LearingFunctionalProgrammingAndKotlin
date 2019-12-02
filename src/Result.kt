import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.NullPointerException
import java.lang.RuntimeException

sealed class Result<out A>: Serializable {

    /**
     * Result abstract functions
     */

    abstract fun forEach(onSuccess: (A) -> Unit = {},
                         onFailure: (RuntimeException) -> Unit = {},
                         onEmpty: () -> Unit = {})

    abstract fun <B> map(f: (A) -> B): Result<B>

    abstract fun <B> flatMap(f: (A) -> Result<B>): Result<B>

    abstract fun mapFailure(message: String): Result<A>

    /**
     * Companion object
     */

    companion object {

        /**
         * Constructors
         */

        // Operator overloading for Result constructors without params
        operator fun <A> invoke(): Result<A> =
            Empty

        // Operator overloading for Result constructors with single param
        operator fun <A> invoke(a: A? = null): Result<A> = when (a) {
            null -> Failure(NullPointerException())
            else -> Success(a)
        }

        // Operator overloading for Result constructors with extra message param
        operator fun <A> invoke(a: A? = null, message: String): Result<A> = when (a) {
            null -> Failure(NullPointerException(message))
            else -> Success(a)
        }

        // Operator overloading for Result constructors with extra function param
        operator fun <A> invoke(a: A? = null, f: (A) -> Boolean): Result<A> = when (a) {
            null -> Failure(NullPointerException())
            else -> when {
                f(a) -> Success(a)
                else -> Empty
            }
        }

        // Operator overloading for Result constructors with both message and function param
        operator fun <A> invoke(a: A? = null, message: String, f: (A) -> Boolean): Result<A> = when (a) {
            null -> Failure(NullPointerException())
            else -> when {
                f(a) -> Success(a)
                else -> Failure(
                    IllegalArgumentException(
                        "Argument $a does not match condition: $message"
                    )
                )
            }
        }

        /**
         * Request handler methods used to validate the content of a Result type.
         */

        // Checks if given function param succeeds and returns a Result instance
        fun <A> of(f: () -> A): Result<A> =
            try {
                Result(f())
            } catch (e: RuntimeException) {
                failure(e)
            } catch (e: Exception) {
                failure(e)
            }

        // Based on the predicate result returns a Result type
        fun <T> of(predicate: (T) -> Boolean,
                   value: T,
                   message: String): Result<T> =
            try {
                if (predicate(value))
                    Result(value)
                else
                    failure("Assertion failed for value $value with message: $message")
            } catch (e: Exception) {
                failure(
                    IllegalStateException(
                        "Exception occured while validation $value",
                        e
                    )
                )
            }

        fun <A> failure(message: String): Result<A> =
            Failure(IllegalStateException(message))

        fun <A> failure(exception: RuntimeException): Result<A> =
            Failure(exception)

        fun <A> failure(exception: java.lang.Exception): Result<A> =
            Failure(IllegalStateException(exception))
    }

    /**
     * Internal Result type classes
     */

    internal object Empty: Result<Nothing>() {

        override fun forEach(onSuccess: (Nothing) -> Unit,
                             onFailure: (RuntimeException) -> Unit,
                             onEmpty: () -> Unit) {
            onEmpty()
        }

        override fun <B> map(f: (Nothing) -> B): Result<B> = Empty

        override fun <B> flatMap(f: (Nothing) -> Result<B>): Result<B> = Empty

        override fun mapFailure(message: String): Result<Nothing> = this

    }

    internal class Success<out A>(internal val value: A): Result<A>() {

        override fun forEach(onSuccess: (A) -> Unit,
                             onFailure: (RuntimeException) -> Unit,
                             onEmpty: () -> Unit) {
            onSuccess(value)
        }

        override fun <B> map(f: (A) -> B): Result<B> = try {
            Success(f(value))
        } catch (e: RuntimeException) {
            Failure(e)
        } catch (e: Exception) {
            Failure(RuntimeException(e))
        }

        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> = try {
            f(value)
        } catch (e: RuntimeException) {
            Failure(e)
        } catch (e: Exception) {
            Failure(RuntimeException(e))
        }

        override fun mapFailure(message: String): Result<A> = this

    }

    internal class Failure<out A>(internal val exception: RuntimeException): Result<A>() {

        override fun forEach(onSuccess: (A) -> Unit,
                             onFailure: (RuntimeException) -> Unit,
                             onEmpty: () -> Unit) {
            onFailure(exception)
        }

        override fun <B> map(f: (A) -> B): Result<B> = Failure(exception)

        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> = Failure(exception)

        override fun mapFailure(message: String): Result<A> = Failure(RuntimeException(message))

    }
}