import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StreamReader {
    public static String readStream(InputStream stream) throws IOException {
        // previous method with readLine was vulnerable as it would read
        // arbitrarily large request bodies filled with new line characters
        byte[] buffer = stream.readNBytes((int) ServerConfig.MAX_REQUEST_BYTES + 1);
        if (buffer.length > ServerConfig.MAX_REQUEST_BYTES) {
            throw new IOException("Request body exceeds maximum allowed size.");
        }
        return new String(buffer, StandardCharsets.UTF_8);
    }
}