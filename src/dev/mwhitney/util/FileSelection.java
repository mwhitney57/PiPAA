package dev.mwhitney.util;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A {@link Transferable} with similar implementation to that of Java's
 * {@link StringSelection}, but for a {@link File}.
 * <p>
 * This transferable only supports {@link DataFlavor#javaFileListFlavor}.
 * 
 * @author mwhitney57
 * @since 0.9.5
 * @see {@link DataFlavor#javaFileListFlavor}
 */
public class FileSelection implements Transferable, ClipboardOwner {
    /** A {@link List} of {@link File} transfer data. */
    private List<File> data;

    /**
     * Creates a {@link Transferable} capable of transferring the passed
     * {@link File} data.
     * 
     * @param data - one or more {@link File} objects to transfer.
     */
    public FileSelection(File... data) {
        this(List.of(data));
    }
    
    /**
     * Creates a {@link Transferable} capable of transferring the passed
     * {@link File} data.
     * 
     * @param data - a {@link List} of one or more {@link File} objects.
     */
    public FileSelection(List<File> data) {
        this.data = data;
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { DataFlavor.javaFileListFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.javaFileListFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
        return (Object) this.data;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {}
}
