package com.elmakers.mine.bukkit.heroes;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;

public class SpellSkill extends ActiveSkill {

    public SpellSkill(Heroes heroes) {
        super(heroes, "Testing");
    }

    @Override
    public SkillResult use(Hero hero, String[] strings) {
        hero.getPlayer().sendMessage("TESTING!");
        return SkillResult.NORMAL;
    }

    @Override
    public String getDescription(Hero hero) {
        return this.getDescription();
    }
}
