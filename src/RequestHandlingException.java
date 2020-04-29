public class RequestHandlingException extends Exception {
    private Status status;
    private String msg;

    public RequestHandlingException(Status status) {
        super("");
        this.status = status;
    }

    public RequestHandlingException(Status status, String msg) {
        super(msg);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return msg;
    }
}
