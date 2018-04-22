import com.sun.xml.internal.bind.v2.model.core.ID;
import java.util.*;

/*
 *   Round Robin Scheduler
 *   By Christopher Jones
 *   NOTE: I chose LinkedList over a Priority Queue for ease of coding.
 *   With larger process objects, this should be implemented using a Priority Queue instead of a Linked List
 */
public class Main {

    /* 
     * What is known: ID, Arrival Time, and Service Time
     * Create n processes with these three attributes.
     */
    public static void main(String[] args) {
        int numberOfProcesses = 30;
        int contextSwitch = 0;
        int quantum = 2;
        int numberOfFinishedProcesses = 0;

        /*
         * Assign Process Arrival Time
         * Assign Process Service Time
         * NOTE: The index is the process ID, so we need to first
         * know arrival times to assigned ID
         */
        int processArrivalTime = 0;

        ArrayList<IncomingProcess> IncomingProcessList = new ArrayList<>();
        for (int i = 0; i < numberOfProcesses; i++) {
            int processServiceTime = returnRandom(2, 4);
            IncomingProcessList.add(new IncomingProcess(processArrivalTime, processServiceTime));
            processArrivalTime += returnRandom(4, 8);
        }

        /*
         * Assign ID
         * Sort the Process list by arrival time.
         * Use a comparator class to compare object attributes (in this case, arrivalTime)
         */
        Collections.sort(IncomingProcessList, new ProcessArrivalComparator());
        for (int i = 0; i < IncomingProcessList.size(); i++) {
            String ID = "P" + (i + 1);
            IncomingProcessList.get(i).setID(ID);
        }

        /*
         * Place the processes in a processAdapter class collection
         */
        ArrayList<Process> processAdapter = new ArrayList<>();
        for (IncomingProcess p : IncomingProcessList) {
            processAdapter.add(new Process(p.getID(), p.getProcessArrivalTime(), p.getServiceTime()));
        }

        /*
         * The QUEUE
         * TODO: Change this to Priority Queue class. 
         */
        LinkedList<Process> READY_QUEUE = new LinkedList<>();
        int clockTime = 0;
        while (true) {
         
          //System.out.println(clockTime);
           
            for (Process p : processAdapter) {
                if (p.getProcessArrivalTime() == clockTime) {
                    READY_QUEUE.addLast(p);
                  //  System.out.println("ITEM ADDED: ID " + p.getID());
                }
            }

            if (numberOfFinishedProcesses == numberOfProcesses) {
                break;
            }


            if (READY_QUEUE.isEmpty()) {
                clockTime++;
                continue;
            }


            /*
             * If there is an item in the queue, process it.
             */
            if (!READY_QUEUE.isEmpty()) {
                //The first time a process has been executed
                if (READY_QUEUE.getFirst().getTimeRemaining() == READY_QUEUE.getFirst().getServiceTime()) {
                    READY_QUEUE.getFirst().setStartTime(clockTime);
                    READY_QUEUE.getFirst().setInitialWait(clockTime - READY_QUEUE.getFirst().getProcessArrivalTime());
                }

                if (READY_QUEUE.getFirst().getTimeRemaining() <= quantum) {
                    for (Process p : processAdapter) {
                        if (!READY_QUEUE.isEmpty()) {
                            String ID = READY_QUEUE.getFirst().getID();
                            if (p.getID().equals(ID)) {

                                p.setStartTime(READY_QUEUE.getFirst().getStartTime());
                                p.setEndTime(clockTime + READY_QUEUE.getFirst().getTimeRemaining());
                                p.setTurnAround(p.getEndTime() - p.getProcessArrivalTime());

                                p.setInitialWait(p.getStartTime() - p.getProcessArrivalTime());
                                p.setTotalWait(p.getEndTime() - p.getProcessArrivalTime());
                                numberOfFinishedProcesses++;
                              //  System.out.println(READY_QUEUE.getFirst().getID() + " REMOVED");

                            }

                        }
                    }
                    clockTime += READY_QUEUE.getFirst().getTimeRemaining() + contextSwitch;
                    READY_QUEUE.getFirst().setTimeRemaining(0);
                    READY_QUEUE.removeFirst();
                    //numberOfFinishedProcesses++;
                } else {

                /*
                 * If the process does not end before quantum time, then proceed
                 */
                    int newTimeRemaining = READY_QUEUE.getFirst().getTimeRemaining() - quantum;
                    READY_QUEUE.getFirst().setTimeRemaining(newTimeRemaining);
                    Process p = READY_QUEUE.getFirst();
                    READY_QUEUE.removeFirst();
                    READY_QUEUE.addLast(p);
                    clockTime += (quantum + contextSwitch);
                }
            }

        }

    /*
     * Output the results of the processes
     */
        outputResultsTable(processAdapter, numberOfFinishedProcesses);

    }


