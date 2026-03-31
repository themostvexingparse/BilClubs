package embeddings;

/*
    a useless class that I may remove in the future
*/

public class Embeddings {

    private static GeminiApi geminiApi;
    private static String modelName;

    public static void initialize(GeminiApi providedGeminiApi, String providedModelName) {
        geminiApi = providedGeminiApi;
        modelName = providedModelName;
    }

    /*
        ensure an embeddable has the embeddings calculated
    */
    public static void ensureEmbeddingIsCalculated(Embeddable item) {
        if (item.getEmbedding() != null) {
            return; // if embeddings have been already calculated, skip
        }
        
        String textToEmbed = item.generateEmbeddingText();
        String taskType = item.getTaskType(); 
        
        float[] newEmbedding = geminiApi.generateEmbedding(textToEmbed, modelName, taskType);

        item.setEmbedding(newEmbedding);
        System.out.println("Success (" + taskType + "). Stored new embedding of dimension: " + newEmbedding.length);
    
    }
}