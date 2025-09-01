package co.com.crediya.model.gateways;

public interface PasswordEncoderGateway {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
