import embeddings.Embeddable;
import embeddings.GeminiApi;

// NOTE: We can't have this as a part of the embeddings package because it needs to access APIHandler

public class EmbeddingsTask implements Runnable {

    private static final String modelName = "models/gemini-embedding-001";
    private static GeminiApi geminiApi = new GeminiApi();
    private Embeddable embeddableObject = null; 

    EmbeddingsTask(Embeddable embeddable) {
        embeddableObject = embeddable;
        String apiKey = System.getenv().get("GEMINI_API_KEY");
        geminiApi.setApiKey(apiKey);
        geminiApi.setOutputDimension(3072);
    }

    @Override
    public void run() {
        if (embeddableObject == null) return;
        if (!(
            embeddableObject instanceof User ||
            embeddableObject instanceof Club
            // TODO: add events here when implemented
        )) return;
        System.out.println("--------------------");
        String embeddingsText = embeddableObject.generateEmbeddingText();
        String taskType = embeddableObject.getTaskType();
        System.out.println(embeddingsText);
        System.out.println(taskType);
        float[] embeddings = geminiApi.generateEmbedding(embeddingsText, modelName, taskType);
        embeddableObject.setEmbedding(embeddings);
        if (embeddableObject instanceof User) {
            User updateUser = (User) embeddableObject;
            APIHandler.manager.updateUser(updateUser);
        } else if (embeddableObject instanceof Club) {
            Club updateClub = (Club) embeddableObject;
            APIHandler.manager.updateClub(updateClub);
        } // TODO: add events here when implemented
        System.out.println("--------------------");
    }
    
}
