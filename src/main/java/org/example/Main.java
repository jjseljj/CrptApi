package org.example;

import java.util.concurrent.TimeUnit;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        // Создаем экземпляр класса CrptApi без указания аутентификации
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);

        // Создаем JSON объект для передачи в теле запроса
        Object document = createDocumentObject();
        String signature = "example_signature";

        // Вызываем метод создания документа через API Честного знака
        crptApi.createDocument(document, signature);
    }

    private static Object createDocumentObject() {

        return new Object();
    }
}
