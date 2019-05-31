import java.util.List;
import java.util.Map;

/**
 * 进程类，用于进程的定义和进程状态的管理
 */
public class Process {
    private int PID; // 进程ID
    private String processName; // 进程名
    private int priority; // 进程优先级
    private State state; // 进程状态
    private Map<Resource, Integer> resourceMap; // 进程持有的资源和相应数量
    private Resource blockResource;// 如果进程状态为阻塞的话，这个属性就指向被阻塞的资源，否则应该为null

    private Process parent; // 进程的父进程
    private List<Process> children; // 进程的子进程

    private static final PCB pcb = PCB.getPCB();
    private static final Queue readyQueue = Queue.getReadyQueue();

    // 进程的五状态：NEW（新建）, READY（就绪）,RUNNING（运行）, BLOCKED（阻塞）, TERMINATED（终止）
    public enum State {
        NEW, READY, RUNNING, BLOCKED, TERMINATED
    }

    public Process(int PID, String processName, int priority, State state, Map<Resource, Integer> resourceMap, Process parent, List<Process> children) {
        this.PID = PID;
        this.processName = processName;
        this.priority = priority;
        this.state = state;
        this.resourceMap = resourceMap;
        this.parent = parent;
        this.children = children;
    }

    public int getPID() {
        return PID;
    }

    public String getProcessName() {
        return processName;
    }

    public int getPriority() {
        return priority;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setParent(Process parent) {
        this.parent = parent;
    }

    public List<Process> getChildren() {
        return children;
    }


    public Resource getBlockResource() {
        return blockResource;
    }

    public Map<Resource, Integer> getResourceMap() {
        return resourceMap;
    }

    public void setBlockResource(Resource blockResource) {
        this.blockResource = blockResource;
    }

    // 删除进程时调用
    public void destroy() {
        killSubTree();
        PCB.scheduler();
        return;
    }

    // 删除子进程
    public void removeChild(Process process) {
        for (Process child : children) {
            if (child == process) {
                children.remove(child);
                return;
            }
        }
    }

    // 删除进程以及以其为根的进程树
    public void killSubTree() {
        if (!children.isEmpty()) { // 当进程子树不为空
            int childNum = children.size();
            for (int i = 0; i < childNum; i++) {
                Process child = children.get(0);
                child.killSubTree();// 递归删除子树
            }
        }
        // 对不同状态的进程处理
        if (this.getState() == State.TERMINATED) { // 进程状态已为终止状态，说明删除成功
            pcb.killProcess(this);
            return;
        } else if (this.getState() == State.READY) { // 进程状态为就绪状态，从就绪队列删除，修改其状态为终止状态
            readyQueue.removeProcess(this);
            pcb.killProcess(this);
            this.setState(State.TERMINATED);
        } else if (this.getState() == State.BLOCKED) { // 进程状态为阻塞状态，从阻塞队列删除，修改其状态为终止状态
            Resource blockResource = this.getBlockResource();
            blockResource.removeBlockProcess(this);
            pcb.killProcess(this);
            this.setState(State.TERMINATED);
        } else if (this.getState() == State.RUNNING) { // 进程状态为运行状态时直接终止，修改其状态为终止状态
            this.setState(State.TERMINATED);
            pcb.killProcess(this);
        }
        // 清除进程的parent和child指针
        parent.removeChild(this);
        parent = null;
        //释放资源
        for (Resource resource : resourceMap.keySet()) {
            resource.release(this);
        }
        return;
    }

}