    private static void outputResultsTable(ArrayList<Process> processList, int finishedProcesses) {

        /*
         * Turnaround Time
         */
        double totalTurnaround = 0;
        for (Process p : processList) {
            totalTurnaround += p.getTurnAround();
        }
        System.out.printf("%-10s %-2s %-10s\n", "The average Turnaround Time was: ", (double) totalTurnaround/finishedProcesses, " seconds");



        /*
         * Output Table
         */
        System.out.printf("%-10s %-2s %-10s %-2s %-10s %-2s %-20s %-2s %-20s %-2s %-20s %-2s %-20s\n", "Process ID", "|", "Start Time", "|", "End Time", "|", "Initial Wait Time",
                "|", "Process Arrival Time", "|", "Process Service Time", "|", "Process Turnaround Time");
        System.out.println("---------------------------------------------------------------------------------------------------------------");
        for (Process P : processList) {
            System.out.printf("%-10s %-2s %-10d %-2s %-10d %-2s %-20d %-2s %-20s %-2s %-20s %-2s %-20s\n", P.getID(), "|",
                    P.getStartTime(), "|", P.getEndTime(),
                    "|", P.getInitialWait(), "|", P.getProcessArrivalTime(),
                    "|", P.getServiceTime(), "|", P.getTurnAround());

        }



    }

    /*
     * Compute a random integer within range a to b
     */
    private static int returnRandom(int a, int b) {
        Random randomInt = new Random();
        return randomInt.nextInt(a + 1) + (b - a);
    }
}

class IncomingProcess implements Comparable<IncomingProcess> {
    private String ID;
    private int processArrivalTime;
    private int serviceTime;

    IncomingProcess(int arrivalTime, int serviceTime) {
        setProcessArrivalTime(arrivalTime);
        setServiceTime(serviceTime);
    }

    public int getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }

    public int getProcessArrivalTime() {
        return processArrivalTime;
    }

    public void setProcessArrivalTime(int processArrivalTime) {
        this.processArrivalTime = processArrivalTime;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int compareTo(IncomingProcess anotherInstance) {
        return this.processArrivalTime - anotherInstance.processArrivalTime;
    }

}

class ProcessArrivalComparator implements Comparator<IncomingProcess> {
    public int compare(IncomingProcess process1, IncomingProcess process2) {
        return process1.getProcessArrivalTime() - process2.getProcessArrivalTime();
    }
}

class Process {
    private String ID;
    private int processArrivalTime;
    private int serviceTime;
    private int startTime;
    private int endTime;
    private int initialWait;
    private int totalWait;
    private int turnAround;
    private int processTimeRemaining;

    Process(String ID, int processArrivalTime, int serviceTime) {
        setID(ID);
        setProcessArrivalTime(processArrivalTime);
        setServiceTime(serviceTime);
        setTimeRemaining(serviceTime);
    }

    public int getTimeRemaining() {
        if (processTimeRemaining < 0) {
            processTimeRemaining = 0;
        }
        return processTimeRemaining;
    }

    public void setTimeRemaining(int timeRemaining) {
        this.processTimeRemaining = timeRemaining;
    }


    public int getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }

    public int getProcessArrivalTime() {
        return processArrivalTime;
    }

    public void setProcessArrivalTime(int processArrivalTime) {
        this.processArrivalTime = processArrivalTime;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getTotalWait() {
        return totalWait;
    }

    public void setTotalWait(int totalWait) {
        this.totalWait = totalWait;
    }

    public int getTurnAround() {
        return turnAround;
    }

    public void setTurnAround(int turnAround) {
        this.turnAround = turnAround;
    }

    public int getInitialWait() {
        return initialWait;
    }

    public void setInitialWait(int initialWait) {
        this.initialWait = initialWait;
    }

}
