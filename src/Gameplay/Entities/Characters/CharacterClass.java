/* Copyright (C) All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Dustin Loughrin <zapoke@gmail.com>, 2018 - 2020
 */

package Gameplay.Entities.Characters;

public class CharacterClass
{
    private static int uniqueID = 0;
    private int myUID;
    private String name;
    private int statEXPForLevel[];

    //TODO: Abilities for certain levels?

    public CharacterClass(String n, int sEXP[])
    {
        name = n;
        statEXPForLevel = sEXP;
        myUID = uniqueID;
        uniqueID++;
    }

    public String getClassName() { return name; }
    public int getUID() { return myUID; }

    public int[] adjustStatEXP(int cStatEXP[])
    {
        int newStats[] = cStatEXP;
        for(int i = 0; i < cStatEXP.length; i++)
        {
            newStats[i] += statEXPForLevel[i];
        }
        return newStats;
    }
}
