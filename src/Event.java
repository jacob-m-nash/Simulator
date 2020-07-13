public class Event {
    private double eventTime;
    private int eventID;
    private static int counter = 0;
    public Call call;

    public Event(double eventTime, Call call){
        this.eventTime = eventTime;
        this.eventID = ++counter;
        this.call = call;
    }

    public double getEventTime(){
        return eventTime;
    }
}
