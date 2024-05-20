package com.alt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AltAPI {
    private static final String BASE_URL = "https://rdb.altlinux.org/api/export/branch_binary_packages/";

    public static String getJsonForPackages(String branch1,String branch2) throws JsonProcessingException {
        Client client = ClientBuilder.newClient();

        JsonArray packagesNodeFirstBranch = fetchPackages(client, branch1);
        if (nullOrEmpty(packagesNodeFirstBranch)) {
            return "Нет пакетов для " + branch1;
        }

        JsonArray packagesNodeSecondBranch = fetchPackages(client, branch2);
        if (nullOrEmpty(packagesNodeSecondBranch)) {
            return "Нет пакетов для " + branch2;
        }

        HashMap<String, AltPackage> hmFirstBranch = new HashMap<>();
        HashMap<String, AltPackage> hmSecondBranch = new HashMap<>();


        for (JsonObject currentPackage : packagesNodeFirstBranch.getValuesAs(JsonObject.class)) {
            String name = currentPackage.getString("name");
            String version = currentPackage.getString("version");
            AltPackage altPackage = new AltPackage(name, version);
            hmFirstBranch.put(name, altPackage);
        }

        for (JsonObject currentPackage : packagesNodeSecondBranch.getValuesAs(JsonObject.class)) {
            String name = currentPackage.getString("name");
            String version = currentPackage.getString("version");
            AltPackage altPackage = new AltPackage(name, version);
            hmSecondBranch.put(name, altPackage);
        }

        HashMap<String, List> hmPackagesOnlyFirstBranch = new HashMap<>();
        HashMap<String, List> hmPackagesOnlySecondBranch = new HashMap<>();
        HashMap<String, List> hmPackagesThird = new HashMap<>();

        if (hmFirstBranch.size() > 0 && hmSecondBranch.size() > 0) {
            ArrayList<AltPackage> lExcessPackages = new ArrayList<>();
            ArrayList<AltPackage> lPackagesOnlyFirst = new ArrayList<>();
            ArrayList<AltPackage> lPackagesOnlySecond = new ArrayList<>();

            for (Map.Entry<String, AltPackage> entrySet : hmFirstBranch.entrySet()) {
                String nameFirstBranch = entrySet.getKey();

                if (!hmSecondBranch.containsKey(nameFirstBranch)) {
                    lPackagesOnlyFirst.add(entrySet.getValue());
                } else {
                    try {
                        if (entrySet.getValue().compareVersion(hmSecondBranch.get(nameFirstBranch).getVersion())) {
                            lExcessPackages.add(entrySet.getValue());
                        } else {
                            lExcessPackages.add(hmSecondBranch.get(nameFirstBranch));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Неверный формат данных для пакета - " + nameFirstBranch);
                        e.printStackTrace();
                    }
                }
            }
            hmPackagesOnlyFirstBranch.put("packages" + upperFirstLetter(branch1), lPackagesOnlyFirst);

            for (Map.Entry<String, AltPackage> entrySet : hmSecondBranch.entrySet()) {
                if (!lExcessPackages.contains(entrySet.getValue()) && hmSecondBranch.containsKey(entrySet.getKey())) {
                    lPackagesOnlySecond.add(entrySet.getValue());
                }
            }

            hmPackagesThird.put("packagesNonInclude", lExcessPackages);
            hmPackagesOnlySecondBranch.put("packages" + upperFirstLetter(branch2), lPackagesOnlySecond);
        }

        LinkedHashMap<String, Object> hmResult = new LinkedHashMap<>();
        hmResult.putAll(hmPackagesOnlyFirstBranch);
        hmResult.putAll(hmPackagesOnlySecondBranch);
        hmResult.putAll(hmPackagesThird);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResult = objectMapper.writeValueAsString(hmResult);

        return jsonResult;
    }



    private static JsonArray fetchPackages(Client client, String branch) {
        String url = BASE_URL.concat(branch);
        WebTarget target = client.target(url);
        Response response = target.request().get();

        if (response.getStatus() != 200) {
            System.out.println("Failed to fetch data for " + branch);
            return null;
        }

        String responseBody = response.readEntity(String.class);
//        String testStr = "{\n" +
//                "  \"packages\": [\n" +
//                "    {\n" +
//                "      \"name\": \"i586-zzuf\",\n" +
//                "      \"epoch\": 0,\n" +
//                "      \"version\": \"0.15\",\n" +
//                "      \"release\": \"alt1_10\",\n" +
//                "      \"arch\": \"x86_64-i586\",\n" +
//                "      \"disttag\": \"sisyphus+225100.100.1.1\",\n" +
//                "      \"buildtime\": 1552687303,\n" +
//                "      \"source\": \"\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"name\": \"package2\",\n" +
//                "      \"epoch\": 1,\n" +
//                "      \"version\": \"2.0\",\n" +
//                "      \"release\": \"beta\",\n" +
//                "      \"arch\": \"x86_64\",\n" +
//                "      \"disttag\": \"disttag2\",\n" +
//                "      \"buildtime\": 1620427303,\n" +
//                "      \"source\": \"source2\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"name\": \"package4\",\n" +
//                "      \"epoch\": 2,\n" +
//                "      \"version\": \"4.0\",\n" +
//                "      \"release\": \"alpha\",\n" +
//                "      \"arch\": \"x86_64\",\n" +
//                "      \"disttag\": \"disttag4\",\n" +
//                "      \"buildtime\": 1620427303,\n" +
//                "      \"source\": \"source4\"\n" +
//                "    }\n" +
//                "  ]\n" +
//                "}\n";
//        String testStr2 = "{\n" +
//                "  \"packages\": [\n" +
//                "    {\n" +
//                "      \"name\": \"i586-zzuf\",\n" +
//                "      \"epoch\": 0,\n" +
//                "      \"version\": \"0.15\",\n" +
//                "      \"release\": \"alt1_10\",\n" +
//                "      \"arch\": \"x86_64-i586\",\n" +
//                "      \"disttag\": \"sisyphus+225100.100.1.1\",\n" +
//                "      \"buildtime\": 1552687303,\n" +
//                "      \"source\": \"\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"name\": \"package3\",\n" +
//                "      \"epoch\": 1,\n" +
//                "      \"version\": \"3.0\",\n" +
//                "      \"release\": \"rc1\",\n" +
//                "      \"arch\": \"x86_64\",\n" +
//                "      \"disttag\": \"disttag3\",\n" +
//                "      \"buildtime\": 1620427303,\n" +
//                "      \"source\": \"source3\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"name\": \"package2\",\n" +
//                "      \"epoch\": 1,\n" +
//                "      \"version\": \"2.0\",\n" +
//                "      \"release\": \"beta\",\n" +
//                "      \"arch\": \"x86_64\",\n" +
//                "      \"disttag\": \"disttag2\",\n" +
//                "      \"buildtime\": 1620427303,\n" +
//                "      \"source\": \"source2\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"name\": \"package5\",\n" +
//                "      \"epoch\": 3,\n" +
//                "      \"version\": \"5.0\",\n" +
//                "      \"release\": \"gamma\",\n" +
//                "      \"arch\": \"x86_64\",\n" +
//                "      \"disttag\": \"disttag5\",\n" +
//                "      \"buildtime\": 1620427303,\n" +
//                "      \"source\": \"source5\"\n" +
//                "    }\n" +
//                "  ]\n" +
//                "}\n";
//        JsonObject jsonObject = null;
//        if (branch.equals("sisyphus")) {
//            jsonObject = parseJson(testStr);
//        } else {
//            jsonObject = parseJson(testStr2);
//        }
        JsonObject jsonObject = parseJson(responseBody);
        return jsonObject.getJsonArray("packages");
    }

    private

    static JsonObject parseJson(String jsonString) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            return jsonReader.readObject();
        }
    }

    public static String upperFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private static boolean nullOrEmpty(JsonArray jsonArray) {
        return jsonArray == null || jsonArray.isEmpty();
    }

    private static boolean nullOrEmpty(Object obj) {
        return obj == null;
    }

    static final class AltPackage {
        private String name;
        private String version;

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public static String removeLettersAndReplaceEmptyWithZero(String input) {
            // Удаляем все буквы, оставляем только цифры и символы плавающей точки
            String result = input.replaceAll("[^0-9.]", "");
            // Если строка пустая, заменяем её на "0"
            if (result.isEmpty()) {
                result = "0";
            }
            return result;
        }

        public AltPackage(String name, String version) {
            version = removeLettersAndReplaceEmptyWithZero(version);
            this.name = name;
            this.version = version;
        }

        public boolean compareVersion(String otherVersion) {
            otherVersion = removeLettersAndReplaceEmptyWithZero(otherVersion);
            String[] thisParts = this.version.split("\\.");
            String[] otherParts = otherVersion.split("\\.");

            // Сравниваем каждую часть версии
            for (int i = 0; i < Math.max(thisParts.length, otherParts.length); i++) {
                int thisPart = (i < thisParts.length) ? Integer.parseInt(thisParts[i]) : 0;
                int otherPart = (i < otherParts.length) ? Integer.parseInt(otherParts[i]) : 0;
                if (thisPart < otherPart) {
                    return false;
                } else if (thisPart > otherPart) {
                    return true;
                }
            }

            return true;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            AltPackage that = (AltPackage) object;
            return Objects.equals(name, that.name) && Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, version);
        }

        public String toJson() {
            return "{\"name\":\"" + name + "\",\"version\":\"" + version + "\"}";
        }
    }
}
