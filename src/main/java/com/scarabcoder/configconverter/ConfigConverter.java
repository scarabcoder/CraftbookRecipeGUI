package com.scarabcoder.configconverter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ConfigConverter extends JavaPlugin {

    public void onEnable(){
        FileConfiguration craftbook = YamlConfiguration.loadConfiguration(new File(Bukkit.getPluginManager().getPlugin("CraftBook").getDataFolder(), "crafting-recipes.yml"));
        for(String rec : craftbook.getConfigurationSection("crafting-recipes").getKeys(false)) {
            ConfigurationSection recSec = craftbook.getConfigurationSection("crafting-recipes." + rec);
            if(!recSec.getString("type").equals("Shaped"))
                continue;
            String name = rec;
            if (!this.getDataFolder().exists())
                this.getDataFolder().mkdir();
            File f = new File(this.getDataFolder(), name + ".yml");
            if (!f.exists())
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            ConfigurationSection ingredients = recSec.getConfigurationSection("ingredients");
            List<String> shape = recSec.getStringList("shape");

            HashMap<Character, ItemStack> ingrds = new HashMap<>();
            for (String str : ingredients.getKeys(false)) {
                char key = ingredients.getString(str).charAt(0);
                String[] i = str.split("\\|");
                ItemStack is;
                if (i[0].contains(":")) {
                    is = new ItemStack(Material.valueOf(i[0].split(":")[0]));
                    is.setDurability(Short.valueOf(i[0].split(":")[1].replace(" ", "")));
                } else {
                    is = new ItemStack(Material.valueOf(i[0]));
                }
                ItemMeta im = is.getItemMeta();
                if(i.length == 2)
                    im.setDisplayName(i[1]);
                is.setItemMeta(im);
                System.out.println(key);
                ingrds.put(key, is);
            }

            String res[] = recSec.getConfigurationSection("results").getKeys(false).iterator().next().split("\\|");

            FileConfiguration fc = YamlConfiguration.loadConfiguration(f);

            ConfigurationSection recipe = fc.createSection("craftingresult");
            if(res.length > 1)
                recipe.set("NAME", res[1].split("/")[0]);
            recipe.set("ID", res[0].split(";")[0]);
            recipe.set("POSITION-X", 7);
            recipe.set("POSITION-Y", 2);

            int x = 0;
            int row = 1;
            for (String str : shape) {
                int column = 3;
                for (char c : str.toCharArray()) {
                    if(c != ' ') {
                        System.out.println(c);
                        ItemStack s = ingrds.get(c);
                        ConfigurationSection cs = fc.createSection("ingredient" + x);
                        cs.set("ID", s.getType() + ":" + s.getDurability());
                        cs.set("POSITION-X", column);
                        cs.set("POSITION-Y", row);
                    }
                    x++;
                    column++;
                }
                row++;
            }
            try {
                fc.save(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
