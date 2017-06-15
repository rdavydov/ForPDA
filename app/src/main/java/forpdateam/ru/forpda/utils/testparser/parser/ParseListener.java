package forpdateam.ru.forpda.utils.testparser.parser;

public interface ParseListener {
    void characters(String string);
    void start(Block block);
    void end(Block block);
}
