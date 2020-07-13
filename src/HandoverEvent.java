public class HandoverEvent extends  Event {
    Call currentCall;
    BaseStation currentBaseStation;
    BaseStation nextBaseStation;

    public HandoverEvent(double eventTime, Call currentCall) {
        super(eventTime, currentCall);
        this.currentCall = currentCall;
        this.currentBaseStation = currentCall.connectedBaseStation;
        if (call.getVelocity() < 0) {
            nextBaseStation = Simulator.stations[currentBaseStation.getId() - 1];
        } else {
            nextBaseStation = Simulator.stations[currentBaseStation.getId() + 1];
        }
    }
}

