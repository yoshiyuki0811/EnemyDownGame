package plugin.enemyDown.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import plugin.enemyDown.Date.PlayerScore;
import plugin.enemyDown.Main;

/**
 * 制限時間内にランダムで出現する敵をたおして、スコアを取得するゲームを起動するコマンド
 * スコアはてきによって変わり、倒せた敵の合計によってスコアが変動します。
 * 結果がプレイヤー名、点数、日時等で保存されます。
 */
public class EnemyDownCommand extends BaseCommand implements Listener {

  public static final int GAME_TIME = 20;
  
  private Main main;
  private List<PlayerScore> playerScoreList = new ArrayList<>();
  private List<Entity> spawnEntityList = new ArrayList<>();


  public EnemyDownCommand(Main main) {
    this.main = main;
  }
  @Override
  public boolean onExecutePlayerCommand(Player player) {
    PlayerScore nowPlayerScore =getPlayerScore(player);
    
    initPlayerStatus(player);

    gamePlay(player, nowPlayerScore);
    return true;
  }

  

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender) {

    return false;
  }
  /**
   * 新規のプレイヤーを情報をリストに追加します。
   * @param player　プレイヤー情報
   * @return 新規プレイヤー
   */
  private PlayerScore addNewPlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore(player.getName());
    playerScoreList.add(newPlayer);
    return newPlayer;
  }

  @EventHandler
  public void onEnemyDeath(EntityDeathEvent e) {
    LivingEntity enemy = e.getEntity();
    Player player = enemy.getKiller();

    if (Objects.isNull(player) || playerScoreList.isEmpty()) {
      return;
    }
    for (PlayerScore playerScore : playerScoreList){
      if (playerScore.getPlayerName().equals(player.getName())){
        int point = switch (enemy.getType()) {
          case ZOMBIE -> 10;
          case SKELETON, WITCH -> 20;
          default -> 0;
        };

        playerScore.setScore(playerScore.getScore() + point);

        player.sendMessage("敵を倒した！現在のスコアは" +playerScore.getScore() +"点！");
      }
    }
  }
  /**
   * 現在実行しているプレイヤーんスコア情報を取得する。
   *
   * @param player　コマンドを実行したプレイヤー
   * return　現在実行しているプレイヤーのスコア情報
   */
  private PlayerScore getPlayerScore(Player player) {
    PlayerScore playerScore = new PlayerScore(player.getName());

    if (playerScoreList.isEmpty()){
      playerScore  = addNewPlayer(player);
    }else{
      playerScore = playerScoreList.stream().findFirst().map(ps
          -> ps.getPlayerName().equals(player.getName())
          ? ps
          : addNewPlayer(player)).orElse(playerScore);
      playerScore.setGameTime(GAME_TIME);
    }
    playerScore.setGameTime(GAME_TIME);
    playerScore.setScore(0);
    return playerScore;
  }
  /**
   * ゲームを始める前にプレイヤーの状態を設定する。
   * 体力と空腹度を最大にして、装備をネザライト一式にする。
   * @param player　コマンドを実行したプレイヤー
   *
   */

  private void initPlayerStatus(Player player) {
    player.setHealth(20);
    player.setFoodLevel(20);
    PlayerInventory inventory = player.getInventory();
    inventory.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
    inventory.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
    inventory.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
    inventory.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
    inventory.setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
  }

  /**
   * ゲームを実行します。規定の時間内に敵を倒すとスコアが加算されます。合計スコアを時間経過後に表示します。
   * @param player　コマンドを実行したプレイヤー
   * @param nowPlayerScore　プレイヤーのスコア情報
   */
  private void gamePlay(Player player, PlayerScore nowPlayerScore) {
    Bukkit.getScheduler().runTaskTimer(main, Runnable ->{
      if (nowPlayerScore.getGameTime()<= 0){
        Runnable.cancel();

        player.sendTitle("ゲームが終了しました。",
            nowPlayerScore.getPlayerName() + "合計"+ nowPlayerScore.getScore() +"点！",
            0,60,0);

        spawnEntityList.forEach(Entity::remove);
        return;
      }

      Entity spawnEntity = player.getWorld().spawnEntity(getEnemySpawnLocation(player), getEnemy());
      spawnEntityList.add(spawnEntity);
      nowPlayerScore.setGameTime(nowPlayerScore.getGameTime() -5);
    },0,5 * 20);
  }
  /**
   * 敵の出現エリアを取得します。
   * 出現エリアはX軸とZ軸の位置からプラス、ランダムで-１０～９の値が設定されます。
   *
   * @param player　コマンドを実行したプレイヤー
   * @param 　コマンドを実行したプレイヤーの所属するワールド
   *
   *
   */
  private Location getEnemySpawnLocation(Player player) {
    Location playerlocation = player.getLocation();
    int randomX = new SplittableRandom().nextInt(20)-10;
    int randomZ = new SplittableRandom().nextInt(20)-10;

    double x = playerlocation.getX()+randomX;
    double y = playerlocation.getY();
    double z = playerlocation.getZ()+randomZ;

    return new Location(player.getWorld(),x, y, z );
  }
  /**
   * ランダムで敵を抽出して、その結果の敵を取得します。
   * return　敵
   */
  private  EntityType getEnemy() {
    List<EntityType> enemyList = List.of(EntityType.ZOMBIE,EntityType.SKELETON,EntityType.WITCH);
    return enemyList.get(new SplittableRandom().nextInt(enemyList.size()));
  }
}
