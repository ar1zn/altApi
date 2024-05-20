package com.alt;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws JsonProcessingException {
        // Создаем карту соответствия вариантов выбора и соответствующих им значений
        Map<String, String> options = new HashMap<>();
        options.put("1", "sisyphus");
        options.put("2", "sisyphus_e2k");
        options.put("3", "sisyphus_riscv64");
        options.put("4", "sisyphus_loongarch64");
        options.put("5", "p10");
        options.put("6", "p10_e2k");
        options.put("7", "p9");
        options.put("8", "p9_e2k");
        options.put("9", "p8");
        options.put("10", "c10f1");
        options.put("11", "c9f2");
        options.put("12", "c7");

        // Выводим меню
        System.out.println("Выберите два пакета (укажите номера через запятую):");
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ": " + options.get(Integer.toString(i + 1)));
        }

        // Считываем выбор пользователя
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите номера выбранных пакетов: ");
        String[] choices = scanner.nextLine().split(",");

        // Проверяем, есть ли такие выборы в списке
        for (String choice : choices) {
            if (!options.containsKey(choice.trim())) {
                System.out.println("Неверный выбор: " + choice.trim());
                return;
            }
        }

        // Проверяем, что выбрано ровно 2 пакета
        if (choices.length != 2) {
            System.out.println("Выберите ровно два пакета.");
            return;
        }

        // Выводим выбранные пакеты
        System.out.println("Вы выбрали следующие пакеты:");
        String branch1=  options.get(choices[0].trim());
        String branch2 = options.get(choices[1].trim());
        System.out.println(AltAPI.getJsonForPackages(branch1,branch2));
    }
}
