public class CallTerminationEvent extends Event {
    String type;

    public CallTerminationEvent(double eventTime, Call call, String type) {
        super(eventTime, call);
        this.type = type;
    }
}