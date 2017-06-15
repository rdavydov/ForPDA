package forpdateam.ru.forpda.utils.testparser.parser;

public interface Parser {
    void parse(String string);
    void register(ParseListener listener);
}

