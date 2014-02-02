package ru.innova.task.proxy;

import java.util.concurrent.CountDownLatch;

import ru.innova.task.common.AbstractWorker;
import ru.innova.task.common.InteractionHelper;

/**
 * Управляющий поток, исполняющий команды пользователя, полученный из консоли.
 * 
 * @author sergey
 */
public class ControlThread extends AbstractWorker {
    private final InteractionHelper interactionHelper;
    private final CountDownLatch startSignal;
    private final CountDownLatch exitSignal;

    /**
     * Создает объект
     * 
     * @param startSignal объект для передачи сигнала о старте
     * @param exitSignal объект для передачи сигнала о выходе
     */
    public ControlThread(CountDownLatch startSignal, CountDownLatch exitSignal) {
        super(1, "proxy.Control");
        this.startSignal = startSignal;
        this.exitSignal = exitSignal;
        this.interactionHelper = new InteractionHelper();
    }

    @Override
    public Integer call() {
        while (true) {
            if (exited) {
                break;
            }
            String cmd = interactionHelper.prompt("Enter command: ", "help");
            if (cmd.equals("help")) {
                interactionHelper.showMessage("help - show this message\n"
                                        + "start - start transferring numbers\n"
                                        + "exit - exit program");
            } else if (cmd.equals("start")) {
                startSignal.countDown();
            } else if (cmd.equals("exit")) {
                interactionHelper.showMessage("Bye!");
                exitSignal.countDown();
                break;
            }
        }
        return 0;
    }
}
