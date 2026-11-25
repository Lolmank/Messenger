package messenger.exception;

public class InvalidRoomNameException extends RuntimeException {
    public InvalidRoomNameException(String message) {
        super(message);
    }
}
