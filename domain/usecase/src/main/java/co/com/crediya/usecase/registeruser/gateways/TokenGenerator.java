package co.com.crediya.usecase.registeruser.gateways;

public interface TokenGenerator {
    String generateToken(String email, String role);
}
