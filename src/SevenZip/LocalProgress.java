package SevenZip;

import java.io.IOException;


public class LocalProgress implements ICompressProgressInfo {
    IProgress _progress;
    boolean _inSizeIsMain;
    
    public void Init(IProgress progress, boolean inSizeIsMain) {
        _progress = progress;
        _inSizeIsMain = inSizeIsMain;
    }
    
    public int SetRatioInfo(long inSize, long outSize) {
        return _progress.SetCompleted(_inSizeIsMain ? inSize : outSize);
        
    }
    
}
