import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Simulator {

    private PriorityQueue<Event> FEL;
    private double simulationClock;
    private int attemptedCalls,blockedCallCount, droppedCallCount, numberOfEvents = 0;
    private NumberGenerator numberGenerator = new NumberGenerator();


    public static BaseStation[] stations;

    private void  init(int baseStationMode){
        simulationClock = 0;
        attemptedCalls = 0;
        blockedCallCount = 0;
        droppedCallCount = 0;
        numberOfEvents = 0;
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

    private void handleEvent(Event event){
        if (event instanceof CallInitiationEvent){ // Initiating call
                attemptedCalls++;
            if(stations[event.call.getConnectedBaseStation().getId()].hasFreeChannelForInitiation()){
                stations[event.call.getConnectedBaseStation().getId()].addCall();
                if (event.call.handoverEventRequired()){
                    addHandoverEvent(event.call);
                }
                else{
                    addCallTerminationEven(event.call);
                }
            }
            else {
                CallTerminationEvent droppedEvent = new CallTerminationEvent(simulationClock , event.call, "Blocked");
                FEL.add(droppedEvent);
            }
        }
        if (event instanceof CallTerminationEvent) { // Termination of call
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
        if (event instanceof HandoverEvent){ // Handover of call
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
                    addHandoverEvent(event.call);
                }
                else{
                    addCallTerminationEven(event.call);
                }
            }
            else {
                CallTerminationEvent droppedEvent = new CallTerminationEvent(simulationClock , event.call, "Dropped");
                FEL.add(droppedEvent);
            }
        }
    }

    private void addHandoverEvent(Call call){
        HandoverEvent handoverEvent = new HandoverEvent(simulationClock + call.getTimeLeftInBaseStation(), call);
        FEL.add(handoverEvent);
    }

    private void addCallTerminationEven(Call call){
        CallTerminationEvent callTerminationEvent;
        if( call.getTimeLeftInBaseStation() <= call.getTimeLeftInCall()){
            callTerminationEvent = new CallTerminationEvent(simulationClock + call.getTimeLeftInBaseStation(),call,"end");
        }
        else { callTerminationEvent = new CallTerminationEvent(simulationClock + call.getTimeLeftInCall(),call,"end");}
        FEL.add(callTerminationEvent);
    }

    public int deterministicMode(String filePath){
        String line;
        int expectedNumberOfCalls = 0;
        try{
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            br.readLine();
            while((line = br.readLine()) != null){
                System.out.println(line);
                String[] data = line.split(",");
                Call call= new Call(Double.parseDouble(data[1]),Double.parseDouble(data[3]),stations[Integer.parseInt(data[2]) - 1],Double.parseDouble(data[4]),numberGenerator.positionInBaseStation());
                Event event = new CallInitiationEvent(call.getCallStartTime(), call);
                FEL.add(event);
                expectedNumberOfCalls = FEL.size();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!FEL.isEmpty()){
            Event event = FEL.poll();
            simulationClock = event.getEventTime();
            handleEvent(event);
            numberOfEvents++;
        }
        return expectedNumberOfCalls;
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
            numberOfEvents++;
            if (event instanceof CallTerminationEvent){
                if((event.call.getId() % (numberOfCalls + warmUpPeriod) == 0)){
                    return;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Simulator sim = new Simulator();
        Scanner input = new Scanner(System.in);
        System.out.println("Enter output file name:");
        String outputFileName = input.nextLine();
        File outputFile = new File("output/" + outputFileName + ".csv");
        if(outputFile.exists()){
            System.out.println("File already exists. 1. Delete  2. Append");
            if (input.nextInt() == 1){
                outputFile.delete();
            }
        }
        FileWriter csvWriter = new FileWriter(outputFile);
        csvWriter.append("\n");
        System.out.println("Simulation modes: \n 1. Deterministic\n 2. Stochastic");
        System.out.println("Please enter the simulation mode:");
            int simMode = input.nextInt();
            System.out.println("Base station modes: \n 1. No reserved channels\n 2. One reserved channel");
            System.out.println("Please enter Base Station Mode:");
            int baseStationMode = input.nextInt();
            int numberOfSimulations = 1;
            Date startTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String strStartTime = formatter.format(startTime);
            csvWriter.append("Simulation Number" + "," + "Simulation Type"+ "," + "Base Station Mode" + ","
                    + "Expected Number Of Calls" + "," + "Number Of Calls" + "," + "Number Of Dropped Calls" + ","
                    + "Number Of Blocked Calls" + "," + "Simulation Start Time" + "," + "Simulation Finish Time" + ","
                    + "Number Of Events");
            if(simMode == 1){
                sim.init(baseStationMode);
                System.out.println("Please enter the csv file path for the deterministic simulation");
                String filePath = input.nextLine();
                int expectedNumberOfCalls = sim.deterministicMode(filePath);
                Date finishTime = new Date();
                String strFinishTime = formatter.format(finishTime);
                csvWriter.append(String.valueOf(numberOfSimulations)).append(",Deterministic,").append(String.valueOf(baseStationMode))
                        .append(",").append(String.valueOf(expectedNumberOfCalls)).append(",").append(String.valueOf(sim.attemptedCalls))
                        .append(",").append(String.valueOf(sim.droppedCallCount)).append(",").append(String.valueOf(sim.blockedCallCount))
                        .append(",").append(strStartTime).append(",").append(strFinishTime).append(",").append(String.valueOf(sim.numberOfEvents));
                csvWriter.append("\n");
        }
        if (simMode == 2) {
            System.out.println("Please enter number of simulations:");
            numberOfSimulations = input.nextInt();
            System.out.println("Please enter number of calls:");
            int numberOfCalls = input.nextInt();
            for (int i = 1; i <= numberOfSimulations; i++){
                System.out.println("Simulation #: "+ (i));
                sim.init(baseStationMode);
                sim.warmUp(500);
                sim.stochasticMode(numberOfCalls ,500);
                Date finishTime = new Date();
                String strFinishTime = formatter.format(finishTime);
                csvWriter.append(String.valueOf(i)).append(",Stochastic,").append(String.valueOf(baseStationMode))
                        .append(",").append(String.valueOf(numberOfCalls)).append(",").append(String.valueOf(sim.attemptedCalls))
                        .append(",").append(String.valueOf(sim.droppedCallCount)).append(",").append(String.valueOf(sim.blockedCallCount))
                        .append(",").append(strStartTime).append(",").append(strFinishTime).append(",").append(String.valueOf(sim.numberOfEvents));
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

