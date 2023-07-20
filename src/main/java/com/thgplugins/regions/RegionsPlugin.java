package com.thgplugins.regions;

import com.thgplugins.regions.command.RegionCommand;
import com.thgplugins.regions.controller.RegionController;
import com.thgplugins.regions.interfaces.IConstructor;
import com.thgplugins.regions.interfaces.IController;
import com.thgplugins.regions.listener.RegionListener;
import com.thgplugins.regions.listener.RegionManageListener;
import com.thgplugins.regions.repository.AsyncSQLAPI;
import fr.minuskube.inv.InventoryManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RegionsPlugin extends JavaPlugin implements IConstructor{


    @Getter
    private static RegionsPlugin instance;

    @Getter
    private static InventoryManager inventoryManager;

    @Getter
    private static AsyncSQLAPI SQL;

    private List<IController> controllerList;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        init();
    }

    @Override
    public void onDisable() {
        this.controllerList.forEach(IController::unload);
        SQL.close();
    }

    public void init(){

        /* Setup databases */
        try {
            SQL = new AsyncSQLAPI(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        initControllers();
        initListeners();
        initCommands();
        initTasks();
    }






    public void initControllers(){

        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        this.controllerList = Stream.of(
                new RegionController()
        ).collect(Collectors.toList());
        this.controllerList.forEach(IController::init);
        this.controllerList.forEach(IController::loadRepositories);


    }

    @Override
    public void initCommands() {
        register(new RegionCommand(this));
    }

    public void initListeners() {
        register(this,
                new RegionListener(),
                new RegionManageListener()

        );
    }

    public void initTasks() {
    }





}
