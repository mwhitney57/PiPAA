package dev.mwhitney.util.interfaces;

/**
 * A {@link Runnable} which is meant to be executed more than once. Many
 * implementations of the {@link Runnable} interface only require a single
 * execution. This extension upon that interface makes it clear that this code
 * is intended to be run as many times as necessary.
 * 
 * @author mwhitney57
 */
public interface RecurringRunnable extends Runnable {}
