package forpdateam.ru.forpda.utils.testparser.html;

import java.util.Map;

import forpdateam.ru.forpda.utils.testparser.parser.Block;

class HtmlBlock implements Block {

    private final String name;
    private final Map<String, String> attributes;

    HtmlBlock(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
