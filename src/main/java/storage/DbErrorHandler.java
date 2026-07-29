package storage;

import java.sql.SQLException;

public final class DbErrorHandler {

    private DbErrorHandler() {}

    public static String translate(SQLException e) {
        String state = e.getSQLState();
        if (state == null) state = "";

        if (state.startsWith("08") || e.getMessage().contains("Connection refused")) {
            return "Ошибка подключения к базе данных.\nПроверьте, запущен ли PostgreSQL и корректны ли данные в db.properties.";
        }

        if ("23505".equals(state)) {
            return "Ошибка: такая запись уже существует.\nПроверьте уникальность логина, ID или других полей.";
        }

        if ("23503".equals(state)) {
            return "Ошибка: запись ссылается на несуществующий объект.\nПроверьте, что прибор/пользователь с таким ID действительно создан.";
        }

        if (state.startsWith("22")) {
            return "Ошибка формата данных.\nПроверьте корректность введённых значений (даты, числа, тексты).";
        }

        if (state.startsWith("42")) {
            return "Ошибка структуры базы данных.\nТаблица не найдена. Выполните миграцию schema.sql или проверьте currentSchema.";
        }

        String msg = e.getMessage();
        return "Ошибка базы данных:\n" + (msg.length() > 150 ? msg.substring(0, 150) + "..." : msg);
    }
}