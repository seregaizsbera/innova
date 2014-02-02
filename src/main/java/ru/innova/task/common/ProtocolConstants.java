package ru.innova.task.common;

/**
 * Константы используемые при передаче управляющих сообщений.
 * 
 * @author sergey
 */
public final class ProtocolConstants {
    /**
     * Пустое значение.
     */
    public static final int NONE_VALUE = -1;
    
    /**
     * Это значение шлет initator в proxy, когда выполняет старт.
     */
    public static final int START_SIGN = -2;
    
    /**
     * Это значение шлет initator в proxy, когда приостанавливается.
     */
    public static final int STOP_SIGN = -3;
    
    
    /**
     * Это значение шлет initator в proxy перед выходом
     */
    public static final int EXIT_SIGN = -4;
    
    /**
     * Это значение шлет initator в proxy, чтобы proxy понял, что к
     * нему подключился именно initiator.
     */
    public static final int INITIATOR_SIGN = -5;

    /**
     * Это значение шлет echo в proxy, чтобы proxy понял, что к
     * нему подключился именно echo.
     */
    public static final int ECHO_SIGN = -6;
    
    /**
     * На всякий случай у proxy тоже есть свой идентификатор.
     */
    public static final int PROXY_SIGN = -7;
    
    /**
     * Это значение шлют клиенты в proxy, чтобы proxy понял, что соединение создается
     * для передачи данных от клиента к proxy.
     */
    public static final int SENDER_SIGN = -8;

    /**
     * Это значение шлют клиенты в proxy, чтобы proxy понял, что соединение создается
     * для передачи данных от proxy к клиенту.
     */
    public static final int RECEIVER_SIGN = -9;
}
