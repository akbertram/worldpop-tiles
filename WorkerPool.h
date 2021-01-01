
#ifndef WORLDPOPTILES_WORKERPOOL_H
#define WORLDPOPTILES_WORKERPOOL_H

#include <vector>
#include <mutex>
#include <thread>
#include <deque>

#include "Task.h"

using namespace std;

template<class T>
class WorkerPool {

    deque<T> queue;
    mutex queueMutex;

public:
    void Add(T& task) {
        queue.push_back(task);
    }

    void Run() {
        cerr << queue.size() << " tasks enqueued." << endl;

        vector<thread> pool;
        unsigned int numThreads = thread::hardware_concurrency();
        for(int i = 0; i < numThreads; i++) {
            pool.emplace_back(&WorkerPool::ProcessQueue, this);
        }
        for(thread& worker : pool) {
            worker.join();
        }
    }

private:
    void ProcessQueue() {
        while(true) {
            queueMutex.lock();
            if(queue.empty()) {
                queueMutex.unlock();
                return;
            }
            T task = queue.front();
            queue.pop_front();
            queueMutex.unlock();

            task.Run();
        }
    };

};


#endif //WORLDPOPTILES_WORKERPOOL_H
