import java.util.LinkedList;
import java.util.List;

public class CommandHistory {
    private final List<String> history;
    private final int maxSize;

    public CommandHistory(int maxSize) {
        this.maxSize = maxSize;
        this.history = new LinkedList<>();
    }

    /**
     * Добавляет команду в историю.
     * Если размер превышает лимит, удаляется самая старая команда.
     */
    public void add(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }
        history.add(command.trim());
        if (history.size() > maxSize) {
            history.remove(0); // Удаляем первую (самую старую)
        }
    }

    /**
     * Возвращает копию истории для безопасного использования.
     */
    public List<String> getHistory() {
        return new LinkedList<>(history);
    }

    /**
     * Очищает историю.
     */
    public void clear() {
        history.clear();
    }

    /**
     * Возвращает текущее количество команд в истории.
     */
    public int size() {
        return history.size();
    }
}
