package com.elmakers.mine.bukkit.heroes;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillRegistrar;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class MagicHeroesPlugin extends JavaPlugin {
    private MagicAPI magicAPI = null;
    private Heroes heroes = null;

    @Override
    public void onLoad() {
        Plugin heroesPlugin = Bukkit.getPluginManager().getPlugin("Heroes");
        if (heroesPlugin == null || !(heroesPlugin instanceof Heroes)) {
            getLogger().warning("Heroes plugin not found, MagicHeroes will not function.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            getLogger().info("Integrating with Heroes");
            this.heroes = (Heroes)heroesPlugin;
        }

        try {
            ClassPool pool = ClassPool.getDefault();
            ClassClassPath magicPath = new ClassClassPath(SpellSkill.class);
            pool.insertClassPath(magicPath);
            ClassClassPath heroesPath = new ClassClassPath(ActiveSkill.class);
            pool.insertClassPath(heroesPath);
            CtClass cc = pool.get("com.elmakers.mine.bukkit.heroes.SpellSkill");
            cc.setName("Test-SpellSkill");

            SkillRegistrar.registerSkill(this, (Class<? extends Skill>)cc.toClass());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        Plugin magicPlugin = Bukkit.getPluginManager().getPlugin("Magic");
        if (magicPlugin == null || !(magicPlugin instanceof MagicAPI)) {
            getLogger().warning("Magic plugin not found, MagicHeroes will not function.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            getLogger().info("Integrating with Magic");
            this.magicAPI = (MagicAPI)magicPlugin;
        }

        saveDefaultConfig();
    }
}