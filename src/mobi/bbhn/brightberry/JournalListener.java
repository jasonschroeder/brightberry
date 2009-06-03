package mobi.bbhn.brightberry;

import net.rim.device.api.io.file.FileSystemJournal;
import net.rim.device.api.io.file.FileSystemJournalEntry;
import net.rim.device.api.io.file.FileSystemJournalListener;

final class BrightBerryJournalListener implements FileSystemJournalListener {
    private PostPhotoScreen _screen;
    private long _lastUSN; // = 0;
    
    BrightBerryJournalListener(PostPhotoScreen screen) {
        _screen = screen;
    }

    public void fileJournalChanged() {
        long nextUSN = FileSystemJournal.getNextUSN();
        String msg = null;
        
        for (long lookUSN = nextUSN - 1; lookUSN >= _lastUSN && msg == null; --lookUSN) {
            FileSystemJournalEntry entry = FileSystemJournal.getEntry(lookUSN);
            if (entry == null) { 
                break;
            }

            String path = entry.getPath();
            
            if (path.endsWith(".jpg") && entry.getEvent() == FileSystemJournalEntry.FILE_ADDED || path.endsWith(".JPG") && entry.getEvent() == FileSystemJournalEntry.FILE_ADDED) {
        		_screen.updateFileName(entry.getPath());
        		break;
            }
        }
        _lastUSN = nextUSN;
    }
}
