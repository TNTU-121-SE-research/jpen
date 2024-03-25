package jpen.demo.configuration;

public record Size<T>(T width, T height) {
    @Override
    public String toString() {
        return String.format("width=%s height=%s", width, height);
    }
}
