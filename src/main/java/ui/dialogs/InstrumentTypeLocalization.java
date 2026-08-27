package ui.dialogs;

import domain.Instrument;
import enums.InstrumentType;

public final class InstrumentTypeLocalization {

    private InstrumentTypeLocalization() {}

    public static String getRussianName(InstrumentType type) {
        if (type == null) return "Неизвестный прибор";
        return switch (type) {
            case BEAKER -> "Химический стакан";
            case MICROSCOPE -> "Микроскоп";
            case SCALE -> "Лабораторные весы";
            case PETRI_DISH -> "Чашка Петри";
            case SPIRIT_LAMP -> "Спиртовка";
            case GLOVES -> "Защитные перчатки";
            case TONG -> "Лабораторные щипцы";
            case FUNNEL -> "Воронка";
            case THERMOMETER -> "Термометр";
            case PIPETTE -> "Пипетка";
        };
    }

    public static String formatInstrument(Instrument i) {
        if (i == null) return "-";
        return String.format("[#%d] %s (%s)", i.getId(), getRussianName(i.getType()), i.getType());
    }
}
