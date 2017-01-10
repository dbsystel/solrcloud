package db.garagedays.solrcloud.model;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 10.01.17.
 */
public class ConfigClass {

    private int shards = 1;
    private int replicas = 1;

    public int getShards() {
        return shards;
    }

    public void setShards(int shards) {
        this.shards = shards;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }
}
