package ru.innova.task.common;

/**
 * <p>
 * Интерфейс, через который контролируется соответствие отправляемых и поступаемых данных.
 * </p>
 * 
 * <p>
 * Одни объекты сообщают данному интерфейсу, что они сгенерировали новое значение,
 * другие объекты сообщают, что они получили значение, а сам объект DataController
 * отвечает на вопрос, все ли сгенерированные значения были получены.
 * </p>
 * 
 * <p>
 * Предполагается, что данные генерируются по порядку и не повторяются. Поэтому
 * достаточно хранить только самое большее значение отправленного и полученного объектов. 
 * </p>
 * 
 * @author sergey
 */
public interface DataController {

    /**
     * Зафиксировать полученное значение.
     * 
     * @param value полученное значение
     */
    void saved(Integer value);

    /**
     * Зафиксировать сгенерированное значение.
     * 
     * @param value сгенерированное значение
     */
    void produced(Integer value);
    
    /**
     * Узнать, все ли сгенерированные значения сохраненны.
     * 
     * @return <code>true</code>, если самое большое сохраненное значение
     *     не меньше самого большого сгенерированного значения.
     */
    boolean areAllSaved();
    
}
