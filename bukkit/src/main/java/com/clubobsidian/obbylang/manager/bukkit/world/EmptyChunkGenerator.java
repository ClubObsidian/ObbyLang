package com.clubobsidian.obbylang.manager.bukkit.world;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

class EmptyChunkGenerator extends ChunkGenerator {
	   
    @Override
    public byte[] generate(World world, Random random, int cx, int cz) 
    {
        int height = world.getMaxHeight();
        byte[] result = new byte[256 * height];
        return result;
    }
}