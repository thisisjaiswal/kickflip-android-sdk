package io.kickflip.sdk.event;

public class TextureCreatedEvent {

    private int texture;

    public TextureCreatedEvent(int texture) {
        this.texture = texture;
    }

    public int getTexture() {
        return texture;
    }
}
