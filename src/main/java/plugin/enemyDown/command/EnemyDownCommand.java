package plugin.enemyDown.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import plugin.enemyDown.Date.PlayerScore;
import plugin.enemyDown.Main;

public class EnemyDownCommand implements CommandExecutor ,Listener{


  private List<PlayerScore> playerScoreList = new ArrayList<>();


  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player ) {
      if (playerScoreList.isEmpty()){
        addNewPlayer(player);

      }else{
        for (PlayerScore playerScore : playerScoreList){
          if (!playerScore.getPlayerName().equals(player.getName())){
            addNewPlayer(player);
          }
        }
      }
      World world = player.getWorld();
      //プレイヤーの状態を初期化する。（体力と空腹度を最大値にする。）
      initPlayerStatus(player);

      List<EntityType> enemyList = List.of(EntityType.ZOMBIE,EntityType.SKELETON);
    int random = new SplittableRandom().nextInt(2);



    world.spawnEntity(getEnemySpawnLocation(player, world), enemyList.get(random));
    }
    return false;
  }/**
   * 新規のプレイヤーを情報をリストに追加します。
   * @param player　プレイヤー情報
   *
   */
  private void addNewPlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore();
    newPlayer.setPlayerName(player.getName());
    playerScoreList.add(newPlayer);
  }

  @EventHandler
  public void onEnemyDeath(EntityDeathEvent e) {
    Player player = e.getEntity().getKiller();

    if (Objects.isNull(player) || playerScoreList.isEmpty()) {
      return;
    }
    for (PlayerScore playerScore : playerScoreList){
      if (playerScore.getPlayerName().equals(player.getName())){
        playerScore.setScore(playerScore.getScore() +10);
        player.sendMessage("敵を倒した！現在のスコアは" +playerScore.getScore() +"点！");
      }
    }
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
   * 敵の出現エリアを取得します。
   * 出現エリアはX軸とZ軸の位置からプラス、ランダムで-１０～９の値が設定されます。
   *
   * @param player　コマンドを実行したプレイヤー
   * @param world　コマンドを実行したプレイヤーの所属するワールド
   *
   *
   */
  private Location getEnemySpawnLocation(Player player, World world) {
    Location playerlocation = player.getLocation();
    int randomX = new SplittableRandom().nextInt(20)-10;
    int randomZ = new SplittableRandom().nextInt(20)-10;

    double x = playerlocation.getX()+randomX;
    double y = playerlocation.getY();
    double z = playerlocation.getZ()+randomZ;

    return new Location(world,x, y, z );
  }
}
