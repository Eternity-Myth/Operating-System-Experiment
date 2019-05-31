import java.util.Deque;
import java.util.LinkedList;

/**
 * 队列类，用于管理与维护进程队列
 */
public class Queue {
    private Deque<Process>[] deques;// 不同优先级就绪队列组成数组
    private static final Queue readyQueue = new Queue();// 单例设计模式-饿汉式

    private Queue() {
        deques = new LinkedList[3];
        for (int i = 0; i < 3; i++) { //进程有3种不同优先级，因此构造3个就绪队列
            deques[i] = new LinkedList<>();
        }
    }

    public static Queue getReadyQueue() {
        return readyQueue;
    }

    // 将进程加入到其对应优先级的就绪队列中
    public void addProcess(Process process) {
        int priority = process.getPriority();
        Deque<Process> deque = deques[priority];
        deque.addLast(process);
    }

    // 获得就绪队列里面优先级最高的进程。若队列为空，则返回null
    public Process getProcess() {
        for (int i = 2; i >= 0; i--) { // 因为是获取优先级最高的进程，因此应该按优先级从高到低遍历
            Deque<Process> deque = deques[i];
            if (!deque.isEmpty()) {
                return deque.peekFirst(); // 当队列不空时返回队列中第一个进程
            }
        }
        return null; // 若队列为空，则返回null
    }

    // 删除就绪队列里面指定的进程。删除成功返回true，若进程不存在就返回false。
    public boolean removeProcess(Process process) {
        int priority = process.getPriority();
        Deque<Process> deque = deques[priority];
        return deque.remove(process);
    }
}
