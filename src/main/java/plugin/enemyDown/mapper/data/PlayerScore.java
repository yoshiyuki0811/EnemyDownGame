package plugin.enemyDown.mapper.data;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * プレイヤーのスコア情報を扱うオブジェクト。
 * DBに存在するテーブルと連動する。
 *
 */
@Setter
@Getter
@NoArgsConstructor
public class PlayerScore {

  private int id;
  private String playerName;
  private int score ;
  private String difficulty;
  private LocalDateTime registeredAT;



  public PlayerScore(String playerName,int score,String difficulty){
    this.playerName = playerName;
    this.score =score;
    this.difficulty=difficulty;

  }


}
