package ubc.cosc322.core;

public enum Color {
    BLACK(1),
    WHITE(2);

    private final int code;

    private Color(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}