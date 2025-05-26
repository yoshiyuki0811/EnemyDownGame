package plugin.enemyDown.Date;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

/**
 * EnemyDownのゲームを実行する際のスコア情報を扱うプロジェクト
 * プレイヤー名、合計点
 *
 */
@Getter
@Setter
public class PlayerScore {

  private String playerName;
  private int  score;
 private  int gameTime;

}
