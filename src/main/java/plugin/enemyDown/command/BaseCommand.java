package plugin.enemyDown.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * コマンドを実行して動かすプラグイン処理の基底クラスです。
 */
public abstract class BaseCommand implements CommandExecutor {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (sender instanceof Player player ) {


       return onExecutePlayerCommand(player, command ,label ,args);
  }else{
      return onExecuteNPCCommand(sender, command ,label ,args);
    }
  }

  /**
   *コマンド実行者がプレイヤーだった時に実行します。
   * @param player　コマンドを実行したプレイヤー
   * @param   command コマンド
   * @param label ラベル
   * @param args コマンド引き数
   * @return 処理の実行の有無
   */
  public abstract boolean  onExecutePlayerCommand(Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args);

  /**
   * コマンド実行者がプレイヤーだった場合に実行します。
   *
   * @param sender　コマンド実行者
   * @return 処理の実行
   */
  public abstract boolean onExecuteNPCCommand(CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);


}
