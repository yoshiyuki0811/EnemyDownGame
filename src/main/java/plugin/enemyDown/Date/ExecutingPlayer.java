package plugin.enemyDown.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * EnemyDownのゲームを実行する際のプレイヤー情報を扱うプロジェクト
 * プレイヤー名、合計点
 *
 */
@Getter
@Setter
public class ExecutingPlayer {

  private String playerName;
  private int  score;
 private  int gameTime;

  public ExecutingPlayer(String PlayerName) {
    this.playerName = PlayerName;
  }
}
