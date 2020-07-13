public class BaseStation {
    int id;
    int  freeChannels;
    int mode;

    public BaseStation(int id, int mode) {
        this.id = id;
        this.mode = mode;
        this.freeChannels = 10;
    }

    public boolean hasFreeChannelForHandover(){ // if there are 9 full channels then there is no free chanel for initiation (if not less than 2 free channels
        if (freeChannels !=0) return true;
        else return false;
    }
    public boolean hasFreeChannelForInitiation(){
        if (mode == 0) {
            if (freeChannels != 0) return true;
        }
        else if(mode == 1){
            if(freeChannels >= 2) return true;


        }
        return false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addCall()  {
        freeChannels --;
    }

    public void removeCall(){
        freeChannels ++;
    }




}



