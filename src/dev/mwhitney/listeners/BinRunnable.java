package dev.mwhitney.listeners;

import java.io.IOException;

import dev.mwhitney.exceptions.UnsupportedBinActionException;

/**
 * An interface similarly structured to {@link Runnable} which simply contains a
 * run method. However, unlike {@link Runnable}, this is intended to be used in
 * the context of binaries which often throw certain exceptions.
 * 
 * @author mwhitney57
 */
@FunctionalInterface
public interface BinRunnable extends PiPRunnable {
    /**
     * A method to run code which may possibly throw an
     * {@link InterruptedException}, {@link IOException}, or
     * {@link UnsupportedBinActionException}.
     * <p>
     * To prevent the halting of code execution due to a thrown exception, ensure to
     * wrap the necessary code in an inner-try/catch block.
     * 
     * @throws InterruptedException          if the binary-context code was
     *                                       interrupted.
     * @throws IOException                   if the binary-context code had an
     *                                       input/output error, such as not finding
     *                                       the targeted binary.
     * @throws UnsupportedBinActionException if the binary-context code attempted to
     *                                       perform an unsupported action with a
     *                                       binary.
     */
    @Override
    public void run() throws InterruptedException, IOException, UnsupportedBinActionException;
    @Override
    public default boolean covers(final Exception e) {
        if (e == null) return false;

        return (e instanceof InterruptedException || e instanceof IOException || e instanceof UnsupportedBinActionException);
    }
}
