package jolyjdia.bot.newcalculator.internal.lexer.tokens;

import jolyjdia.bot.newcalculator.internal.Identifiable;
import org.jetbrains.annotations.Contract;

public abstract class Token implements Identifiable {

    private final int position;

    @Contract(pure = true)
    protected Token(int position) {
        this.position = position;
    }

    @Contract(pure = true)
    @Override
    public final int getPosition() {
        return position;
    }

}