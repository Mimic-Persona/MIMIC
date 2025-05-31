package com.codecool.dungeoncrawl.agent.skill_library.skill_test.aggressive.code;

import com.codecool.dungeoncrawl.agent.GLog;
import com.codecool.dungeoncrawl.agent.skill_library.basic_skills.KillMob;
import com.codecool.dungeoncrawl.agent.skill_library.basic_skills.Navigate;
import com.codecool.dungeoncrawl.agent.skill_library.basic_skills.PickUp;
import com.codecool.dungeoncrawl.agent.skill_library.basic_skills.Utils;

import static com.codecool.dungeoncrawl.gui.Main.map;

public class AttackBackbone {
    public static void attackBackbone() {
        KillMob.killMob(map.getNearestMob());
    }

    public static void main(String[] args) {
        attackBackbone();
    }
}