package io.kickflip.sdk.event;

public class NewTextureEvent {

    private int texture;

    public NewTextureEvent(int texture) {
        this.texture = texture;
    }

    public int getTexture() {
        return texture;
    }
}
