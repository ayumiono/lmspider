package com.lmdna.spider.berkeleydb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmdna.spider.node.master.MasterNode;

public class Checkpointer implements Serializable{

	private static final long serialVersionUID = 5704879029048256451L;

    private final static Logger LOGGER = LoggerFactory.getLogger(Checkpointer.class.getName());

    private static final String DEFAULT_PREFIX = "";
    
    /**
     * String to prefix any new checkpoint names.
     */
    private  String checkpointPrefix = DEFAULT_PREFIX;
    
    /**
     * Next  overall series checkpoint number.
     */
    private int nextCheckpoint = 1;
    
    private int maxCheckpoint = 10;

    /**
     * All checkpoint names in chain prior to now. May not all still
     * exist on disk.
     */
    private List predecessorCheckpoints = new LinkedList();

    /**
     * If a checkpoint has begun, its directory under
     * <code>checkpointDirectory</code>.
     */
    private transient File checkpointInProgressDir = null;

    /**
     * If the checkpoint in progress has encountered fatal errors.
     */
    private transient boolean checkpointErrors = false;
    
    /**
     * checkpointThread is set if a checkpoint is currently running.
     */
    private transient Thread checkpointThread = null;
    
    private transient MasterNode master;
    
    /**
     * Setup in constructor or on a call to revovery.
     */
    private transient Timer timerThread = null;
    
    public static final DecimalFormat INDEX_FORMAT = new DecimalFormat("00000");

    /**
     * Create a new CheckpointContext with the given store directory
     * @param cc CrawlController instance thats hosting this Checkpointer.
     * @param checkpointDir Where to store checkpoint.
     */
    public Checkpointer(final MasterNode master, final File checkpointDir) {
        this(master, DEFAULT_PREFIX);
    }
    
    /**
     * Create a new CheckpointContext with the given store directory
     *
     * @param cc CrawlController instance thats hosting this Checkpointer.
     * @param prefix Prefix for checkpoint label.
     */
    public Checkpointer(final MasterNode master, final String prefix) {
        super();
        initialize(master, prefix);
        
    }
    
    protected void initialize(final MasterNode master, final String prefix) {
        this.master = master;
        this.checkpointPrefix = prefix;
    }
    
    void cleanup() {
        if (this.timerThread != null) {
            LOGGER.info("Cleanedup Checkpoint TimerThread.");
            this.timerThread.cancel();
        }
    }
    
    /**
     * @return Returns the nextCheckpoint index.
     */
    public int getNextCheckpoint() {
        return this.nextCheckpoint;
    }

    /**
     * Run a checkpoint of the crawler.
     */
    public void checkpoint() {
        String name = "Checkpoint-" + getNextCheckpointName();
        this.checkpointThread = new CheckpointingThread(name);
        this.checkpointThread.setDaemon(true);
        this.checkpointThread.start();
    }

    /**
     * Thread to run the checkpointing.
     * @author stack
     */
    public class CheckpointingThread extends Thread {
        public CheckpointingThread(final String name) {
            super(name);
        }

        public MasterNode getController() {
        	return Checkpointer.this.master;
        }
        
        public void run() {
            LOGGER.info("Started");
            final boolean alreadyPaused = getController().isPaused() ||
                getController().isPausing();
            try {
                getController().requestCrawlPause();
                setCheckpointErrors(false);
                if (!waitOnPaused()) {
                    checkpointFailed("Failed wait for complete pause.");
                } else {
                    createCheckpointInProgressDirectory();
                    this.getController().checkpoint();
                }
            } catch (Exception e) {
                checkpointFailed(e);
            } finally {
                if (!isCheckpointErrors()) {
                    writeValidity();
                }
                Checkpointer.this.nextCheckpoint++;
                clearCheckpointInProgressDirectory();//TODO should be cleared?
                LOGGER.info("Finished");
                getController().completePause();
                if (!alreadyPaused) {
                    getController().requestCrawlResume();
                }
            }
        }
        
