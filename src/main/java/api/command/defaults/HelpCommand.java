package api.command.defaults;

import api.Bot;
import api.command.Command;
import api.storage.User;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "<math>", "просмотреть информацию");
        setAlias("info", "?");
    }

    @Override
    public final void execute(@NonNls User sender, @NotNull String[] args) {
        if (args.length == 1) {
            sender.sendMessageFromChat(Bot.getHelpCommand().getHelpCommand());
        } else if (args.length == 2) {
            if(args[1].equalsIgnoreCase("math")) {
                sender.sendMessageFromChat(Bot.getHelpCommand().getHelpMath());
            }
        } else {
            sender.sendMessageFromChat("Использование: " + getUseCommand());
        }
    }
}
