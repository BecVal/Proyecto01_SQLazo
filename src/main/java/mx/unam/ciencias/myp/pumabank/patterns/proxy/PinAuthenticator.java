package mx.unam.ciencias.myp.pumabank.patterns.proxy;

public class PinAuthenticator {
    private String storedPin;

    public PinAuthenticator(String storedPin) {
        this.storedPin = storedPin;
    }

    public boolean validate(String inputPin) {
        return storedPin != null && storedPin.equals(inputPin);
    }
}