        private synchronized boolean waitOnPaused() {
            // If we're paused we can exit but also exit if the crawl has been
            // resumed by the operator.
            while(!getController().isPaused() && !getController().isRunning()) {
                try {
                    wait(1000 * 3);
                } catch (InterruptedException e) {
                    // May be for us.
                }
            }
            return getController().isPaused();
        }
    }
    
    /**
     * 只保留10个备份文件夹
     * @return
     */
    protected File createCheckpointInProgressDirectory() {
    	//只保留10个备份文件夹
        File[] dirs = this.master.getCheckpointsDisk().listFiles();
        if(dirs.length>=maxCheckpoint){
        	File smallIndexFile = null;
        	for(File curDir : dirs){
        		if(smallIndexFile==null){
        			smallIndexFile = curDir;
        		}else{
        			if(Integer.parseInt(curDir.getName())<Integer.parseInt(smallIndexFile.getName())){
        				smallIndexFile = curDir;
        			}
        		}
        	}
        	FileUtils.deleteDir(smallIndexFile);
        }
        this.checkpointInProgressDir =
            new File(Checkpointer.this.master.getCheckpointsDisk(),getNextCheckpointName());
        this.checkpointInProgressDir.mkdirs();
        
        return this.checkpointInProgressDir;
    }
    
    protected void clearCheckpointInProgressDirectory() {
        this.checkpointInProgressDir = null;
    }
    
    protected MasterNode getController() {
        return this.master;
    }
    
    /**
     * @return next checkpoint name (zero-padding string).
     */
    public String getNextCheckpointName() {
        return formatCheckpointName(this.checkpointPrefix, this.nextCheckpoint);
    }
    
    public static String formatCheckpointName(final String prefix,
    		final int index) {
    	return prefix + INDEX_FORMAT.format(index);
    }

    protected void writeValidity() {
        File valid = new File(this.checkpointInProgressDir,Checkpoint.VALIDITY_STAMP_FILENAME);
        try {
            FileOutputStream fos = new FileOutputStream(valid);
            fos.write(ArchiveUtils.get14DigitDate().getBytes());
            fos.close();
        } catch (IOException e) {
            valid.delete();
        }
    }

    /**
     * @return Checkpoint directory. Name of the directory is the name of this
     * current checkpoint.  Null if no checkpoint in progress.
     */
    public File getCheckpointInProgressDirectory() {
        return this.checkpointInProgressDir;
    }
    
    /**
     * @return True if a checkpoint is in progress.
     */
    public boolean isCheckpointing() {
        return this.checkpointThread != null && this.checkpointThread.isAlive();
    }

    /**
     * Note that a checkpoint failed
     *
     * @param e Exception checkpoint failed on.
     */
    protected void checkpointFailed(Exception e) {
        LOGGER.info(" Checkpoint failed", e);
        checkpointFailed();
    }
    
    protected void checkpointFailed(final String message) {
        LOGGER.warn(message);
        checkpointFailed();
    }
    
    protected void checkpointFailed() {
        this.checkpointErrors = true;
    }
    
    /**
     * @return True if current/last checkpoint failed.
     */
    public boolean isCheckpointFailed() {
        return this.checkpointErrors;
    }

    /**
     * @return Return whether this context is at a new crawl, never-
     * checkpointed state.
     */
    public boolean isAtBeginning() {
        return nextCheckpoint == 1;
    }

    /**
     * Call when recovering from a checkpoint.
     * Call this after instance has been revivifyied post-serialization to
     * amend counters and directories that effect where checkpoints get stored
     * from here on out.
     * @param cc CrawlController instance.
     */
    public void recover(final MasterNode master) {
        // Prepend the checkpoint name with a little 'r' so we tell apart
        // checkpoints made from a recovery.  Allow for there being
        // multiple 'r' prefixes.
        initialize(master, 'r' + this.checkpointPrefix);
    }
    
    /**
     * @return Returns the predecessorCheckpoints.
     */
    public List getPredecessorCheckpoints() {
        return this.predecessorCheckpoints;
    }

    protected boolean isCheckpointErrors() {
        return this.checkpointErrors;
    }

    protected void setCheckpointErrors(boolean checkpointErrors) {
        this.checkpointErrors = checkpointErrors;
    }

}
