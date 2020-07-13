public class BaseStationFullException extends  Exception {
    public BaseStationFullException(int baseID ){
        System.out.println("Base Station" + baseID  + "channels full. Dropping call");
    }
}