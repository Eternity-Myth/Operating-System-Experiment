import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PCB（进程管理块）类，用于进程的管理
 */
public class PCB {
    private static final PCB pcb = new PCB();// 单例设计模式-饿汉式
    private static final Queue readyQueue = Queue.getReadyQueue(); //生成就绪队列

    public static PCB getPCB() {
        return pcb;
    }

    private static Map<String, Process> existProcesses;// 所有存活的进程，包括Running（运行状态）,Blocked（阻塞状态）,Ready（就绪状态）
    private Process currentProcess;// 当前占用CPU的进程
    private AtomicInteger pidGenerator;// pid生成器，用以生成唯一的pid

    private PCB() {
        existProcesses = new HashMap<>();
        pidGenerator = new AtomicInteger();
    }

    public Process getCurrentProcess() {
        return currentProcess;
    }

    public void setCurrentProcess(Process currentProcess) {
        this.currentProcess = currentProcess;
    }

    // 生成PID号（以自增方式生成保证不重复）
    public int generatePID() {
        return pidGenerator.getAndIncrement();
    }

    // 每个进程一经创建，便会调用该方法，将其放在ExistList中
    public void addExistList(Process process) {
        existProcesses.put(process.getProcessName(), process);
    }

    // 创建新进程
    public Process createProcess(String processName, int priority) {
        Process currentProcess = pcb.getCurrentProcess();
        // 为新建进程分配PID，进程名，优先级，进程状态，资源，父进程和子进程信息等
        Process process = new Process(pcb.generatePID(), processName, priority, Process.State.NEW, new ConcurrentHashMap<>(), currentProcess, new LinkedList<>());
        if (currentProcess != null) { // 排除创建的进程为第一个进程的特殊情况
            currentProcess.getChildren().add(process);// 新创建进程作为当前进程的子进程
            process.setParent(currentProcess); // 旧进程作为新创建进程的父进程
        }
        pcb.addExistList(process); // 将新创建的进程放在ExistList中
        readyQueue.addProcess(process);// 将新创建的进程放入就绪队列中
        process.setState(Process.State.READY); // 成功进入就绪队列的进程，其状态将置为就绪状态
        PCB.scheduler(); // 调度
        return process;
    }

    // 主要用于判断用户输入的进程名称是否合法，因为name对用户来说是进程唯一标识
    public static boolean exsitName(String name) {
        return existProcesses.containsKey(name);
    }

    // 通过进程名称在existList里面找到进程，返回对进程的引用。若无，则返回null
    public Process findProcess(String processName) {
        for (Map.Entry<String, Process> entry : existProcesses.entrySet()) {
            String name = entry.getKey();
            if (processName.equals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    // 进程调度
    public static void scheduler() {
        Process currentProcess = pcb.getCurrentProcess();
        Process readyProcess = readyQueue.getProcess();
        if (readyProcess == null) { // 就绪队列为空时，CPU正在运行的只有init进程
            pcb.getCurrentProcess().setState(Process.State.RUNNING);// 状态设为运行状态
            return;
        } else if (currentProcess == null) { // 实际上，此处只有在刚初始化系统时才可能发生
            readyQueue.removeProcess(readyProcess);
            pcb.setCurrentProcess(readyProcess);
            readyProcess.setState(Process.State.RUNNING);
            return;
        } else if (currentProcess.getState() == Process.State.BLOCKED || currentProcess.getState() == Process.State.TERMINATED) { //当前进程被阻塞或者已经被终止
            readyQueue.removeProcess(readyProcess); // 从就绪队列取出一个就绪进程
            pcb.setCurrentProcess(readyProcess); // 将该进程设为当前运行的进程
            readyProcess.setState(Process.State.RUNNING); // 该进程状态设为运行状态
        } else if (currentProcess.getState() == Process.State.RUNNING) { // 新创建了进程，或者阻塞队列中进程转移到readyList
            if (currentProcess.getPriority() < readyProcess.getPriority()) { // 若就绪进程优先级更高，则切换进程
                preempt(readyProcess, currentProcess);
            }
        } else if (currentProcess.getState() == Process.State.READY) { // 时间片完的情况
            if (currentProcess.getPriority() <= readyProcess.getPriority()) { // 若有优先级大于或等于当前进程的就绪进程，则切换进程
                preempt(readyProcess, currentProcess);
            } else { // 如果没有高优先级的就绪进程，则当前进程依然继续运行
                currentProcess.setState(Process.State.RUNNING);
            }
        }
        return;
    }

    // 进程切换
    public static void preempt(Process readyProcess, Process currentProcess) {
        if (exsitName(currentProcess.getProcessName())) {
            readyQueue.addProcess(currentProcess); // 将当前进程加入就绪队列中
            currentProcess.setState(Process.State.READY); // 将进程状态置为就绪状态
            readyQueue.removeProcess(readyProcess); // 从就绪队列取出一个就绪进程
            pcb.setCurrentProcess(readyProcess);// 将该进程设为当前运行的进程
            readyProcess.setState(Process.State.RUNNING);// 该进程状态设为运行状态
            return;
        }
    }

    // 时间片轮转（RR），时间片完后切换进程
    public static void timeout() {
        pcb.getCurrentProcess().setState(Process.State.READY); // 时间片完直接将当前运行进程置为就绪状态
        scheduler(); // 调度
    }

    // 从existProcess队列中删除进程
    public void killProcess(Process process) {
        String name = process.getProcessName();
        existProcesses.remove(name);
    }

    // 递归打印进程树信息
    public void printProcessTree(Process process, int retract) {
        for (int i = 0; i < retract; i++) {
            System.out.print("  ");
        }
//        System.out.println("|-" + process.getProcessName() + "(进程状态：" + process.getState() + ",优先级：" + process.getPriority() + ")");
        System.out.print("|-");
        printProcessDetail(process);
        List<Process> children = process.getChildren(); // 获取子进程
        for (int i = 0; i < children.size(); i++) {
            Process child = children.get(i);
            printProcessTree(child, retract + 1); // 递归打印子树的进程树信息
        }
    }

    // 输出进程的详细信息
    public void printProcessDetail(Process process) {
        System.out.print(process.getProcessName() + "(PID:" + process.getPID() + ",进程状态：" + process.getState() + ",优先级：" + process.getPriority() + ",");
        if (process.getResourceMap().isEmpty()) { // 判断有无资源占用
            System.out.println("(无资源占用))");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (Map.Entry<Resource, Integer> entry : process.getResourceMap().entrySet()) {
                Resource res = entry.getKey();
                int holdNum = entry.getValue();
                sb.append(",").append("R").append(res.getRID()).append(":").append(holdNum);
            }
            sb.append(")");
            String result = sb.toString();
            System.out.println(result.replaceFirst(",", ""));
        }
    }

//    // 打印existProcess的信息，主要是方便测试
//    public void printExistProcess() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("existList:[");
//        for (Map.Entry<String, Process> entry : existProcesses.entrySet()) {
//            String name = entry.getKey();
//            String state = entry.getValue().getState().toString();
//            sb.append(",").append(name)
//                    .append("(").append(state).append(")");
//        }
//        sb.append("]");
//        String result = sb.toString();
//        System.out.println(result.replaceFirst(",", ""));
//    }

}
