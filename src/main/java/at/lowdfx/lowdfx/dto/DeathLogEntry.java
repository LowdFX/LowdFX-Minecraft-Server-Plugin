package at.lowdfx.lowdfx.dto;

public class DeathLogEntry {
    private final String player;
    private final String killer;
    private final String cause;
    private final String weapon;
    private final String deathTime;
    private final String inventory;

    public DeathLogEntry(String player, String killer, String cause, String weapon, String deathTime, String inventory) {
        this.player = player;
        this.killer = killer;
        this.cause = cause;
        this.weapon = weapon;
        this.deathTime = deathTime;
        this.inventory = inventory;
    }

    public String getPlayer() {
        return player;
    }

    public String getKiller() {
        return killer;
    }

    public String getCause() {
        return cause;
    }

    public String getWeapon() {
        return weapon;
    }

    public String getDeathTime() {
        return deathTime;
    }

    public String getInventory() {
        return inventory;
    }
}
