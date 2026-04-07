package embeddings;

public class CosineSimilarity {

    public static double compute(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }
        if (vectorA.length == 0) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    public static double compute(Embeddable a, Embeddable b) {
        return compute(a.getEmbedding(), b.getEmbedding());
    }
}
