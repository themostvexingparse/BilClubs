package embeddings;

/*
    An interface for objects that can be represented with a vector embedding.
*/

import java.io.Serializable;

public interface Embeddable extends Serializable {
    String generateEmbeddingText();
    float[] getEmbedding();
    void setEmbedding(float[] embeddings);
    String getTaskType();
}