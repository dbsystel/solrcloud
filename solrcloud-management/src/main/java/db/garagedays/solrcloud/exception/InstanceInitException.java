package db.garagedays.solrcloud.exception;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 10.01.17.
 */
public class InstanceInitException extends Exception {
    public InstanceInitException(String message) {
        super(message);
    }

    public InstanceInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
