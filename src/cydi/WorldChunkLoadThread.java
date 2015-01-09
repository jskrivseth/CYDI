/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

/**
 *
 * @author Jesse
 */
public class WorldChunkLoadThread implements Runnable {

    WorldChunk chunkToProcess;

    public WorldChunkLoadThread(WorldChunk chunkToProcess) {
        this.chunkToProcess = chunkToProcess;

    }

    @Override
    public void run() {
        synchronized (chunkToProcess) {
            //setup the chunk

            if (Game.OPT_SAVE_CHUNKS) {
                chunkToProcess.load();
            } else {
                chunkToProcess.generate();
            }
        }
    }
}
