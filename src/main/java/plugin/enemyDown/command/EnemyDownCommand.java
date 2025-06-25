package plugin.enemyDown.command;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
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
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import plugin.enemyDown.PlayerScoreData;
import plugin.enemyDown.date.ExecutingPlayer;
import plugin.enemyDown.Main;
import plugin.enemyDown.mapper.data.PlayerScore;

/**
 * 制限時間内にランダムで出現する敵をたおして、スコアを取得するゲームを起動するコマンド
 * スコアはてきによって変わり、倒せた敵の合計によってスコアが変動します。
 * 結果がプレイヤー名、点数、日時等で保存されます。
 */
public class EnemyDownCommand extends BaseCommand implements Listener {

  public static final int GAME_TIME = 20;

  public static final String EASY = "easy";
  public static final String NORMAL = "normal";
  public static final String HARD = "hard";
  public static final String NONE = "none";
  public static final String LIST= "list";

  private final Main main;

  private final PlayerScoreData playerScoreData =new PlayerScoreData();


  private final List<ExecutingPlayer> executingPlayerList = new ArrayList<>();
  private final List<Entity> spawnEntityList = new ArrayList<>();



  public EnemyDownCommand(Main main) {

    this.main= main;

  }
  @Override
  public boolean onExecutePlayerCommand(Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length ==1  && LIST.equals(args[0])) {
      sendPlayerScoreList(player);

      return false;
      }
    String difficulty = getDifficulty(player, args);
if (difficulty.equals(NONE)){
  return false;
}
    ExecutingPlayer nowExecutingPlayer =getPlayerScore(player);
    
    initPlayerStatus(player);

    gamePlay(player, nowExecutingPlayer, difficulty);
    return true;
  }

  /**
   * 難易度をコマンド引数から実行します
   *
   * @param player　コマンドを実行したプレイヤー
   * @param args　コマンド引数
   * @return 難易度
   */
  private String getDifficulty(Player player,String[] args) {
    if (args.length ==1  && (EASY.equals(args[0]) || NORMAL.equals(args[0]) ||HARD.equals(args[0]))) {
     return args[0];
    }
    player.sendMessage(ChatColor.RED +"実行できません。コマンド引数の一つ目に難易度指定必要です「easy,normal,hard」");
    return NONE;
  }


  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

    return false;
  }
  /**
   * 新規のプレイヤーを情報をリストに追加します。
   * @param player　プレイヤー情報
   * @return 新規プレイヤー
   */
  private ExecutingPlayer addNewPlayer(Player player) {
    ExecutingPlayer newPlayer = new ExecutingPlayer(player.getName());
    executingPlayerList.add(newPlayer);
    return newPlayer;
  }
  /**
   * 現在登録されているスコアの一覧をメッセージに送る。
   *
   * @param player　プレイヤー
   */

  private void sendPlayerScoreList(Player player) {
    List<PlayerScore> playerScoreList = playerScoreData.selectList();
    for (PlayerScore playerScore : playerScoreList) {
      player.sendMessage(playerScore.getId() + " : "
          + playerScore.getPlayerName() + " : "
          + playerScore.getScore() + " : "
          + playerScore.getDifficulty() + " : "
          + playerScore.getRegisteredAT().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
  }

  @EventHandler
  public void onEnemyDeath(EntityDeathEvent e) {
    LivingEntity enemy = e.getEntity();
    Player player = enemy.getKiller();

    if (Objects.isNull(player) || spawnEntityList.stream().noneMatch(entity ->entity.equals(enemy))) {
      return;
    }
   executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .findFirst()
        .ifPresent(p ->{
          int point = switch (enemy.getType()) {
            case ZOMBIE -> 10;
            case SKELETON, WITCH -> 20;
            default -> 0;
          };
          p.setScore(p.getScore() + point);
          player.sendMessage("敵を倒した！現在のスコアは" +p.getScore() +"点！");
        });

  }
  /**
   * 現在実行しているプレイヤーんスコア情報を取得する。
   *
   * @param player　コマンドを実行したプレイヤー
   * return　現在実行しているプレイヤーのスコア情報
   */
  private ExecutingPlayer getPlayerScore(Player player) {
    ExecutingPlayer executingPlayer = new ExecutingPlayer(player.getName());

    if (executingPlayerList.isEmpty()){
      executingPlayer = addNewPlayer(player);
    }else{
      executingPlayer = executingPlayerList.stream().findFirst().map(ps
          -> ps.getPlayerName().equals(player.getName())
          ? ps
          : addNewPlayer(player)).orElse(executingPlayer);
      executingPlayer.setGameTime(GAME_TIME);
    }
    executingPlayer.setGameTime(GAME_TIME);
    executingPlayer.setScore(0);
    removePotionEffect(player);
    return executingPlayer;
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
   * @param difficulty 難易度
   * @param nowExecutingPlayer　プレイヤーのスコア情報
   */
  private void gamePlay(Player player, ExecutingPlayer nowExecutingPlayer,String difficulty) {
    Bukkit.getScheduler().runTaskTimer(main, Runnable ->{
      if (nowExecutingPlayer.getGameTime()<= 0){
        Runnable.cancel();

        player.sendTitle("ゲームが終了しました。",
            nowExecutingPlayer.getPlayerName() + "合計"+ nowExecutingPlayer.getScore() +"点！",
            0,60,0);


        spawnEntityList.forEach(Entity::remove);
        spawnEntityList.clear();


        removePotionEffect(player);

        playerScoreData.insert(
            new PlayerScore(nowExecutingPlayer.getPlayerName()
            , nowExecutingPlayer.getScore()
            ,difficulty));

        return;
      }

      Entity spawnEntity = player.getWorld().spawnEntity(getEnemySpawnLocation(player), getEnemy(difficulty));
      spawnEntityList.add(spawnEntity);
      nowExecutingPlayer.setGameTime(nowExecutingPlayer.getGameTime() -5);
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
   *
   * @param  difficulty 難易度
   * return　敵
   */
  private  EntityType getEnemy(String difficulty) {
    List<EntityType> enemyList;
    switch (difficulty) {
      case NORMAL -> enemyList = List.of(EntityType.ZOMBIE, EntityType.SKELETON);
      case HARD -> enemyList = List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITCH);
      case null, default -> enemyList = List.of(EntityType.ZOMBIE);
    }
    return enemyList.get(new SplittableRandom().nextInt(enemyList.size()));
  }

  /**
   * プレイヤーに設定されている特殊効果を除外します。
   *
   * @param player　コマンドを実行したプレイヤー
   */
  private  void removePotionEffect(Player player) {
    player.getActivePotionEffects().stream()
        .map(PotionEffect::getType)
        .forEach(player::removePotionEffect);
  }
}
