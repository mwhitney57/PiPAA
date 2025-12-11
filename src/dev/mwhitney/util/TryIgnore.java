package dev.mwhitney.util;

import dev.mwhitney.util.interfaces.tfunctions.TFunction;
import dev.mwhitney.util.interfaces.tfunctions.TFunctionI;
import dev.mwhitney.util.interfaces.tfunctions.TFunctionII;
import dev.mwhitney.util.interfaces.tfunctions.TFunctionIII;
import dev.mwhitney.util.interfaces.tfunctions.TFunctionIV;
import dev.mwhitney.util.interfaces.tfunctions.TFunctionV;
import dev.mwhitney.util.interfaces.tfunctions.TRFunction;
import dev.mwhitney.util.interfaces.tfunctions.TRFunctionI;
import dev.mwhitney.util.interfaces.tfunctions.TRFunctionII;
import dev.mwhitney.util.interfaces.tfunctions.TRFunctionIII;
import dev.mwhitney.util.interfaces.tfunctions.TRFunctionIV;
import dev.mwhitney.util.interfaces.tfunctions.TRFunctionV;

/**
 * A utility class that provides methods which apply a given function while
 * ignoring any thrown exceptions. This reduces boilerplate and improves
 * readability.
 * <p>
 * However, usage of these methods must be done sparingly. <b>This is NOT a fix
 * for anything other than code readability.</b> It does not improve
 * performance. It does not prevent exceptions. It's a shorthand way of ignoring
 * them and satisfying the compiler, <b>which is often bad</b>.
 * <p>
 * For the rarer cases where ignoring the exception is the best course of
 * action, this class comes in handy.
 * <p>
 * There are {@code run} and {@code get} methods available, each having
 * alternative {@code ...With} versions for providing parameters in the passed
 * function. The idea, of course, being that the function is being run
 * <b>with</b> those values.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public final class TryIgnore {
    // Specify private constructor to disallow object creation.
    private TryIgnore() {}
    
    /**
     * Runs the passed function which may throw an exception, but ignores any actual
     * throws. Returns when the passed function is finished executing or an
     * exception is thrown. Any thrown exception will be caught and ignored within
     * this method before returning.
     * 
     * @param f - the function to run which may throw an exception.
     */
    public static void run(TFunction<Exception> f) {
        try {
            f.apply();
        } catch (Exception e) {}
    }
    /**
     * Runs the passed function which may throw an exception, but ignores any actual
     * throws. Returns when the passed function is finished executing or an
     * exception is thrown. Any thrown exception will be caught and ignored within
     * this method before returning.
     * <p>
     * The parameter will be provided to the function when it is called to execute.
     * 
     * @param <P> the type of parameter to pass to the function.
     * @param f   - the function to run which may throw an exception.
     * @param p   - the parameter to pass to the provided function.
     */
    public static <P> void runWith(TFunctionI<P, Exception> f, P p) {
        try {
            f.apply(p);
        } catch (Exception e) {}
    }
    /**
     * Runs the passed function which may throw an exception, but ignores any actual
     * throws. Returns when the passed function is finished executing or an
     * exception is thrown. Any thrown exception will be caught and ignored within
     * this method before returning.
     * <p>
     * The parameters will be provided to the function when it is called to execute.
     * 
     * @param <P1> the type of the first parameter to pass to the function.
     * @param <P2> the type of the second parameter to pass to the function.
     * @param f    - the function to run which may throw an exception.
     * @param p1   - the first parameter to pass to the provided function.
     * @param p2   - the second parameter to pass to the provided function.
     */
    public static <P1, P2> void runWith(TFunctionII<P1, P2, Exception> f, P1 p1, P2 p2) {
        try {
            f.apply(p1, p2);
        } catch (Exception e) {}
    }
    /**
     * Runs the passed function which may throw an exception, but ignores any actual
     * throws. Returns when the passed function is finished executing or an
     * exception is thrown. Any thrown exception will be caught and ignored within
     * this method before returning.
     * <p>
     * The parameters will be provided to the function when it is called to execute.
     * 
     * @param <P1> the type of the first parameter to pass to the function.
     * @param <P2> the type of the second parameter to pass to the function.
     * @param <P3> the type of the third parameter to pass to the function.
     * @param f    - the function to run which may throw an exception.
     * @param p1   - the first parameter to pass to the provided function.
     * @param p2   - the second parameter to pass to the provided function.
     * @param p3   - the third parameter to pass to the provided function.
     */
    public static <P1, P2, P3> void runWith(TFunctionIII<P1, P2, P3, Exception> f, P1 p1, P2 p2, P3 p3) {
        try {
            f.apply(p1, p2, p3);
        } catch (Exception e) {}
    }
    /**
     * Runs the passed function which may throw an exception, but ignores any actual
     * throws. Returns when the passed function is finished executing or an
     * exception is thrown. Any thrown exception will be caught and ignored within
     * this method before returning.
     * <p>
     * The parameters will be provided to the function when it is called to execute.
     * 
     * @param <P1> the type of the first parameter to pass to the function.
     * @param <P2> the type of the second parameter to pass to the function.
     * @param <P3> the type of the third parameter to pass to the function.
     * @param <P4> the type of the fourth parameter to pass to the function.
     * @param f    - the function to run which may throw an exception.
     * @param p1   - the first parameter to pass to the provided function.
     * @param p2   - the second parameter to pass to the provided function.
     * @param p3   - the third parameter to pass to the provided function.
     * @param p4   - the fourth parameter to pass to the provided function.
     */
    public static <P1, P2, P3, P4> void runWith(TFunctionIV<P1, P2, P3, P4, Exception> f, P1 p1, P2 p2, P3 p3, P4 p4) {
        try {
            f.apply(p1, p2, p3, p4);
        } catch (Exception e) {}
    }
    /**
     * Runs the passed function which may throw an exception, but ignores any actual
     * throws. Returns when the passed function is finished executing or an
     * exception is thrown. Any thrown exception will be caught and ignored within
     * this method before returning.
     * <p>
     * The parameters will be provided to the function when it is called to execute.
     * 
     * @param <P1> the type of the first parameter to pass to the function.
     * @param <P2> the type of the second parameter to pass to the function.
     * @param <P3> the type of the third parameter to pass to the function.
     * @param <P4> the type of the fourth parameter to pass to the function.
     * @param <P5> the type of the fifth parameter to pass to the function.
     * @param f    - the function to run which may throw an exception.
     * @param p1   - the first parameter to pass to the provided function.
     * @param p2   - the second parameter to pass to the provided function.
     * @param p3   - the third parameter to pass to the provided function.
     * @param p4   - the fourth parameter to pass to the provided function.
     * @param p5   - the fifth parameter to pass to the provided function.
     */
    public static <P1, P2, P3, P4, P5> void runWith(TFunctionV<P1, P2, P3, P4, P5, Exception> f, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) {
        try {
            f.apply(p1, p2, p3, p4, p5);
        } catch (Exception e) {}
    }
    /**
     * Gets a value from passed the function which may throw an exception, but
     * ignores any actual throws. Returns when the passed function returns a value
     * or throws an exception. Any thrown exception will be caught and ignored
     * within this method before returning.
     * <p>
     * A value of {@code null} will always be returned if an exception is thrown and
     * caught while executing the provided function.
     * 
     * @param <R> the type of value returned by the function.
     * @param f   - the function to run which may throw an exception.
     */
    public static <R> R get(TRFunction<R, Exception> f) {
        try {
            return f.apply();
        } catch (Exception e) {}
        return null;
    }
    /**
     * Gets a value from passed the function which may throw an exception, but
     * ignores any actual throws. Returns when the passed function returns a value
     * or throws an exception. Any thrown exception will be caught and ignored
     * within this method before returning.
     * <p>
     * The parameter will be provided to the function when it is called to execute.
     * <p>
     * A value of {@code null} will always be returned if an exception is thrown and
     * caught while executing the provided function.
     * 
     * @param <R> the type of value returned by the function.
     * @param <P> the type of the parameter to pass to the function.
     * @param f   - the function to run which may throw an exception.
     * @param p   - the parameter to pass to the provided function.
     */
    public static <R, P> R getWith(TRFunctionI<P, R, Exception> f, P p) {
        try {
            return f.apply(p);
        } catch (Exception e) {}
        return null;
    }
    /**
     * Gets a value from passed the function which may throw an exception, but
     * ignores any actual throws. Returns when the passed function returns a value
     * or throws an exception. Any thrown exception will be caught and ignored
     * within this method before returning.
     * <p>
     * The parameters will be provided to the function when it is called to execute.
     * <p>
     * A value of {@code null} will always be returned if an exception is thrown and
     * caught while executing the provided function.
     * 
     * @param <R>  the type of value returned by the function.
     * @param <P1> the type of the first parameter to pass to the function.
     * @param <P2> the type of the second parameter to pass to the function.
     * @param f    - the function to run which may throw an exception.
     * @param p1   - the first parameter to pass to the provided function.
     * @param p2   - the second parameter to pass to the provided function.
     */
    public static <R, P1, P2> R getWith(TRFunctionII<P1, P2, R, Exception> f, P1 p1, P2 p2) {
        try {
            return f.apply(p1, p2);
        } catch (Exception e) {}
        return null;
    }
    /**
     * Gets a value from passed the function which may throw an exception, but
     * ignores any actual throws. Returns when the passed function returns a value
     * or throws an exception. Any thrown exception will be caught and ignored
     * within this method before returning.
     * <p>
     * The parameters will be provided to the function when it is called to execute.
     * <p>
     * A value of {@code null} will always be returned if an exception is thrown and
     * caught while executing the provided function.
     * 
     * @param <R>  the type of value returned by the function.
     * @param <P1> the type of the first parameter to pass to the function.
     * @param <P2> the type of the second parameter to pass to the function.
     * @param <P3> the type of the third parameter to pass to the function.
     * @param f    - the function to run which may throw an exception.
     * @param p1   - the first parameter to pass to the provided function.
     * @param p2   - the second parameter to pass to the provided function.
     * @param p3   - the third parameter to pass to the provided function.
     */
    public static <R, P1, P2, P3> R getWith(TRFunctionIII<P1, P2, P3, R, Exception> f, P1 p1, P2 p2, P3 p3) {
        try {
            return f.apply(p1, p2, p3);
        } catch (Exception e) {}
        return null;
    }
    /**
     * Gets a value from passed the function which may throw an exception, but
     * ignores any actual throws. Returns when the passed function returns a value
     * or throws an exception. Any thrown exception will be caught and ignored
     * within this method before returning.
     * <p>
     * The parameters will be provided to the function when it is called to execute.
     * <p>
     * A value of {@code null} will always be returned if an exception is thrown and
     * caught while executing the provided function.
     * 
     * @param <R>  the type of value returned by the function.
     * @param <P1> the type of the first parameter to pass to the function.
     * @param <P2> the type of the second parameter to pass to the function.
     * @param <P3> the type of the third parameter to pass to the function.
     * @param <P4> the type of the fourth parameter to pass to the function.
     * @param f    - the function to run which may throw an exception.
     * @param p1   - the first parameter to pass to the provided function.
     * @param p2   - the second parameter to pass to the provided function.
     * @param p3   - the third parameter to pass to the provided function.
     * @param p4   - the fourth parameter to pass to the provided function.
     */
    public static <R, P1, P2, P3, P4> R getWith(TRFunctionIV<P1, P2, P3, P4, R, Exception> f, P1 p1, P2 p2, P3 p3, P4 p4) {
        try {
            return f.apply(p1, p2, p3, p4);
        } catch (Exception e) {}
        return null;
    }
    /**
     * Gets a value from passed the function which may throw an exception, but
     * ignores any actual throws. Returns when the passed function returns a value
     * or throws an exception. Any thrown exception will be caught and ignored
     * within this method before returning.
     * <p>
     * The parameters will be provided to the function when it is called to execute.
     * <p>
     * A value of {@code null} will always be returned if an exception is thrown and
     * caught while executing the provided function.
     * 
     * @param <R>  the type of value returned by the function.
     * @param <P1> the type of the first parameter to pass to the function.
     * @param <P2> the type of the second parameter to pass to the function.
     * @param <P3> the type of the third parameter to pass to the function.
     * @param <P4> the type of the fourth parameter to pass to the function.
     * @param <P5> the type of the fifth parameter to pass to the function.
     * @param f    - the function to run which may throw an exception.
     * @param p1   - the first parameter to pass to the provided function.
     * @param p2   - the second parameter to pass to the provided function.
     * @param p3   - the third parameter to pass to the provided function.
     * @param p4   - the fourth parameter to pass to the provided function.
     * @param p5   - the fifth parameter to pass to the provided function.
     */
    public static <R, P1, P2, P3, P4, P5> R getWith(TRFunctionV<P1, P2, P3, P4, P5, R, Exception> f, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) {
        try {
            return f.apply(p1, p2, p3, p4, p5);
        } catch (Exception e) {}
        return null;
    }
}
