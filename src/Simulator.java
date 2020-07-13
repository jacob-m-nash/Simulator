import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Simulator {

    private PriorityQueue<Event> FEL;
    private double simulationClock;
    private int attemptedCalls,blockedCallCount, droppedCallCount = 0;
    private NumberGenerator numberGenerator = new NumberGenerator();


    public static BaseStation[] stations;

    private void  init(int baseStationMode){
        simulationClock = 0;
        attemptedCalls = 0;
        blockedCallCount = 0;
        droppedCallCount = 0;
        stations = new BaseStation[20];
        FEL = new PriorityQueue<>(eventComparator);
        for(int i = 0; i < 20; i ++){
            stations[i] = new BaseStation(i,baseStationMode);
        }
    }

    private void warmUp(int numberOfCalls){
        Call prevCall = new Call(simulationClock + numberGenerator.carInterArrival(),numberGenerator.callDuration(),stations[numberGenerator.baseStation() - 1],numberGenerator.velocity(),numberGenerator.positionInBaseStation());
        Event prevEvent = new CallInitiationEvent(prevCall.getCallStartTime(), prevCall);
        FEL.add(prevEvent);
        for (int i = 0; i < numberOfCalls - 1 ; i++){
            Call nextCall = new Call(prevCall.getCallStartTime() + numberGenerator.carInterArrival(),numberGenerator.callDuration(),stations[numberGenerator.baseStation() - 1],numberGenerator.velocity(),numberGenerator.positionInBaseStation());
            Event nextEvent = new CallInitiationEvent(nextCall.getCallStartTime(),nextCall);
            FEL.add(nextEvent);
            prevCall = nextCall;
        }
        while(!FEL.isEmpty()){
            Event event = FEL.poll();
            simulationClock = event.getEventTime();
            handleEvent(event);
            if (event instanceof CallInitiationEvent){
                if((event.call.getId() % (numberOfCalls) == 0)){
                    return;
                }
            }
        }
    }

    private void warmDown() {
        while (!FEL.isEmpty()) {
            Event event = FEL.poll();
            simulationClock = event.getEventTime();
            handleEvent(event);
        }
    }

    private void stochasticMode(int numberOfCalls, int warmUpPeriod){
        Call prevCall = new Call(simulationClock + numberGenerator.carInterArrival(),numberGenerator.callDuration(),stations[numberGenerator.baseStation() - 1],numberGenerator.velocity(),numberGenerator.positionInBaseStation());
        Event prevEvent = new CallInitiationEvent(prevCall.getCallStartTime(), prevCall);
        FEL.add(prevEvent);
        for (int i = 0; i < numberOfCalls - 1 ; i++){
            Call nextCall = new Call(prevCall.getCallStartTime() + numberGenerator.carInterArrival(),numberGenerator.callDuration(),stations[numberGenerator.baseStation() - 1],numberGenerator.velocity(),numberGenerator.positionInBaseStation());
            Event nextEvent = new CallInitiationEvent(nextCall.getCallStartTime(),nextCall);
            FEL.add(nextEvent);
            prevCall = nextCall;
        }
        while(!FEL.isEmpty()){
            Event event = FEL.poll();
            simulationClock = event.getEventTime();
            handleEvent(event);
            if (event instanceof CallInitiationEvent){
                if((event.call.getId() % (numberOfCalls + warmUpPeriod) == 0)){
                    return;
                }
            }
        }
    }

    private void handleEvent(Event event){
        if (event instanceof CallInitiationEvent){
            attemptedCalls++;
            if(stations[event.call.getConnectedBaseStation().getId()].hasFreeChannelForInitiation()){
                stations[event.call.getConnectedBaseStation().getId()].addCall();
                if (event.call.handoverEventRequired()){
                    HandoverEvent handoverEvent = new HandoverEvent(simulationClock + event.call.getTimeLeftInBaseStation(), event.call);
                    FEL.add(handoverEvent);
                }
                else{
                    CallTerminationEvent callTerminationEvent;
                    if(event.call.getVelocity() < 0 && event.call.getConnectedBaseStation().getId() == 0){
                        callTerminationEvent = new CallTerminationEvent(simulationClock + event.call.getTimeLeftInBaseStation(),event.call,"end");
                    }
                    else if(event.call.getConnectedBaseStation().getId() == 19){
                        callTerminationEvent = new CallTerminationEvent(simulationClock + event.call.getTimeLeftInBaseStation(),event.call,"end");
                    }
                    else { callTerminationEvent = new CallTerminationEvent(simulationClock + event.call.getTimeLeftInCall(),event.call,"end");}
                    FEL.add(callTerminationEvent);
                }
            }
            else {
                CallTerminationEvent droppedEvent = new CallTerminationEvent(simulationClock , event.call, "Blocked");
                FEL.add(droppedEvent);
            }
        }

        if (event instanceof CallTerminationEvent) {
            switch (((CallTerminationEvent) event).type) {
                case "Blocked":
                    blockedCallCount++;
                    break;
                case "Dropped":
                    droppedCallCount++;
                    break;
            }
            event.call.connectedBaseStation.removeCall();
        }

        if (event instanceof HandoverEvent){

            if(((HandoverEvent) event).nextBaseStation.hasFreeChannelForHandover()){
                ((HandoverEvent) event).nextBaseStation.addCall();
                event.call.setTimeLeftInCall(event.call.getTimeLeftInCall() - event.call.getTimeLeftInBaseStation());
                if(event.call.getVelocity() < 0){
                    event.call.setPosition(2);
                }
                else{
                    event.call.setPosition(0.0);
                }
                event.call.updateTimeLeftInBaseStation();
                event.call.connectedBaseStation.removeCall();
                event.call.setConnectedBaseStation(((HandoverEvent) event).nextBaseStation);
                if (event.call.handoverEventRequired()){
                    HandoverEvent handoverEvent = new HandoverEvent(simulationClock + event.call.getTimeLeftInBaseStation(), event.call);
                    FEL.add(handoverEvent);
                }
                else{
                    CallTerminationEvent callTerminationEvent;
                    if(event.call.getVelocity() < 0 && event.call.getConnectedBaseStation().getId() == 0){
                        callTerminationEvent = new CallTerminationEvent(simulationClock + event.call.getTimeLeftInBaseStation(),event.call,"end");
                    }
                    else if(event.call.getConnectedBaseStation().getId() == 19){
                        callTerminationEvent = new CallTerminationEvent(simulationClock + event.call.getTimeLeftInBaseStation(),event.call,"end");
                    }
                    else { callTerminationEvent = new CallTerminationEvent(simulationClock + event.call.getTimeLeftInCall(),event.call,"end");}
                    FEL.add(callTerminationEvent);
                }
            }
            else {
                CallTerminationEvent droppedEvent = new CallTerminationEvent(simulationClock , event.call, "Dropped");
                FEL.add(droppedEvent);
            }
        }
    }


    public void readInData(String filePath){
        String line;
        try{
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            br.readLine();
            while((line = br.readLine()) != null){
                System.out.println(line);
                String[] data = line.split(",");
                Call call= new Call(Double.parseDouble(data[1]),Double.parseDouble(data[3]),stations[Integer.parseInt(data[2]) - 1],Double.parseDouble(data[4]),numberGenerator.positionInBaseStation());
                Event event = new CallInitiationEvent(call.getCallStartTime(), call);
                FEL.add(event);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        Simulator sim = new Simulator();
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter Base Station Mode:");
        int mode = input.nextInt();
        System.out.println("Please enter number of simulations: ");
        int simNumber = input.nextInt();
        System.out.println("Please enter number of calls: ");
        int numberOfCalls = input.nextInt();
        FileWriter csvWriter = new FileWriter("SimOutputMode" + String.valueOf(mode) + ".csv");
        csvWriter.append("Simulation Number" + "," + "Number Of Calls" + "," + "Number Of Dropped Calls" + "," + "Number Of Blocked Calls");
        csvWriter.append("\n");
        for (int i = 0; i < simNumber; i++){
            System.out.println("Simulation #: "+ (i + 1));
            sim.init(mode);
            sim.warmUp(500);
            sim.attemptedCalls = 0;
            sim.stochasticMode(numberOfCalls ,500);
            System.out.println("Number of calls: "+ sim.attemptedCalls);
            System.out.println("Number of dropped calls: " + sim.droppedCallCount);
            System.out.println("Number of blocked calls: " + sim.blockedCallCount);
            csvWriter.append(String.valueOf(i + 1)).append(",").append(String.valueOf(sim.attemptedCalls)).append(",").append(String.valueOf(sim.droppedCallCount)).append(",").append(String.valueOf(sim.blockedCallCount));
            csvWriter.append("\n");
            int droppedCallsWarmDownOffset = sim.droppedCallCount;
            int blockedCallsWarmDownOffset = sim.blockedCallCount;
            sim.warmDown();
            System.out.println("Number of dropped calls during warm down: " + (sim.droppedCallCount - droppedCallsWarmDownOffset));
            System.out.println("Number of blocked calls during warm down: " + (sim.blockedCallCount - blockedCallsWarmDownOffset));
        }
        csvWriter.flush();
        csvWriter.close();
    }

    private static Comparator<Event> eventComparator = new Comparator<Event>(){
        @Override
        public int compare(Event e1, Event e2) {
            if (e1.getEventTime() > e2.getEventTime())
                return 1;
            else if (e1.getEventTime() < e2.getEventTime())
                return -1;

            else
                return 0;
        }
    };
}

