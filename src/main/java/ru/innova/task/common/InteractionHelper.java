package ru.innova.task.common;

import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Вспомогательный класс, позволяющий взаимодействовать с пользователем через консоль,
 * если она доступна.
 * 
 * @author sergey
 */
public class InteractionHelper {
    private final PrintWriter err;
    private final PrintWriter out;
    private final Console console;
    private final String encoding;

    /**
     * Создает объект
     */
    public InteractionHelper() {
        this.console = System.console();
        if (console == null) {
            this.err = new PrintWriter(new InternalWriter(System.err));
            this.out = new PrintWriter(new InternalWriter(System.out));
        } else {
            this.err = this.console.writer();
            this.out = this.console.writer();
        }
        this.encoding = System.getProperty("console.encoding", System.getProperty("file.encoding", "iso8859-1"));
    }
    
    /**
     * Вывести сообщение.
     * 
     * @param msg формат сообщения
     * @param params параметры сообщения
     */
    public void showMessage(String msg, Object... params) {
        out.printf(msg, params);
        out.println();
    }

    /**
     * Вывести сообщение об ошибке
     * 
     * @param msg формат сообщения об ошибке
     * @param params параметры сообщения об ошибке
     */
    public void showError(String msg, Object... params) {
        err.printf(msg, params);
        err.println();
    }

    /**
     * Вывести сообщение о пойманном исключении.
     * 
     * @param e исключение, которое надо описать
     */
    public void showException(Throwable e) {
        e.printStackTrace(err);
    }
    
    private static final class InternalWriter extends Writer {
        private final PrintStream std;
        InternalWriter(PrintStream printStream) {
            this.std = printStream;
        }
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            std.print(new String(cbuf, off, len));
        }
        @Override
        public void flush() throws IOException {
            std.flush();
        }
        @Override
        public void close() throws IOException {
            std.close();
        }
    }

    /**
     * Подождать, пока пользователь нажмет Enter.
     */
    public void review() {
        if (console != null) {
            console.readLine("Press Enter...");
        }
    }

    /**
     * Запросить у пользователя ввод текстовой строки.
     * @param message сообщение с приглашением
     * @param defaultValue значение по умолчанию
     * @return введенный пользователем текст без последнего конца строки. Если пользователь
     *    ввел пустую строку, то возвращается значение <code>defaultValue</code>
     */
    public String prompt(String message, String defaultValue) {
        String result;
        String defaultStr = defaultValue == null ? "" : defaultValue;
        if (console != null) {
            result = console.readLine("%s", message, defaultStr);
        } else {
            err.printf("%s", message, defaultStr);
            err.flush();
            byte buf[] = new byte[1024];
            try {
                int cnt = System.in.read(buf);
                while (cnt >= 1 && (buf[cnt - 1] == 0x0A || buf[cnt - 1] == 0x0D)) {
                    cnt--;
                }
                result = new String(buf, 0, cnt, encoding);
            } catch (IOException e) {
                showException(e);
                result = null;
            }
        }
        if (result != null && result.trim().isEmpty()) {
            result = null;
        }
        if (result == null) {
            result = defaultStr;
        }
        return result;
    }
}
