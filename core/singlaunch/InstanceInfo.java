package singlaunch;

public class InstanceInfo {
    public String id;
    public String name;
    public String versionId;
    public long createdAt;

    public InstanceInfo() {}

    public InstanceInfo(String id, String name, String versionId) {
        this.id = id;
        this.name = name;
        this.versionId = versionId;
        this.createdAt = System.currentTimeMillis();
    }
}
