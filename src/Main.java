
import java.io.*;
import java.util.Scanner;

public class Main {
    private static final PCB pcb = PCB.getPCB();// 生成PCB

    // 生成4个资源R1、R2、R3、R4，并设定资源数量
    private static final Resource R1 = new Resource(1, 1);
    private static final Resource R2 = new Resource(2, 2);
    private static final Resource R3 = new Resource(3, 3);
    private static final Resource R4 = new Resource(4, 4);

    public static void main(String[] args) throws IOException {

        pcb.createProcess("init", 0); // 创建init进程，优先级设定为0
        System.out.print("init" + "  ");
        if (args.length != 0) { // 有命令行参数时，从文件读取
            loadFile(args[0]);
        } else { // 无命令行参数时，从键盘录入
            System.out.println();
            Scanner scanner = new Scanner(System.in); //scanner接收输入
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                if (input.trim().equals("")) { //空命令时继续接收
                    continue;
                }
                exec(input);
            }
        }
    }

    // 对输入的命令进行处理，适用于键盘录入或者文件读入
    public static void exec(String input) {
        String[] commands = new String[]{input};
        for (String command : commands) { //对不同的输入命令进行处理
            String[] cmds = command.split("\\s+");
            String options = cmds[0];
            switch (options) {
//                    case "init":
//                        if (pcb.findProcess("init") != null) { // 已经完成了初始化，系统中已经有init进程
//                            System.out.println("错误！已完成过初始化！init进程已存在！");
//                        } else {
//                            pcb.createProcess("init", 0); // 创建init进程，优先级设定为0
//                            System.out.println("初始化成功！init进程已创建！");
//                        }
//                        break;
                case "cr":
//                    if (pcb.findProcess("init") == null) { // 检查是否完成了初始化
//                        System.out.println("错误！系统未初始化！请先执行init命令初始化！");
//                    } else
                    if (cmds.length != 3) { // 检查输入格式是否正确
                        System.out.println("错误！请输入合法的参数！");
                    } else {
                        String processName = cmds[1];
                        int priority = 0;
                        try { // 检查优先级的输入是否正确
                            priority = Integer.parseInt(cmds[2]);
                            if (priority <= 0 || priority > 2) {
                                System.out.println("错误！请输入合法的参数！");
                                continue;
                            }
                        } catch (Exception e) {
                            System.out.println("错误！请输入合法的参数！");
                        }
                        if (pcb.exsitName(processName)) { // 检查用户输入的进程名是否已经存在
                            System.out.println("错误！进程名" + processName + "已经存在！请选择其它的进程名！");
                            break;
                        }
                        pcb.createProcess(processName, priority);
                    }
                    break;
                case "de":
//                    if (pcb.findProcess("init") == null) { // 检查是否完成了初始化
//                        System.out.println("错误！系统未初始化！请先执行init命令初始化！");
//                    } else
                    if (cmds.length != 2) { // 检查输入格式是否正确
                        System.out.println("错误！请输入合法的参数！");
                    } else {
                        String processName = cmds[1];
                        Process process = pcb.findProcess(processName);
                        if (process == null) { // 检查用户输入的进程名是否已经存在
                            System.out.println("错误！没有名为" + processName + "的进程！");
                        } else if (processName.equals("init")) { // 设定不允许用户删除系统init进程
                            System.out.println("错误！您没有权限终止init进程！");
                        } else {
                            process.destroy();
//                            process.killSubTree(); // 将进程自身包括其所有子进程终止
//                            PCB.scheduler();
//                                    System.out.println("终止进程成功！");
                        }
                    }
                    break;
                case "req":
//                    if (pcb.findProcess("init") == null) { // 检查是否完成了初始化
//                        System.out.println("错误！系统未初始化！请先执行init命令初始化！");
//                    } else
                    if (cmds.length != 3) { // 检查输入格式是否正确
                        System.out.println("错误！请输入合法的参数！");
                    } else {
                        String resourceName = cmds[1];
                        int needNum = 0;
                        try {
                            needNum = Integer.parseInt(cmds[2]);
                        } catch (Exception e) {
                            System.out.println("错误！请输入合法的参数！");
                        }
                        Process currentProcess = pcb.getCurrentProcess(); // 获取当前进程
                        switch (resourceName) { // 检查资源名称，请求对应资源
                            case "R1":
                                R1.request(currentProcess, needNum);
                                break;
                            case "R2":
                                R2.request(currentProcess, needNum);
                                break;
                            case "R3":
                                R3.request(currentProcess, needNum);
                                break;
                            case "R4":
                                R4.request(currentProcess, needNum);
                                break;
                            default:
                                System.out.println("错误！请输入合法的参数！");
                        }
                    }
                    break;
                case "rel":
//                    if (pcb.findProcess("init") == null) { // 检查是否完成了初始化
//                        System.out.println("错误！系统未初始化！请先执行init命令初始化！");
//                    } else
                    if (cmds.length != 2) { // 检查输入格式是否正确
                        System.out.println("错误！请输入合法的参数！");
                    } else {
                        String resourceName = cmds[1];
                        Process currentProcess = pcb.getCurrentProcess(); // 获取当前进程
                        switch (resourceName) { // 检查资源名称，释放对应资源
                            case "R1":
                                R1.release(currentProcess);
                                break;
                            case "R2":
                                R2.release(currentProcess);
                                break;
                            case "R3":
                                R3.release(currentProcess);
                                break;
                            case "R4":
                                R4.release(currentProcess);
                                break;
                            default:
                                System.out.println("错误！请输入合法的参数！");
                        }
                    }
                    break;
                case "to":
                    pcb.timeout();
                    break;
                case "lp":
//                    if (pcb.findProcess("init") == null) { // 检查是否完成了初始化
//                        System.out.println("错误！系统未初始化！请先执行init命令初始化！");
//                    }
                    if (cmds.length == 1) { // lp命令打印所有进程树和信息
                        pcb.printProcessTree(pcb.findProcess("init"), 0);
                    } else if (cmds.length < 3 || !cmds[1].equals("-p")) { // lp -p pname命令打印某具体进程的信息
                        System.out.println("错误！请输入合法的参数或命令！");
                    } else {
                        String pname = cmds[2];
                        Process process = pcb.findProcess(pname);
                        if (process == null) {
                            System.out.println("错误！没有名为" + pname + "的进程！");
                        } else {
                            pcb.printProcessDetail(process);
                        }
                    }
                    break;
                case "lr":
                    R1.printCurrentStatus();
                    R2.printCurrentStatus();
                    R3.printCurrentStatus();
                    R4.printCurrentStatus();
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                    System.out.println("Good Bye！");
                    System.exit(0);
//                case"list":
//                    pcb.printExistProcess();
//                    break;
                default:
                    System.out.println("错误！请输入合法的命令！");
                    break;
            }
        }
        if (pcb.getCurrentProcess() != null) {
            System.out.print(pcb.getCurrentProcess().getProcessName() + "  ");
        }
    }

    // 以读文件的方式运行系统
    private static void loadFile(String filePath) throws IOException {
        InputStream in = new FileInputStream(filePath);
        LineNumberReader reader = new LineNumberReader(new FileReader(filePath));
        String cmd = null;
        while ((cmd = reader.readLine()) != null) {
            if (!"".equals(cmd)) {
                exec(cmd);
            }
        }
    }

    private static void printHelp() {
        System.out.println("-----------------------------------------------------------------------------------------------------");
        System.out.println("     cr pname priority:  创建新进程并指定进程名与优先级（0,1,2）");
        System.out.println("     de pname:           按照特定进程名终止某进程（init进程除外，不允许终止init进程）");
        System.out.println("     req RID num:        为当前正在运行的进程请求指定数量的资源");
        System.out.println("     rel RID:            为当前正在运行的进程释放某特定的所有资源");
        System.out.println("     to:                 当前正在运行的时间片完");
        System.out.println("     lp:                 打印所有进程的信息");
        System.out.println("     lp -p pname:        指定进程名打印某特定进程的信息");
        System.out.println("     lr:                 打印所有资源的信息");
        System.out.println("     exit:               退出系统");
        System.out.println("     help:               输出此帮助信息");
        System.out.println("By UESTC-关文聪 2016060601008");
        System.out.println("-----------------------------------------------------------------------------------------------------");
    }
}
