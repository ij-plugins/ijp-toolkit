package net.sf.ij_plugins;

public class IJPluginsRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IJPluginsRuntimeException() {
        super();
    }

    public IJPluginsRuntimeException(String message) {
        super(message);
    }

    public IJPluginsRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IJPluginsRuntimeException(Throwable cause) {
        super(cause);
    }

}
