package dev.mwhitney.main;

import uk.co.caprica.vlcj.binding.lib.LibC;
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.provider.DirectoryProviderDiscoveryStrategy;

/**
 * A custom {@link NativeDiscoveryStrategy} for finding LibVlc in the application folder.
 * 
 * @author mwhitney57
 */
public class BinDiscoveryStrategy extends DirectoryProviderDiscoveryStrategy {
    
    private static final String[] FILENAME_PATTERNS = new String[] {
        "libvlc\\.dll",
        "libvlccore\\.dll"
    };

    private static final String[] PLUGIN_PATH_FORMATS = new String[] {
        "%s\\PiPAA\\bin",
    };
    public BinDiscoveryStrategy() {
        super(FILENAME_PATTERNS, PLUGIN_PATH_FORMATS);
        setPluginPath("APPDATA");
    }
    
    @Override
    public boolean supported() {
        return RuntimeUtil.isWindows();
    }

    @Override
    protected boolean setPluginPath(String pluginPath) {
        return LibC.INSTANCE._putenv(String.format("%s=%s", PLUGIN_ENV_NAME, pluginPath)) == 0;
    }
}
