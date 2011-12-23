package me.ahniolator.plugins.burningcreativesuite;

public class CouldNotConnectException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public CouldNotConnectException() {
        super("[BurningCS] Could not connect to server to check for update!");
    }
    
    public CouldNotConnectException(String string) {
        super(string);
    }
}
