import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class HTMLTemplate implements Cloneable {
    private File file;
    private String content;
    
    public HTMLTemplate(String fileName) {
        file = new File(fileName);
        try {
            StringBuilder stringBuilder = new StringBuilder();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
                stringBuilder.append("\n");
            }
            scanner.close();
            content = stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void format(HashMap<String, String> formatMap) {
        StringBuilder contentBuilder = new StringBuilder(content);
        int cursor = 0;
        while (cursor < contentBuilder.length()) {
            int start = contentBuilder.indexOf("{{", cursor);
            if (start == -1) break;
            int end = contentBuilder.indexOf("}}", start);
            if (end == -1) break;
            String key = contentBuilder.substring(start + 2, end).replace(" ", "");
            String value = formatMap.get(key);
            if (value == null) {
                cursor = end + 2;
                continue;
            }
            contentBuilder.replace(start, end + 2, value);
            cursor = start + value.length();
        }
        content = contentBuilder.toString();
    }

    public void format(String key, String value) {
        StringBuilder contentBuilder = new StringBuilder(content);
        int cursor = 0;
        while (cursor < contentBuilder.length()) {
            int start = contentBuilder.indexOf("{{", cursor);
            if (start == -1) break;
            int end = contentBuilder.indexOf("}}", start);
            if (end == -1) break;
            String extractedKey = contentBuilder.substring(start + 2, end).trim();
            if (!extractedKey.equals(key)) {
                cursor = end + 2;
                continue;
            }
            contentBuilder.replace(start, end + 2, value);
            cursor = start + value.length();
        }
        content = contentBuilder.toString();
    }

    public HTMLTemplate formatted(HashMap<String, String> formatMap) {
        HTMLTemplate returnTemplate;
        returnTemplate = (HTMLTemplate) this.clone();
        returnTemplate.format(formatMap);
        return returnTemplate;
    }

    public HTMLTemplate formatted(String key, String value) {
        HTMLTemplate returnTemplate;
        returnTemplate = (HTMLTemplate) this.clone();
        returnTemplate.format(key, value);
        return returnTemplate;
    }

    public String toString() {
        return content;
    }

    @Override
    public HTMLTemplate clone() {
        try {
            return (HTMLTemplate) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
