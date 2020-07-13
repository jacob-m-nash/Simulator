public class Call {
    private double callStartTime;
    private double timeLeftInCall;
    private double velocity;//km/h
    private double position;//km
    private double timeLeftInBaseStation;
    BaseStation connectedBaseStation;
    private static int counter = 0;
    private int id;

    public Call(double callStartTime, double timeLeftInCall, BaseStation baseStation, double velocity, double position ){
        this.id = ++counter;
        this.callStartTime = callStartTime;
        this.timeLeftInCall = timeLeftInCall;
        this.connectedBaseStation = baseStation;
        this.velocity = velocity;
        this.position = position;
        if(velocity < 0 ){
            timeLeftInBaseStation = - position / ( velocity/3600);
        }
        else{
            this.timeLeftInBaseStation = (2 - position) / (velocity / 3600);}

    }

    public double getCallStartTime(){
        return callStartTime;
    }

    public double getTimeLeftInCall() {
        return timeLeftInCall;
    }

    public void setTimeLeftInCall(double timeLeftInCall) {
        this.timeLeftInCall = timeLeftInCall;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public BaseStation getConnectedBaseStation() {
        return connectedBaseStation;
    }

    public void setConnectedBaseStation(BaseStation connectedBaseStation) {
        this.connectedBaseStation = connectedBaseStation;
    }
    public double getTimeLeftInBaseStation() {
        return timeLeftInBaseStation;
    }

    public void updateTimeLeftInBaseStation(){
        if (velocity < 0){
            this.timeLeftInBaseStation = position / ( -velocity/3600);
        }
        else{
            this.timeLeftInBaseStation = (2 - position) / (velocity / 3600);}
    }

    public boolean handoverEventRequired(){
        if(velocity < 0 && connectedBaseStation.getId() == 0){
            return false;
        }
        if(connectedBaseStation.getId()== 19){
            return false;
        }
        return timeLeftInCall > timeLeftInBaseStation;
    }

    public int getId() {
        return id;
    }
}