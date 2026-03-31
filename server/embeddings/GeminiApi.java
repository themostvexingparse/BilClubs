package embeddings;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiApi {

    // refer to https://github.com/google-gemini/cookbook/blob/main/quickstarts/rest/Embeddings_REST.ipynb
    // refer to https://ai.google.dev/gemini-api/docs/embeddings#rest_2

    private int dimensions = 768; // Only 3072 is normalized so this will later need normalization
    private static final String API_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/%s:embedContent?key=%s";
    private static final HttpClient client = HttpClient.newBuilder().build();
    private String apiKey = null;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public float[] generateEmbedding(String text, String modelName, String taskType) {
        if (text == null || apiKey == null || modelName == null || taskType == null) {
            return null; // return null if anything goes wrong upto this point
        }

        try {
            String safeText = text.replace("\\", "\\\\")
                                  .replace("\"", "\\\"")
                                  .replace("\n", "\\n");
            // escape some problematic characters so we don't break the request body

            String jsonBody = String.format(
                "{\"content\": { \"parts\": [{ \"text\": \"%s\" }] }, \"taskType\": \"%s\", \"output_dimensionality\": %d}",
                safeText,
                taskType,
                dimensions
            ); // construct the request body

            String url = String.format(API_TEMPLATE, modelName, apiKey); // the final URL to post to
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); // send the request

            if (response.statusCode() == 200) {
                return parseEmbeddingVector(response.body()); // if we get OK code we process the returned embedding
            } else {
                System.err.println("API Error: " + response.statusCode() + " " + response.body());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private float[] parseEmbeddingVector(String json) {
        try {
            String key = "\"values\":"; // locate the point where values array start
            int keyIndex = json.indexOf(key);
            if (keyIndex == -1) return null;

            int startBracket = json.indexOf('[', keyIndex); // the beginning of the array
            if (startBracket == -1) return null;

            int endBracket = json.indexOf(']', startBracket); // the end of the array
            if (endBracket == -1) return null;

            String content = json.substring(startBracket + 1, endBracket).trim(); // get the values

            if (content.isEmpty()) return new float[0];

            String[] tokens = content.split(",");
            float[] embeddings = new float[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                embeddings[i] = Float.parseFloat(tokens[i].trim());
            }

            return embeddings;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setOutputDimension(int dimensions) {
        this.dimensions = dimensions;
    }
}