package dev.mwhitney.update.api;

import dev.mwhitney.util.interfaces.PiPEnum;

/**
 * An enum of artifacts available for download when updating.
 * <p>
 * Each value refers to an expected artifact attached to a release of the
 * application that can replace the current instance. For example, if the
 * application is running as an {@code EXE}, the target artifact for updating
 * would be {@link #EXE}.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public enum Artifact implements PiPEnum<Artifact> {
    /** A Java archive package file. Many Java applications run directly from this file. Extension: {@link .jar} */
    JAR,
    /** A native executable program most commonly used on Windows operating systems. Extension: {@link .exe} */
    EXE;
}