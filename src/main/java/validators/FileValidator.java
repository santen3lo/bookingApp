package validators;

import exceptions.StartAfterEndException;
import org.w3c.dom.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import domain.*;

public class FileValidator {
    public void checkFileAccessible(Path path) {
        if (!Files.exists(path)) throw new IllegalArgumentException("Ошибка загрузки: файл не найден: " + path);
        if (!Files.isReadable(path)) throw new IllegalArgumentException("Ошибка загрузки: нет прав на чтение файла");
    }

    public void checkXmlStructure(Document doc) {
        Element root = doc.getDocumentElement();
        if (!"LabData".equals(root.getNodeName())) {
            throw new IllegalArgumentException("Ошибка загрузки: неверный корневой элемент XML (ожидается LabData)");
        }
    }

    public void validateDataIntegrity(List<Booking> bookings, List<Checkout> checkouts, List<Instrument> instruments) {
        // 1. Уникальность ID
        checkUniqueIds(bookings.stream().map(Booking::getId).collect(Collectors.toList()), "Booking");
        checkUniqueIds(checkouts.stream().map(Checkout::getId).collect(Collectors.toList()), "Checkout");
        checkUniqueIds(instruments.stream().map(Instrument::getId).toList(), "Instrument");

        for (Instrument i : instruments) {
            if (i.getType() == null) {
                throw new IllegalArgumentException("Ошибка загрузки: у Instrument id=" + i.getId() + " отсутствует type");
            }
        }

        // 2. Обязательные поля и ограничения
        for (Booking b : bookings) {
            if (b.getStartAt() == null || b.getEndAt() == null){
                throw new IllegalArgumentException("Ошибка загрузки: у Booking id=" + b.getId() + " отсутствуют даты");
            }
            if (b.getEndAt().isBefore(b.getStartAt())){
                throw new StartAfterEndException("Начало после конца у booking id="+b.getId());
            }
            if (b.getOwnerUserId() <= 0){
                throw new IllegalArgumentException("Ошибка загрузки: пустой ownerUsername у Booking id=" + b.getId());
            }
        }
        for (Checkout c : checkouts) {
            if (c.getUserId() <= 0) {
                throw new IllegalArgumentException("Ошибка загрузки: пустой username у Checkout id=" + c.getId());
            }
            if (c.getTakenAt() == null){
                throw new IllegalArgumentException("Ошибка загрузки: отсутствует takenAt у Checkout id=" + c.getId());
            }
        }
    }

    private void checkUniqueIds(List<Long> ids, String entityName) {
        Set<Long> seen = new HashSet<>();
        for (Long id : ids) {
            if (!seen.add(id)) throw new IllegalArgumentException("Ошибка загрузки: дублирующийся id=" + id + " в " + entityName);
        }
    }
}