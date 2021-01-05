import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A queue of tile batches to be rendered.
 *
 * <p>This implements a threadpool of workers that process the tile batches in the queue, ensuring
 * that we don't render overlapping batches (from two different countries) at the same time.</p>
 */
public class TileRenderQueue {

  private final LinkedList<TileBatch> queue = new LinkedList<>();
  private final List<TileBatch> rendering = new ArrayList<>();
  private int numWorkers;

  public TileRenderQueue() {
    this.numWorkers = Runtime.getRuntime().availableProcessors();
  }

  public void enqueue(TileBatch batch) {
    queue.add(batch);
  }

  public void run() {

    List<Thread> workers = new ArrayList<>();
    for (int i = 0; i < numWorkers; i++) {
      Thread worker = new Thread(this::processQueue);
      worker.setName("Tile Render Worker " + i);
      worker.start();
      workers.add(worker);
    }
    for (Thread worker : workers) {
      try {
        worker.join();
      } catch (InterruptedException e) {
        System.err.println("TileRenderQueue interrupted.");
      }
    }
  }

  public void processQueue() {
    while(true) {
      TileBatch batch;
      try {
        batch = takeNext();
      } catch (InterruptedException e) {
        return;
      }

      if (batch == null) {
        return;
      }

      System.err.println("Starting " + batch);
      batch.render();

      done(batch);
    }
  }

  private TileBatch takeNext() throws InterruptedException {

    synchronized (this) {
      while (true) {
        if (queue.isEmpty()) {
          notifyAll();
          return null;
        }

        Iterator<TileBatch> it = queue.iterator();
        while (it.hasNext()) {

          // check to see if this batch overlaps with a batch
          // that's running
          TileBatch batch = it.next();
          if (!rendering.stream().anyMatch(b -> b.overlaps(batch))) {
            rendering.add(batch);
            it.remove();
            return batch;
          }
        }
        System.err.println(Thread.currentThread().getName() + " starved, waiting for non-overlapping batch...");
        wait();
      }
    }
  }

  private void done(TileBatch batch) {
    synchronized (this) {
      rendering.remove(batch);
      notify();
    }
  }
}
