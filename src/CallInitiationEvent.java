import java.util.Random;

public class CallInitiationEvent extends Event {


    public CallInitiationEvent(double eventTime, Call call) {
        super(eventTime, call);
    }

    public boolean handoverEventRequired() {

        double distanceLeftInBaseStation = 2 - call.getPosition();
        double timeLeftInBaseStation = distanceLeftInBaseStation / call.getVelocity();
        return call.getTimeLeftInCall() > timeLeftInBaseStation;
    }
}
