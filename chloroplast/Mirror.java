package chloroplast;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Mirror extends JavaPlugin implements Listener{
	
	ArrayList<Task> tasks = new ArrayList<Task>(10);
	
	public Task getTask(Player player){
		for(Task task:tasks){
			if(task.player.equals(player)){
				return task;
			}
		}
		return null;
	}
	
	
	@Override
	public void onDisable() {
		
	}
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this,  this);	
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage("this command can only be run by a player");
			return false;
		}
		
		if(!cmd.getName().equalsIgnoreCase("mirror")){
			return false;
		}
		Player player = (Player)sender;
		Task task = getTask(player);
		
		if(task == null){
			
			if(args.length==0){
				sender.sendMessage("to few arguments: mirror XY/YZ/XZ (x y z (<world>))");
				return false;
			}
			if(args.length>5){
				sender.sendMessage("to much arguments: mirror XY/YZ/XZ (x y z (<world>))");
				return false;
			}
			
			if(args.length==2 || args.length==3){
				sender.sendMessage("wrong arguments: mirror XY/YZ/XZ (x y z (<world>))");
				return false;
			}
			
			
			int direction;
			if(args[0].equalsIgnoreCase("xy")){
				direction = Task.XY;
			}else if(args[0].equalsIgnoreCase("yz")){
				direction = Task.YZ;
			}else if(args[0].equalsIgnoreCase("xz")){
				direction = Task.XZ;
			}else{
				sender.sendMessage("not known direction. allowed are XY, YZ or XZ");
				return false;
			}
			
			if(args.length == 1){
				
				tasks.add(new Task(player.getWorld(), player, direction));
			}else if(args.length == 4){
				
				tasks.add(new Task(player.getWorld(), direction, player, Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])));
			}else if(args.length == 5){
				World world = getServer().getWorld(args[4]);
				
				if(world == null){
					sender.sendMessage("world name "+args[4]+" not existing");
				}
				
				tasks.add(new Task(world, direction, player, Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])));
			}
			sender.sendMessage("added mirror task");
			
			
		}else{
			tasks.remove(task);
			sender.sendMessage("removed mirror task");
		}
		return true;
		
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		Task task = getTask(event.getPlayer());
		if(task == null){
			return;
		}
		if(!event.getPlayer().getWorld().equals(task.world)){
			return;
		}
		Position pos = task.getOppositePosition(event.getBlockPlaced().getX(), event.getBlockPlaced().getY(), event.getBlockPlaced().getZ());
		Block block = task.world.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
		block.setType(event.getBlockPlaced().getType());
		block.getState().update();
		
	}
	
	@EventHandler
	public void onBlockDestroy(BlockBreakEvent event){
		Task task = getTask(event.getPlayer());
		if(task == null){
			return;
		}
		if(!event.getPlayer().getWorld().equals(task.world)){
			return;
		}
		Position pos = task.getOppositePosition(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
		Block block = task.world.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
		block.setType(Material.AIR);
		block.getState().update();
		
	}
	
	class Position{
		public double x,y,z;
		
		public Position(double x, double y, double z){
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public int getX(){
			return (int)(x);
		}
		public int getY(){
			return (int)(y);
		}
		public int getZ(){
			return (int)(z);
		}
	}
	
	private class Task{
		
		Player player;
		
		Position origin;
		World world;
		
		public final static int XY = 0;
		public final static int YZ = 1;
		public final static int XZ = 2;
		
		int direction;
		
		
		public Task(World world, int direction, Player player, double x, double y, double z){
			origin = new Position(x, y, z);
			this.world = world;
			this.direction = direction;
			this.player = player;
		}
		
		public Task(World world, Player player, int direction){
			this(world, direction, player, player.getLocation().getX()-0.5, player.getLocation().getY()-0.5,  player.getLocation().getZ()-0.5);
		}
		
		public Position getOppositePosition(int x, int y, int z){
			if(direction == XY){
				return new Position(x, y, (2*origin.z)-z);
			}
			if(direction == YZ){
				return new Position((2*origin.x)-x, y, z);
			}
			if(direction == XZ){
				return new Position(x, (2*origin.y)-y, z);
			}
			
			throw new RuntimeException("Unknown direction");
		}
		
	}
	
}
