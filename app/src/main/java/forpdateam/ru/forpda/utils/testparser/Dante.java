package forpdateam.ru.forpda.utils.testparser;


import android.text.SpannableStringBuilder;
import android.text.Spanned;

import java.util.ArrayList;

import forpdateam.ru.forpda.utils.testparser.parser.Block;
import forpdateam.ru.forpda.utils.testparser.parser.BlockListener;
import forpdateam.ru.forpda.utils.testparser.parser.ParseListener;
import forpdateam.ru.forpda.utils.testparser.parser.Parser;

public class Dante implements ParseListener {
    private final Parser parser;
    private final ArrayList<BlockListener> listeners;
    private SpannableStringBuilder builder;

    public Dante(Parser parser) {
        this.parser = parser;
        this.listeners = new ArrayList<>();
        parser.register(this);
    }

    public Spanned parse(String string) {
        builder = new SpannableStringBuilder();
        parser.parse(string);

        return builder;
    }

    @Override public void characters(String string) {
        builder.append(string);
    }

    @Override public void start(Block block) {
        for (BlockListener listener: listeners) {
            if (listener.match(block)) {
                listener.start(block, builder);
            }
        }
    }

    @Override public void end(Block block) {
        for (BlockListener listener: listeners) {
            if (listener.match(block)) {
                listener.end(block, builder);
            }
        }
    }

    public void register(BlockListener listener) {
        this.listeners.add(listener);
    }
}

