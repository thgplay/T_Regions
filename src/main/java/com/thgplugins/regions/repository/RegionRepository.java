package com.thgplugins.regions.repository;

import com.thgplugins.regions.RegionsPlugin;
import com.thgplugins.regions.interfaces.IRepository;
import com.thgplugins.regions.model.Region;
import com.thgplugins.regions.util.RegionsConstants;
import lombok.Getter;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RegionRepository implements IRepository {

    @Getter
    private static RegionRepository instance;

    @Language("MySQL")
    private final String CREATE_TABLE_QUERY = """
            CREATE TABLE IF NOT EXISTS `regions` (
               id         BIGINT AUTO_INCREMENT,
               name       VARCHAR(32) NOT NULL,
               x1         INT         NOT NULL,
               x2         INT         NOT NULL,
               z1         INT         NOT NULL,
               z2         INT         NOT NULL,
               owner      VARCHAR(36) NOT NULL,
               whitelist  LONGTEXT NULL,
               CONSTRAINT regions_pk
               PRIMARY KEY (id)
            ) CHARSET = utf8;
            """;

    @Override
    public void init() {
        instance = this;
        createTableIfNotExists();
    }

    @NotNull
    private String getTable(){
        return String.format("`%s`.`%s`", RegionsConstants.database, getTableName());
    }

    @NotNull
    private String getTableName(){
        return "regions";
    }

    @Override
    public void createTableIfNotExists() {
        RegionsPlugin.getSQL().executeUpdate(CREATE_TABLE_QUERY).exceptionally(this::handleExceptionally);
    }


    @NotNull
    public CompletableFuture<Boolean> insert(@NotNull Region region){

        var future = new CompletableFuture<Boolean>();

        @Language("MySQL")
        var query = """
                INSERT INTO %s (name,x1,x2,z1,z2,owner) VALUES(?,?,?,?,?,?)
                """;

        RegionsPlugin.getSQL().insertDatabase(String.format(query, getTable(),
                        region.getName(),
                        region.getX1(),
                        region.getX2(),
                        region.getZ1(),
                        region.getZ2(),
                        region.getOwner().toString())).thenAccept(id -> {
            region.setId(id);
            future.complete(true);
        }).exceptionally(this::handleExceptionally);;

        return future;
    }

    @NotNull
    public CompletableFuture<Boolean> delete(@NotNull Region region){

        var future = new CompletableFuture<Boolean>();

        @Language("MySQL")
        var query = """
                DELETE FROM %s WHERE id = ?
                """;

        RegionsPlugin.getSQL().executeUpdate(String.format(query, getTable()), region.getId())
                .thenRun(() -> future.complete(true))
                .exceptionally(this::handleExceptionally);

        return future;
    }

    @NotNull
    public CompletableFuture<ArrayList<Region>> fetchAll(){

        var future = new CompletableFuture<ArrayList<Region>>();

        @Language("MySQL")
        var query = "SELECT * FROM %s";

        RegionsPlugin.getSQL().executeQuery(String.format(query, getTable()), rs -> {
            try{
                var list = new ArrayList<Region>();

                while (rs.next()){
                    list.add(new Region(rs));
                }

                future.complete(list);
            }catch (Exception ex){
                ex.printStackTrace();
                future.completeExceptionally(ex);
            }
        }).exceptionally(this::handleExceptionally);


        return future;
    }

    @NotNull
    public CompletableFuture<Optional<Region>> fetch(double x, double z){

        var future = new CompletableFuture<Optional<Region>>();

        @Language("MySQL")
        var query = "SELECT * FROM %s WHERE ? BETWEEN x1 AND x2 AND ? BETWEEN z1 AND z2";

        RegionsPlugin.getSQL().executeQuery(String.format(query, getTable()), rs -> {
            try {
                if (rs.next()){
                    future.complete(Optional.of(new Region(rs)));
                } else future.complete(Optional.empty());
            }catch (Exception ex){
                future.completeExceptionally(ex);
            }
        }, x, z);

        return future;
    }

    @NotNull
    public CompletableFuture<Void> update(@NotNull Region region){

        var future = new CompletableFuture<Void>();

        @Language("MySQL")
        var query = """
                UPDATE %s SET `name` = ?, `whitelist` = ?, x1 = ?, x2 = ?, z1 = ?, z2 = ? WHERE id = ?
                """;

        RegionsPlugin.getSQL().executeUpdate(String.format(query, getTable()),
                region.getName(),
                String.join(",", region.getWhitelist()),
                region.getX1(),
                region.getX2(),
                region.getZ1(),
                region.getZ2(),
                region.getId()).thenRun(() -> future.complete(null))
                .exceptionally(this::handleExceptionally);

        return future;
    }









}
