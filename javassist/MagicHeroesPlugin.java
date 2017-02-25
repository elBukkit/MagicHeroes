package com.elmakers.mine.bukkit.heroes;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillRegistrar;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

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

            URLClassLoader loader = (URLClassLoader)(heroes.getClass().getClassLoader());

            while (loader != null)
            {
                URL[] urls = loader.getURLs();
                for (URL url : urls) {
                    String jarFile = url.toString().substring(5);
                    org.bukkit.Bukkit.getLogger().info("Adding: " + jarFile);
                    //URLClassPath path = new javassist.URLClassPath(url.getHost(), url.getPort(), url.getPath(), url.getFile());
                    pool.insertClassPath(jarFile);
                }
                loader = (URLClassLoader)loader.getParent();
            }
            ClassClassPath heroesPath = new javassist.ClassClassPath(heroes.getClass());
            pool.insertClassPath(heroesPath);

            ClassClassPath magicPath = new ClassClassPath(SpellSkill.class);
            pool.insertClassPath(magicPath);
            //pool.insertClassPath("/Users/nathan/Servers/elHeroes/plugins/Heroes.jar");

            pool.appendClassPath(new LoaderClassPath(org.bukkit.Bukkit.class.getClassLoader()));

            String path = new File(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
            System.out.println("Attempting Instrumentation to " + path);
            pool.insertClassPath(path);

            String magicPath2 = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
            System.out.println("Attempting Instrumentation to " + magicPath2);
            pool.insertClassPath(magicPath2);

            String skillPath = new File(ActiveSkill.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
            System.out.println("Attempting Instrumentation to " + skillPath);
            pool.insertClassPath(skillPath);

            CtClass spellClass = pool.get("com.elmakers.mine.bukkit.heroes.SpellSkill");
            spellClass.setName("Test-SpellSkill");
            SkillRegistrar.registerSkill(this, (Class<? extends Skill>)spellClass.toClass());
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