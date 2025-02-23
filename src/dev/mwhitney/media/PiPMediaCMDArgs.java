package dev.mwhitney.media;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class which represents arguments that go alongside a {@link PiPMediaCMD}.
 * The arguments can be of multiple data types in theory, but forcing this is
 * not recommended. This class makes it easy to distinguish what type(s) of
 * arguments are being passed. It is also much simpler to check if any arguments
 * were passed at all.
 * 
 * @author mwhitney57
 * @since 0.9.4
 */
public class PiPMediaCMDArgs<T> {
    /** A {@link List} of command arguments of a generic type. */
    private final List<T> args;
    
    /**
     * Creates a list of arguments for usage with {@link PiPMediaCMD}.
     * 
     * @param args - one or more command arguments.
     */
    @SafeVarargs
    public PiPMediaCMDArgs(T... args) {
        this.args = (args != null ? Arrays.asList(args) : null);
    }
    
    /**
     * Gets the argument at the specified index.
     * 
     * @param index - an int with the argument index.
     * @return the argument at the specified index.
     */
    public T arg(int index) {
        return (inBounds(index) ? args.get(index) : null);
    }
    
    /**
     * Gets the argument at the specified index as the passed {@link Class} type.
     * However, if the passed type does not match the type of the specified
     * argument, then <code>null</code> will be returned.
     * 
     * @param <C>   - the argument and return type.
     * @param index - an int with the argument index.
     * @param c     - a {@link Class} with the type.
     * @return the argument at the specified index of the passed type.
     */
    @SuppressWarnings("unchecked")
    public <C> C arg(int index, Class<C> c) {
        return (isOfTypeSafe(c) && inBounds(index) ? (C) args.get(index) : null);
    }
    
    /**
     * Checks if the passed argument exists.
     * 
     * @param arg - the argument to check for.
     * @return <code>true</code> if the argument is present; <code>false</code>
     *         otherwise.
     */
    public boolean hasArg(T arg) {
        return (!isEmpty() && args.contains(arg));
    }

    /**
     * Gets all of the arguments as the type of the passed array, but only if the
     * arguments are of that type.
     * <p>
     * This method ultimately calls {@link List#toArray(Object[])}, which is most
     * efficient when passing an empty array. For example, say the arguments are of
     * the {@link Color} type. The optimized method call would be:
     * 
     * <pre>
     * PiPMediaCMDArgs.argsAs(new Color[0]);
     * </pre>
     * 
     * @param <C> - the type of object contained within the passed array.
     * @param arr - the (ideally empty) array to use as reference when retrieving
     *            the typed arguments.
     * @return the arguments of the passed type; <code>null</code> if there are no
     *         arguments, the passed array is <code>null</code>, or the type did not
     *         match.
     * @see {@link #isOfType(Class)} or similar methods to perform type checking on
     *      the arguments.
     */
    public <C> C[] argsAs(C[] arr) {
        if (isEmpty() || arr == null) return null;
        
        if (isOfTypeSafe(arr.getClass().getComponentType()))
            return args.toArray(arr);
        else
            return null;
    }
    
    /**
     * Gets the raw {@link List} of arguments.
     * 
     * @return a {@link List} containing all of the arguments.
     */
    public List<T> raw() {
        return this.args;
    }

    /**
     * Checks if the passed index is in bounds of the internal arguments list. This
     * method will therefore return <code>false</code> if there are no arguments.
     * 
     * @param index - an int with the index.
     * @return <code>true</code> if the index is in bounds; <code>false</code>
     *         otherwise.
     */
    public boolean inBounds(int index) {
        return (index >= 0 && !isEmpty() && index < this.args.size());
    }
    
    /**
     * Checks if the internal arguments {@link List} is valid, meaning not
     * <code>null</code>.
     * 
     * @return <code>true</code> if valid; <code>false</code> otherwise.
     */
    public boolean isValid() {
        return this.args != null;
    }

    /**
     * Checks if the internal arguments {@link List} is empty.
     * 
     * @return <code>true</code> if empty; <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return (!isValid() || this.args.isEmpty());
    }

    /**
     * Checks if the first argument is of the passed type, which should indicate
     * that the entire list of arguments is the same.
     * 
     * @param c - the {@link Class} to compare the argument against.
     * @return <code>true</code> if the arguments match the type of the passed
     *         class; <code>false</code> otherwise.
     * @see {@link #isOfType(Class, int)} to check a specific argument.
     * @see {@link #isOfTypeSafe(Class)} to be extra safe about types.
     */
    public boolean isOfType(Class<?> c) {
        return isOfType(c, 0);
    }
    
    /**
     * Checks if the argument at the passed index is of the passed type, which
     * should indicate that the entire list of arguments is the same.
     * 
     * @param c     - the {@link Class} to compare the argument against.
     * @param index - the index of the argument to check.
     * @return <code>true</code> if the argument matches the type of the passed
     *         class; <code>false</code> otherwise.
     * @see {@link #isOfType(Class)} to simply check the first argument.
     * @see {@link #isOfTypeSafe(Class)} to be extra safe about types.
     */
    public boolean isOfType(Class<?> c, int index) {
        if (isEmpty() || index < 0 || index >= args.size()) return false;
        
        return (c.isInstance(args.get(index)));
    }
    
    /**
     * Checks if <b>ALL</b> of the arguments are of the passed type. Call this
     * method when it is extremely important that each element matches the same
     * passed {@link Class}, leaving no room for type safety issues.
     * 
     * @param c - the {@link Class} to compare the arguments against.
     * @return <code>true</code> if the arguments match the type of the passed
     *         class; <code>false</code> otherwise.
     * @see {@link #isOfType(Class)} to simply check only the first argument.
     * @see {@link #isOfType(Class, int)} to solely check a specific argument.
     */
    public boolean isOfTypeSafe(Class<?> c) {
        if (isEmpty()) return false;
        
        // Determine if each argument is of the same passed Class.
        final AtomicBoolean safe = new AtomicBoolean(Boolean.TRUE);
        args.forEach((a) -> {
            // Unsafe if: still safe and argument is not an instance of the passed class.
            if (safe.get() && !c.isInstance(a)) safe.set(false);
        });
        
        return safe.get();
    }
    
    /**
     * Returns a {@link Object#toString()} representation of the command arguments.
     * The returned String will contain each argument's {@link Object#toString()}
     * representation, so this method is safe to call and should function no matter
     * the argument type.
     * 
     * @return a String representation of these arguments.
     */
    @Override
    public String toString() {
        return (isEmpty() ? "<empty>" : String.join(", ", args.stream().map(Object::toString).toArray(String[]::new)));
    }
}
