package SevenZip;


class SevenZip_CoderInfo {
    
    int NumInStreams;
    int NumOutStreams;
    public ObjectVector<AltCoderInfo> AltCoders = new SevenZip.ObjectVector<AltCoderInfo>();
    
    boolean IsSimpleCoder() { return (NumInStreams == 1) && (NumOutStreams == 1); }
    
    public SevenZip_CoderInfo() {
        NumInStreams = 0;
        NumOutStreams = 0;
    }
}
